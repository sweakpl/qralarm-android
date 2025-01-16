package com.sweak.qralarm.core.ui.components.code_scanner.view

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraConfig(private val context: Context) {

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    var flashEnabled = false
        private set

    private val cameraSelector: CameraSelector by lazy {
        CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
    }

    private val preview: Preview by lazy {
        Preview.Builder().build()
    }

    private val resolutionSelector: ResolutionSelector by lazy {
        ResolutionSelector.Builder().build()
    }

    private val imageAnalysis: ImageAnalysis by lazy {
        ImageAnalysis.Builder().apply {
            setResolutionSelector(resolutionSelector)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setOutputImageRotationEnabled(true)
            }
        }.build()
    }

    fun startCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        try {
            ProcessCameraProvider.configureInstance(Camera2Config.defaultConfig())
        } catch (illegalStateException: IllegalStateException) {  /* no-op */ }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get().apply {
                try {
                    unbindAll()
                    preview.surfaceProvider = previewView.surfaceProvider
                    camera = bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    ).apply {
                        configureAutoFocus(previewView, this)
                    }
                } catch (exception: Exception) {
                    Log.e("CameraConfig", "Error when starting camera", exception)
                }
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopCamera() {
        switchOffFlash()
        imageAnalysis.clearAnalyzer()

        cameraProvider?.let {
            it.unbindAll()
            camera = null
        }

        if (!cameraExecutor.isShutdown) {
            cameraExecutor.shutdownNow()
        }
    }

    private fun configureAutoFocus(previewView: PreviewView, camera: Camera) {
        previewView.afterMeasured {
            val previewViewWidth = previewView.width.toFloat()
            val previewViewHeight = previewView.height.toFloat()

            val autoFocusPoint = SurfaceOrientedMeteringPointFactory(
                previewViewWidth, previewViewHeight
            ).createPoint(previewViewWidth / 2.0f, previewViewHeight / 2.0f)

            try {
                camera.cameraControl.startFocusAndMetering(
                    FocusMeteringAction
                        .Builder(autoFocusPoint, FocusMeteringAction.FLAG_AF)
                        .setAutoCancelDuration(2, TimeUnit.SECONDS)
                        .build()
                )
            } catch (exception: CameraInfoUnavailableException) {
                Log.d("CameraConfig", "Cannot access camera", exception)
            }
        }
    }

    private inline fun View.afterMeasured(crossinline block: () -> Unit) {
        if (measuredWidth > 0 && measuredHeight > 0) {
            block()
        } else {
            viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        if (measuredWidth > 0 && measuredHeight > 0) {
                            viewTreeObserver.removeOnGlobalLayoutListener(this)
                            block()
                        }
                    }
                }
            )
        }
    }

    fun setAnalyzer(analyzer: ImageAnalysis.Analyzer) {
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer)
    }

    fun hasFlash(): Boolean =
        context.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

    fun switchFlash() {
        camera?.let {
            flashEnabled = !flashEnabled
            it.cameraControl.enableTorch(flashEnabled)
        }
    }

    private fun switchOffFlash() {
        if (flashEnabled) {
            switchFlash()
        }
    }
}