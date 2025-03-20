package com.sweak.qralarm.core.ui.components.code_scanner.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.sweak.qralarm.R
import com.sweak.qralarm.core.ui.components.code_scanner.analyzer.CodeAnalyzer
import com.sweak.qralarm.databinding.FragmentCodeScannerBinding

class CodeScannerFragment : Fragment(), CodeAnalyzer.CodeDetector {

    var decodeCallback: (codeValue: String) -> Unit = { /* no-op */ }
    var closeCallback: () -> Unit = { /* no-op */ }

    private var _binding: FragmentCodeScannerBinding? = null
    private val viewBinding get() = _binding!!

    private var cameraConfig: CameraConfig? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCodeScannerBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureCamera()
        configureFlash()
        configureClose()
    }

    override fun onDestroyView() {
        cameraConfig?.stopCamera()
        cameraConfig = null
        _binding = null
        
        super.onDestroyView()
    }

    private fun configureCamera() {
        cameraConfig = CameraConfig(requireContext()).apply {
            setAnalyzer(CodeAnalyzer(this@CodeScannerFragment))
            startCamera(
                lifecycleOwner = this@CodeScannerFragment as LifecycleOwner,
                previewView = viewBinding.cameraXScannerPreviewView
            )
        }
    }

    private fun configureFlash() {
        viewBinding.flashButton.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                if (cameraConfig?.flashEnabled == true) R.drawable.ic_flash_on
                else R.drawable.ic_flash_off
            )
        )

        viewBinding.flashButton.setOnClickListener {
            cameraConfig?.run {
                if (hasFlash()) {
                    switchFlash()

                    viewBinding.flashButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            if (flashEnabled) R.drawable.ic_flash_on else R.drawable.ic_flash_off
                        )
                    )
                }
            }
        }
    }

    private fun configureClose() {
        viewBinding.closeButton.setOnClickListener {
            closeCallback()
        }
    }

    override fun onCodeFound(codeValue: String) {
        decodeCallback(codeValue)
    }

    override fun onError(exception: Exception) {
        Log.e("CodeScannerFragment", exception.toString())
    }
}