package com.sweak.qralarm.core.ui.components.code_scanner.analyzer

interface CodeDetector {
    fun onCodeFound(codeValue: String, hasStrongErrorCorrection: Boolean)
    fun onError(exception: Exception)
}
