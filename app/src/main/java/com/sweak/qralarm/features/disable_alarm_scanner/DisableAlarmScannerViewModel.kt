package com.sweak.qralarm.features.disable_alarm_scanner

import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.ui.platform.WindowInfo
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.DisableAlarm
import com.sweak.qralarm.core.domain.alarm.SetAlarm
import com.sweak.qralarm.core.ui.components.code_scanner.analyzer.CodeDetector
import com.sweak.qralarm.core.ui.components.code_scanner.analyzer.ZXingCodeAnalyzer
import com.sweak.qralarm.features.disable_alarm_scanner.navigation.ID_OF_ALARM
import com.sweak.qralarm.features.disable_alarm_scanner.navigation.IS_DISABLING_BEFORE_ALARM_FIRED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class DisableAlarmScannerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alarmsRepository: AlarmsRepository,
    private val setAlarm: SetAlarm,
    private val disableAlarm: DisableAlarm
) : ViewModel() {

    private val idOfAlarm: Long = savedStateHandle[ID_OF_ALARM] ?: 0
    private val isDisablingBeforeAlarmFired: Boolean =
        savedStateHandle.get<Boolean>(IS_DISABLING_BEFORE_ALARM_FIRED) == true

    private lateinit var alarm: Alarm
    private var assignedCode: String? = null

    private var camera: Camera? = null
    private var shouldScan = true

    private var _state = MutableStateFlow(DisableAlarmScannerScreenState())
    val state = _state.asStateFlow()

    private val backendEventsChannel = Channel<DisableAlarmScannerScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    private var lastWrongCodeWarningMillis = 0L
    private val wrongCodeWarningDelayMillis = 3000L

    init {
        viewModelScope.launch {
            alarmsRepository.getAlarm(alarmId = idOfAlarm)?.let {
                alarm = it
            }
        }
    }

    fun onEvent(event: DisableAlarmScannerScreenUserEvent) {
        when (event) {
            is DisableAlarmScannerScreenUserEvent.InitializeCamera -> viewModelScope.launch {
                val imageAnalysisUseCase = getImageAnalysisUseCase()
                val cameraExecutor = Executors.newSingleThreadExecutor()

                imageAnalysisUseCase.setAnalyzer(cameraExecutor, getCodeAnalyzer())

                try {
                    ProcessCameraProvider.configureInstance(Camera2Config.defaultConfig())
                } catch (_: IllegalStateException) { /* no-op */ }

                val processCameraProvider =
                    ProcessCameraProvider.awaitInstance(event.appContext).also { it.unbindAll() }

                try {
                    camera = processCameraProvider.bindToLifecycle(
                        event.lifecycleOwner,
                        DEFAULT_BACK_CAMERA,
                        getCameraPreviewUseCase(),
                        imageAnalysisUseCase
                    ).apply {
                        configureAutoFocus(event.windowInfo)
                    }
                } catch (_: Exception) {
                    cleanUpCameraResources(
                        imageAnalysisUseCase,
                        processCameraProvider,
                        cameraExecutor
                    )
                    backendEventsChannel.send(
                        DisableAlarmScannerScreenBackendEvent.CameraInitializationError
                    )
                }

                try {
                    awaitCancellation()
                } finally {
                    cleanUpCameraResources(
                        imageAnalysisUseCase,
                        processCameraProvider,
                        cameraExecutor
                    )
                }
            }
            is DisableAlarmScannerScreenUserEvent.ToggleFlash -> {
                camera?.let {
                    _state.update { currentState ->
                        val flashEnabled = !currentState.isFlashEnabled
                        it.cameraControl.enableTorch(flashEnabled)
                        currentState.copy(isFlashEnabled = flashEnabled)
                    }
                }
            }
            else -> { /* no-op */ }
        }
    }

    private fun cleanUpCameraResources(
        imageAnalysisUseCase: ImageAnalysis,
        processCameraProvider: ProcessCameraProvider,
        cameraExecutor: ExecutorService?
    ) {
        turnOffFlash()
        imageAnalysisUseCase.clearAnalyzer()
        processCameraProvider.unbindAll()
        camera = null
        cameraExecutor?.let { if (!it.isShutdown) it.shutdownNow() }
    }

    private fun getCodeAnalyzer(): ImageAnalysis.Analyzer {
        return ZXingCodeAnalyzer(getBarcodeDetector())
    }

    private fun getBarcodeDetector(): CodeDetector =
        object : CodeDetector {
            override fun onCodeFound(codeValue: String) {
                if (shouldScan && ::alarm.isInitialized) {
                    shouldScan = false

                    viewModelScope.launch {
                        if (assignedCode == null) {
                            disableAlarm(codeValue)
                        } else {
                            if (codeValue == assignedCode) {
                                disableAlarm(codeValue)
                            } else {
                                showIncorrectCodeWarning()

                                shouldScan = true
                            }
                        }
                    }
                }
            }

            override fun onError(exception: Exception) {
                Log.e("BarcodeDetector", exception.toString())
            }
        }

    private fun getCameraPreviewUseCase() =
        Preview.Builder().build().apply {
            setSurfaceProvider { newSurfaceRequest ->
                _state.update { currentState ->
                    currentState.copy(surfaceRequest = newSurfaceRequest)
                }
            }
        }

    private fun getImageAnalysisUseCase() =
        ImageAnalysis.Builder().apply {
            setResolutionSelector(ResolutionSelector.Builder().build())
            setOutputImageRotationEnabled(true)
        }.build()

    private fun Camera.configureAutoFocus(windowInfo: WindowInfo) {
        val windowHeight = windowInfo.containerSize.height.toFloat()
        val windowWidth = windowInfo.containerSize.width.toFloat()
        val autoFocusPoint = SurfaceOrientedMeteringPointFactory(
            windowWidth,
            windowHeight
        ).createPoint(windowWidth / 2, windowHeight / 2)

        try {
            cameraControl.startFocusAndMetering(
                FocusMeteringAction
                    .Builder(autoFocusPoint, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(2, TimeUnit.SECONDS)
                    .build()
            )
        } catch (exception: CameraInfoUnavailableException) {
            Log.d("AutoFocus", "Cannot access camera", exception)
        }
    }

    private fun turnOffFlash() {
        if (state.value.isFlashEnabled) {
            camera?.cameraControl?.enableTorch(false)
        }
    }

    private suspend fun disableAlarm(scannedCodeText: String) {
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
        sendCorrectCodeScannedConfirmation(scannedCodeText)
    }

    private suspend fun sendCorrectCodeScannedConfirmation(scannedCodeText: String) {
        backendEventsChannel.send(
            DisableAlarmScannerScreenBackendEvent.CorrectCodeScanned(
                uriStringToOpen =
                    if (alarm.isOpenCodeLinkEnabled) scannedCodeText else null
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
                delay(2500)

                _state.update { currentState ->
                    currentState.copy(shouldShowIncorrectCodeWarning = false)
                }
            }
        }
    }
}
