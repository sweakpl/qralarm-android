package com.sweak.qralarm.features.custom_code_scanner

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
import com.google.zxing.Result
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.ui.components.code_scanner.analyzer.AbstractCodeAnalyzer
import com.sweak.qralarm.core.ui.components.code_scanner.analyzer.CodeAnalyzer
import com.sweak.qralarm.features.custom_code_scanner.navigation.SHOULD_SCAN_FOR_DEFAULT_CODE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class CustomCodeScannerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val shouldScanForDefaultCode =
        savedStateHandle.get<Boolean>(SHOULD_SCAN_FOR_DEFAULT_CODE) == true

    var state = MutableStateFlow(CustomCodeScannerScreenState())

    private val backendEventsChannel = Channel<CustomCodeScannerScreenBackendEvent>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    private var camera: Camera? = null

    fun onEvent(event: CustomCodeScannerScreenUserEvent) {
        when (event) {
            is CustomCodeScannerScreenUserEvent.InitializeCamera -> viewModelScope.launch {
                val imageAnalysisUseCase = getImageAnalysisUseCase()
                val cameraExecutor = Executors.newSingleThreadExecutor()

                imageAnalysisUseCase.setAnalyzer(cameraExecutor, getCodeAnalyzer())

                try {
                    ProcessCameraProvider.configureInstance(Camera2Config.defaultConfig())
                } catch (_: IllegalStateException) { /* no-op */ }

                val processCameraProvider = ProcessCameraProvider.awaitInstance(event.appContext)
                camera = processCameraProvider.bindToLifecycle(
                    event.lifecycleOwner,
                    DEFAULT_BACK_CAMERA,
                    getCameraPreviewUseCase(),
                    imageAnalysisUseCase
                ).apply {
                    configureAutoFocus(event.windowInfo)
                }

                try {
                    awaitCancellation()
                } finally {
                    turnOffFlash()
                    imageAnalysisUseCase.clearAnalyzer()
                    processCameraProvider.unbindAll()
                    camera = null

                    if (!cameraExecutor.isShutdown) {
                        cameraExecutor.shutdownNow()
                    }
                }
            }
            is CustomCodeScannerScreenUserEvent.ToggleFlash -> {
                camera?.let {
                    state.update { currentState ->
                        val flashEnabled = !currentState.isFlashEnabled
                        it.cameraControl.enableTorch(flashEnabled)
                        currentState.copy(isFlashEnabled = flashEnabled)
                    }
                }
            }
            else -> { /* no-op */ }
        }
    }

    private fun getCodeAnalyzer(): AbstractCodeAnalyzer {
        return CodeAnalyzer(getBarcodeDetector())
    }

    private fun getBarcodeDetector(): AbstractCodeAnalyzer.BarcodeDetector =
        object : AbstractCodeAnalyzer.BarcodeDetector {
            override fun onBarcodeFound(result: Result) {
                viewModelScope.launch {
                    if (shouldScanForDefaultCode) {
                        userDataRepository.setDefaultAlarmCode(code = result.text)
                    } else {
                        userDataRepository.setTemporaryScannedCode(code = result.text)
                    }

                    backendEventsChannel.send(
                        CustomCodeScannerScreenBackendEvent.CustomCodeSaved
                    )
                }
            }

            override fun onError(msg: String) {
                Log.e("BarcodeDetector", msg)
            }
        }

    private fun getCameraPreviewUseCase() =
        Preview.Builder().build().apply {
            setSurfaceProvider { newSurfaceRequest ->
                state.update {
                    it.copy(surfaceRequest = newSurfaceRequest)
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
}