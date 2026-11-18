package com.sweak.qralarm.features.disable_alarm_scanner

import android.os.Build
import android.util.Log
import android.util.Size
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.ui.platform.WindowInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.DisableAlarm
import com.sweak.qralarm.core.domain.alarm.DismissalMethod
import com.sweak.qralarm.core.domain.alarm.SetAlarm
import com.sweak.qralarm.core.domain.recognition.ObjectCategories
import com.sweak.qralarm.core.ui.components.code_scanner.analyzer.CodeDetector
import com.sweak.qralarm.core.ui.components.code_scanner.analyzer.ZXingCodeAnalyzer
import com.sweak.qralarm.core.domain.custom_object.CustomObjectsRepository
import com.sweak.qralarm.core.ui.components.object_scanner.CustomObjectAnalyzer
import com.sweak.qralarm.core.ui.components.object_scanner.ObjectAnalyzer
import com.sweak.qralarm.core.ui.components.object_scanner.ObjectDetectionBox
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DisableAlarmScannerViewModel.Factory::class)
class DisableAlarmScannerViewModel @AssistedInject constructor(
    @Assisted private val idOfAlarm: Long,
    @Assisted private val isDisablingBeforeAlarmFired: Boolean,
    private val alarmsRepository: AlarmsRepository,
    private val setAlarm: SetAlarm,
    private val disableAlarm: DisableAlarm,
    private val customObjectsRepository: CustomObjectsRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            idOfAlarm: Long,
            isDisablingBeforeAlarmFired: Boolean
        ): DisableAlarmScannerViewModel
    }

    private lateinit var alarm: Alarm

    private var camera: Camera? = null
    private val shouldScan = AtomicBoolean(true)
    private val challengeCompleted = AtomicBoolean(false)
    private val cameraSession = AtomicInteger()

    private var _state = MutableStateFlow(DisableAlarmScannerScreenState())
    val state = _state.asStateFlow()

    private val backendEventsChannel = Channel<DisableAlarmScannerScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    private var lastWrongCodeWarningMillis = 0L
    private val wrongCodeWarningDelayMillis = 3000L

    private val loadedAlarm = viewModelScope.async {
        requireNotNull(alarmsRepository.getAlarm(alarmId = idOfAlarm))
    }

    /** The screen owns this coroutine so leaving or backgrounding it releases the camera. */
    @androidx.annotation.OptIn(androidx.camera.lifecycle.ExperimentalCameraProviderConfiguration::class)
    suspend fun initializeCamera(event: DisableAlarmScannerScreenUserEvent.InitializeCamera) {
        val session = cameraSession.incrementAndGet()
        var imageAnalysis: ImageAnalysis? = null
        var provider: ProcessCameraProvider? = null
        var executor: ExecutorService? = null
        var analyzer: ImageAnalysis.Analyzer? = null
        try {
            alarm = loadedAlarm.await()
            if (challengeCompleted.get()) awaitCancellation()
            shouldScan.set(true)
            _state.update { it.copy(
                codeName = if (alarm.dismissalMethod == DismissalMethod.CODE) alarm.assignedCode?.name else null,
                objectName = when (alarm.dismissalMethod) {
                    DismissalMethod.OBJECT -> alarm.objectCategoryId?.let(ObjectCategories::displayName)
                    DismissalMethod.CUSTOM_OBJECT -> alarm.customObjectId
                        ?.let { id -> customObjectsRepository.getObject(id)?.name } ?: ""
                    else -> null
                },
                isCustomObject = alarm.dismissalMethod == DismissalMethod.CUSTOM_OBJECT,
                detectionBox = null, confirmationProgress = 0f, recognitionError = false
            ) }
            check(alarm.dismissalMethod.requiresCamera)
            val onObjectResult: (ObjectDetectionBox?, Float, Boolean) -> Unit = { box, progress, completed ->
                viewModelScope.launch {
                    if (cameraSession.get() == session && shouldScan.get()) {
                        _state.update { it.copy(detectionBox = box, confirmationProgress = progress) }
                        if (completed && shouldScan.compareAndSet(true, false)) completeChallenge(null)
                    }
                }
            }
            analyzer = if (alarm.dismissalMethod == DismissalMethod.CUSTOM_OBJECT) {
                val objectId = requireNotNull(alarm.customObjectId)
                val customObject = requireNotNull(customObjectsRepository.getObject(objectId))
                val references = requireNotNull(customObjectsRepository.loadReferenceFeatures(objectId))
                CustomObjectAnalyzer(
                    event.appContext, references, customObject.embeddingThreshold,
                    onResult = onObjectResult,
                    onError = { exception ->
                        Log.e("CustomObjectAnalyzer", "Recognition failed", exception)
                        viewModelScope.launch {
                            if (cameraSession.get() == session) {
                                _state.update { it.copy(recognitionError = true, detectionBox = null, confirmationProgress = 0f) }
                            }
                        }
                    }
                )
            } else if (alarm.dismissalMethod == DismissalMethod.OBJECT) {
                val target = requireNotNull(alarm.objectCategoryId)
                require(target in ObjectCategories.ids)
                ObjectAnalyzer(event.appContext, target, onResult = { box, progress, completed ->
                    viewModelScope.launch {
                        if (cameraSession.get() == session && shouldScan.get()) {
                            _state.update { it.copy(detectionBox = box, confirmationProgress = progress) }
                            if (completed && shouldScan.compareAndSet(true, false)) completeChallenge(null)
                        }
                    }
                }, onError = { exception ->
                    Log.e("ObjectAnalyzer", "Recognition failed", exception)
                    viewModelScope.launch {
                        if (cameraSession.get() == session) {
                            _state.update { it.copy(recognitionError = true, detectionBox = null, confirmationProgress = 0f) }
                        }
                    }
                })
            } else {
                ZXingCodeAnalyzer(getBarcodeDetector(session))
            }
            val analysis = getImageAnalysisUseCase().also { imageAnalysis = it }
            val worker = Executors.newSingleThreadExecutor().also { executor = it }
            analysis.setAnalyzer(worker, analyzer)
            try {
                ProcessCameraProvider.configureInstance(Camera2Config.defaultConfig())
            } catch (_: IllegalStateException) { /* Already configured by another scanner. */ }
            val cameraProvider = ProcessCameraProvider.awaitInstance(event.appContext).also { provider = it }
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                event.lifecycleOwner, DEFAULT_BACK_CAMERA, getCameraPreviewUseCase(), analysis
            ).apply { configureAutoFocus(event.windowInfo) }
            awaitCancellation()
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (exception: Exception) {
            Log.e("AlarmCamera", "Camera challenge failed", exception)
            if (::alarm.isInitialized && alarm.dismissalMethod.isObject) {
                _state.update { it.copy(recognitionError = true) }
                awaitCancellation()
            } else {
                backendEventsChannel.send(DisableAlarmScannerScreenBackendEvent.CameraInitializationError)
            }
        } finally {
            cameraSession.incrementAndGet()
            turnOffFlash()
            imageAnalysis?.clearAnalyzer()
            provider?.unbindAll()
            camera = null
            _state.update { it.copy(surfaceRequest = null, isFlashEnabled = false, detectionBox = null, confirmationProgress = 0f) }
            // Queue closure behind any inference in progress, on the detector's owning thread.
            val closable = analyzer as? AutoCloseable
            if (executor != null) {
                executor.execute { closable?.close() }
                executor.shutdown()
            } else closable?.close()
        }
    }

    fun onEvent(event: DisableAlarmScannerScreenUserEvent) {
        if (event is DisableAlarmScannerScreenUserEvent.ToggleFlash) {
            camera?.takeIf { it.cameraInfo.hasFlashUnit() }?.let { activeCamera ->
                val enabled = !state.value.isFlashEnabled
                activeCamera.cameraControl.enableTorch(enabled)
                _state.update { it.copy(isFlashEnabled = enabled) }
            }
        }
    }

    private fun getBarcodeDetector(session: Int): CodeDetector = object : CodeDetector {
        override fun onCodeFound(codeValue: String, hasStrongErrorCorrection: Boolean) {
            if (cameraSession.get() != session || !shouldScan.compareAndSet(true, false)) return
            viewModelScope.launch {
                if (cameraSession.get() != session) return@launch
                if (alarm.assignedCode == null || codeValue == alarm.assignedCode?.value) {
                    completeChallenge(codeValue)
                } else {
                    showIncorrectCodeWarning()
                    if (cameraSession.get() == session && !challengeCompleted.get()) shouldScan.set(true)
                }
            }
        }
        override fun onError(exception: Exception) {
            Log.e("BarcodeDetector", exception.toString())
        }
    }

    private fun getCameraPreviewUseCase() =
        Preview.Builder().apply {
            if (alarm.dismissalMethod.isObject) {
                setResolutionSelector(ResolutionSelector.Builder().setResolutionStrategy(
                    ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
                ).build())
            }
        }.build().apply {
            setSurfaceProvider { newSurfaceRequest ->
                _state.update { currentState ->
                    currentState.copy(surfaceRequest = newSurfaceRequest)
                }
            }
        }

    private fun getImageAnalysisUseCase() =
        ImageAnalysis.Builder().apply {
            setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            if (alarm.dismissalMethod.isObject) {
                setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                setResolutionSelector(ResolutionSelector.Builder().setResolutionStrategy(
                    ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
                ).build())
            } else setResolutionSelector(ResolutionSelector.Builder().build())

            // Android 13 ImageWriter double-closes buffer fences in the frame rotation path,
            // crashing PowerVR devices - this got fixed in Android 14
            // https://android.googlesource.com/platform/frameworks/base/+/1165c90081ab1ae67b74fffff2c604573b3bde45
            if (alarm.dismissalMethod == DismissalMethod.CODE && Build.VERSION.SDK_INT != Build.VERSION_CODES.TIRAMISU) {
                setOutputImageRotationEnabled(true)
            }
        }.build()

    private fun Camera.configureAutoFocus(windowInfo: WindowInfo) {
        val windowHeight = windowInfo.containerSize.height.toFloat()
        val windowWidth = windowInfo.containerSize.width.toFloat()
        val autoFocusPoint = SurfaceOrientedMeteringPointFactory(
            windowWidth,
            windowHeight
        ).createPoint(windowWidth / 2, windowHeight / 2)

        cameraControl.startFocusAndMetering(
            FocusMeteringAction
                .Builder(autoFocusPoint, FocusMeteringAction.FLAG_AF)
                .setAutoCancelDuration(2, TimeUnit.SECONDS)
                .build()
        )
    }

    private fun turnOffFlash() {
        if (state.value.isFlashEnabled) {
            camera?.cameraControl?.enableTorch(false)
        }
    }

    private suspend fun completeChallenge(scannedCodeText: String?) {
        if (!challengeCompleted.compareAndSet(false, true)) return
        alarmsRepository.setAlarmSnoozed(
            alarmId = idOfAlarm,
            snoozed = false
        )

        if (isDisablingBeforeAlarmFired) {
            disableAlarm(alarmId = alarm.alarmId)
            alarmsRepository.setSkipNextAlarm(
                alarmId = alarm.alarmId,
                skip = true
            )
        }

        handleAlarmRescheduling()
        sendChallengeCompletedConfirmation(scannedCodeText)
    }

    private suspend fun sendChallengeCompletedConfirmation(scannedCodeText: String?) {
        backendEventsChannel.send(
            DisableAlarmScannerScreenBackendEvent.ChallengeCompleted(
                uriStringToOpen =
                    if (alarm.dismissalMethod == DismissalMethod.CODE && alarm.isOpenCodeLinkEnabled) scannedCodeText else null
            )
        )
    }

    private suspend fun handleAlarmRescheduling() {
        if (::alarm.isInitialized) {
            if (alarm.repeatingMode is Alarm.RepeatingMode.Once) {
                disableAlarm(alarmId = alarm.alarmId)
            } else if (alarm.repeatingMode is Alarm.RepeatingMode.Days) {
                setAlarm(
                    alarmId = alarm.alarmId,
                    isReschedulingMissedAlarm = false
                )
            }
        }
    }

    private suspend fun showIncorrectCodeWarning() = coroutineScope {
        val currentTimeInMillis = System.currentTimeMillis()

        if (currentTimeInMillis - lastWrongCodeWarningMillis > wrongCodeWarningDelayMillis) {
            _state.update { currentState ->
                currentState.copy(shouldShowIncorrectCodeWarning = true)
            }

            lastWrongCodeWarningMillis = currentTimeInMillis

            launch {
                delay(2500.milliseconds)

                _state.update { currentState ->
                    currentState.copy(shouldShowIncorrectCodeWarning = false)
                }
            }
        }
    }
}
