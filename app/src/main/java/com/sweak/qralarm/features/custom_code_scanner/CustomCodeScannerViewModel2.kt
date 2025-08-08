package com.sweak.qralarm.features.custom_code_scanner

import android.os.Build
import android.util.Log
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.Result
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.ui.components.code_scanner.analyzer.AbstractCodeAnalyzer
import com.sweak.qralarm.core.ui.components.code_scanner.analyzer.CodeAnalyzer
import com.sweak.qralarm.core.ui.components.code_scanner.analyzer.LegacyCodeAnalyzer
import com.sweak.qralarm.features.custom_code_scanner.navigation.SHOULD_SCAN_FOR_DEFAULT_CODE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class CustomCodeScannerViewModel2 @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val shouldScanForDefaultCode =
        savedStateHandle.get<Boolean>(SHOULD_SCAN_FOR_DEFAULT_CODE) == true

    var state = MutableStateFlow(CustomCodeScannerScreenState2())

    private val backendEventsChannel = Channel<CustomCodeScannerScreenBackendEvent2>()
    val backendEvents = backendEventsChannel.receiveAsFlow()

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private val cameraPreviewUseCase by lazy {
        Preview.Builder().build().apply {
            setSurfaceProvider { newSurfaceRequest ->
                state.update {
                    it.copy(surfaceRequest = newSurfaceRequest)
                }
            }
        }
    }

    private val imageAnalysisUseCase: ImageAnalysis by lazy {
        ImageAnalysis.Builder().apply {
            setResolutionSelector(ResolutionSelector.Builder().build())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setOutputImageRotationEnabled(true)
            }
        }.build()
    }

    fun onEvent(event: CustomCodeScannerScreenUserEvent2) {
        when (event) {
            is CustomCodeScannerScreenUserEvent2.BindToCamera -> viewModelScope.launch {
                val barcodeDetector = object : AbstractCodeAnalyzer.BarcodeDetector {
                    override fun onBarcodeFound(result: Result) {
                        viewModelScope.launch {
                            if (shouldScanForDefaultCode) {
                                userDataRepository.setDefaultAlarmCode(code = result.text)
                            } else {
                                userDataRepository.setTemporaryScannedCode(code = result.text)
                            }

                            backendEventsChannel.send(
                                CustomCodeScannerScreenBackendEvent2.CustomCodeSaved
                            )
                        }
                    }

                    override fun onError(msg: String) {
                        Log.e("BarcodeDetector", msg)
                    }
                }
                val analyzer: AbstractCodeAnalyzer =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        CodeAnalyzer(barcodeDetector)
                    } else {
                        LegacyCodeAnalyzer(barcodeDetector)
                    }
                imageAnalysisUseCase.setAnalyzer(cameraExecutor, analyzer)

                val processCameraProvider = ProcessCameraProvider.awaitInstance(event.appContext)
                processCameraProvider.bindToLifecycle(
                    event.lifecycleOwner,
                    DEFAULT_BACK_CAMERA,
                    cameraPreviewUseCase,
                    imageAnalysisUseCase
                )

                try {
                    awaitCancellation()
                } finally {
                    imageAnalysisUseCase.clearAnalyzer()
                    processCameraProvider.unbindAll()

                    if (!cameraExecutor.isShutdown) {
                        cameraExecutor.shutdownNow()
                    }
                }
            }
        }
    }
}