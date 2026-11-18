package com.sweak.qralarm.features.custom_object.enroll

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.custom_object.CustomObjectsRepository
import com.sweak.qralarm.core.recognition.custom.CustomObjectRecognizer
import com.sweak.qralarm.core.recognition.custom.EnrollmentRules
import com.sweak.qralarm.core.ui.components.object_scanner.ImageEmbedderEngine
import com.sweak.qralarm.core.ui.components.object_scanner.ObjectDetectionBox
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

data class EnrollObjectScreenState(
    val name: String = "",
    val isReEnrolling: Boolean = false,
    val surfaceRequest: SurfaceRequest? = null,
    val shots: List<Bitmap> = emptyList(),
    val isCapturing: Boolean = false,
    val isRecording: Boolean = false,
    val clipFoundNothing: Boolean = false,
    val shotIssue: EnrollmentRules.ShotIssue? = null,
    val inconsistentShotsRemoved: Int = 0,
    val step: Step = Step.CAPTURE,
    val testProgress: Float = 0f,
    val testBox: ObjectDetectionBox? = null,
    val isTestPassed: Boolean = false,
    val isSaving: Boolean = false,
    val showNameError: Boolean = false,
    val cameraError: Boolean = false
) {
    enum class Step { CAPTURE, TEST }
    val canContinue: Boolean get() = shots.size >= EnrollmentRules.MIN_REFERENCES
    val canCaptureMore: Boolean get() = shots.size < EnrollmentRules.MAX_REFERENCES
}

sealed class EnrollObjectBackendEvent {
    data class Saved(val objectId: Long) : EnrollObjectBackendEvent()
    data object SaveFailed : EnrollObjectBackendEvent()
}

@HiltViewModel(assistedFactory = EnrollObjectViewModel.Factory::class)
class EnrollObjectViewModel @AssistedInject constructor(
    @Assisted private val objectIdToReplace: Long,
    @param:ApplicationContext private val context: Context,
    private val repository: CustomObjectsRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(objectIdToReplace: Long): EnrollObjectViewModel
    }

    private val _state = MutableStateFlow(EnrollObjectScreenState(isReEnrolling = objectIdToReplace != 0L))
    val state = _state.asStateFlow()

    private val backendEventsChannel = Channel<EnrollObjectBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    private val shots = mutableListOf<EnrollmentAnalyzer.Shot>()
    private var analyzer: EnrollmentAnalyzer? = null
    private var embeddingThreshold = EnrollmentRules.GATE_MIN

    init {
        if (objectIdToReplace != 0L) viewModelScope.launch {
            repository.getObject(objectIdToReplace)?.let { existing -> _state.update { it.copy(name = existing.name) } }
        }
    }

    /** The screen owns this coroutine, so leaving or backgrounding it releases the camera. */
    @androidx.annotation.OptIn(androidx.camera.lifecycle.ExperimentalCameraProviderConfiguration::class)
    suspend fun runCamera(lifecycleOwner: LifecycleOwner) {
        val executor = Executors.newSingleThreadExecutor()
        val enrollmentAnalyzer = EnrollmentAnalyzer(
            context,
            onShot = { shot -> viewModelScope.launch { onShot(shot) } },
            onClip = { clip -> viewModelScope.launch { onClip(clip) } },
            onTestResult = { box, progress, completed ->
                viewModelScope.launch {
                    _state.update {
                        it.copy(testBox = box, testProgress = progress, isTestPassed = it.isTestPassed || completed)
                    }
                    if (completed) analyzer?.startTest(null)
                }
            },
            onError = { exception ->
                Log.e("EnrollObject", "Analysis failed", exception)
                viewModelScope.launch {
                    _state.update { it.copy(cameraError = true, isCapturing = false, isRecording = false) }
                }
            }
        )
        analyzer = enrollmentAnalyzer
        if (state.value.step == EnrollObjectScreenState.Step.TEST && !state.value.isTestPassed) {
            enrollmentAnalyzer.startTest(shots.map { it.reference.features }, embeddingThreshold)
        }
        var provider: ProcessCameraProvider? = null
        val resolution = ResolutionSelector.Builder().setResolutionStrategy(
            ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
        ).build()
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .setResolutionSelector(resolution)
            .build()
        try {
            analysis.setAnalyzer(executor, enrollmentAnalyzer)
            try {
                ProcessCameraProvider.configureInstance(Camera2Config.defaultConfig())
            } catch (_: IllegalStateException) { /* Already configured. */ }
            val cameraProvider = ProcessCameraProvider.awaitInstance(context).also { provider = it }
            cameraProvider.unbindAll()
            val preview = Preview.Builder().setResolutionSelector(resolution).build().apply {
                setSurfaceProvider { request -> _state.update { it.copy(surfaceRequest = request) } }
            }
            cameraProvider.bindToLifecycle(lifecycleOwner, DEFAULT_BACK_CAMERA, preview, analysis)
            awaitCancellation()
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (exception: Exception) {
            Log.e("EnrollObject", "Camera failed", exception)
            _state.update { it.copy(cameraError = true) }
        } finally {
            analysis.clearAnalyzer()
            provider?.unbindAll()
            analyzer = null
            _state.update { it.copy(surfaceRequest = null, isCapturing = false) }
            executor.execute { enrollmentAnalyzer.close() }
            executor.shutdown()
        }
    }

    fun onNameChanged(name: String) = _state.update { it.copy(name = name.take(40), showNameError = false) }

    fun onCaptureClicked() {
        if (state.value.isCapturing || !state.value.canCaptureMore) return
        _state.update { it.copy(isCapturing = true, shotIssue = null, inconsistentShotsRemoved = 0) }
        analyzer?.requestCapture()
    }

    fun onRecordClicked() {
        if (state.value.isCapturing || state.value.isRecording || !state.value.canCaptureMore) return
        _state.update {
            it.copy(isRecording = true, shotIssue = null, inconsistentShotsRemoved = 0, clipFoundNothing = false)
        }
        analyzer?.requestClip()
    }

    private fun onClip(clip: List<EnrollmentAnalyzer.Shot>) {
        val room = EnrollmentRules.MAX_REFERENCES - shots.size
        shots += clip.take(room.coerceAtLeast(0))
        clip.drop(room.coerceAtLeast(0)).forEach { it.preview.recycle() }
        _state.update { it.copy(isRecording = false, clipFoundNothing = clip.isEmpty()) }
        publishShots()
    }

    fun onRemoveShot(index: Int) {
        if (index !in shots.indices) return
        shots.removeAt(index)
        publishShots()
    }

    private fun onShot(shot: EnrollmentAnalyzer.Shot) {
        if (shot.issue == null && shots.size < EnrollmentRules.MAX_REFERENCES) shots += shot
        _state.update { it.copy(isCapturing = false, shotIssue = shot.issue) }
        publishShots()
    }

    private fun publishShots() = _state.update { it.copy(shots = shots.map { shot -> shot.preview }) }

    /** Drops photos that do not match the others; otherwise moves on to the live test. */
    fun onContinueClicked() = viewModelScope.launch {
        val features = shots.map { it.reference.features }
        val inconsistent = withContext(Dispatchers.Default) {
            ImageEmbedderEngine.ensureOpenCv()
            EnrollmentRules.inconsistentReferences(features)
        }
        if (inconsistent.isNotEmpty()) {
            inconsistent.sortedDescending().forEach { shots.removeAt(it) }
            publishShots()
            _state.update { it.copy(inconsistentShotsRemoved = inconsistent.size) }
            return@launch
        }
        embeddingThreshold = withContext(Dispatchers.Default) {
            val scenes = ImageEmbedderEngine.loadSceneEmbeddings(context)
            EnrollmentRules.embeddingThreshold(features.map { reference ->
                FloatArray(scenes.size) { CustomObjectRecognizer.cosine(reference.embedding, scenes[it]) }
            })
        }
        _state.update { it.copy(step = EnrollObjectScreenState.Step.TEST, testProgress = 0f, isTestPassed = false) }
        analyzer?.startTest(features, embeddingThreshold)
    }

    fun onBackToCaptureClicked() {
        analyzer?.startTest(null)
        _state.update { it.copy(step = EnrollObjectScreenState.Step.CAPTURE, testBox = null, testProgress = 0f) }
    }

    fun onSaveClicked() = viewModelScope.launch {
        val current = state.value
        if (current.name.isBlank()) {
            _state.update { it.copy(showNameError = true) }
            return@launch
        }
        if (!current.isTestPassed || current.isSaving) return@launch
        _state.update { it.copy(isSaving = true) }
        try {
            val references = shots.map { it.reference }
            val id = if (objectIdToReplace != 0L) {
                repository.replaceReferences(objectIdToReplace, references, embeddingThreshold)
                repository.renameObject(objectIdToReplace, current.name)
                objectIdToReplace
            } else {
                repository.addObject(current.name, references, embeddingThreshold)
            }
            backendEventsChannel.send(EnrollObjectBackendEvent.Saved(id))
        } catch (exception: Exception) {
            Log.e("EnrollObject", "Saving failed", exception)
            _state.update { it.copy(isSaving = false) }
            backendEventsChannel.send(EnrollObjectBackendEvent.SaveFailed)
        }
    }
}
