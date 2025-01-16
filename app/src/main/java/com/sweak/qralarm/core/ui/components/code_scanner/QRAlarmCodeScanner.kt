package com.sweak.qralarm.core.ui.components.code_scanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.google.zxing.Result
import com.sweak.qralarm.core.ui.components.code_scanner.view.CodeScannerFragment
import com.sweak.qralarm.databinding.LayoutCodeScannerBinding

@Composable
fun QRAlarmCodeScanner(
    decodeCallback: (result: Result) -> Unit,
    closeCallback: () -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidViewBinding(
        factory = LayoutCodeScannerBinding::inflate,
        modifier = modifier
    ) {
        val codeScannerFragment = fragmentContainerView.getFragment<CodeScannerFragment>()
        codeScannerFragment.decodeCallback = decodeCallback
        codeScannerFragment.closeCallback = closeCallback
    }
}