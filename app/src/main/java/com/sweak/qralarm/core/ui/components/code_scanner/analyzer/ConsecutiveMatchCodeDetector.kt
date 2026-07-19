package com.sweak.qralarm.core.ui.components.code_scanner.analyzer

import java.util.concurrent.atomic.AtomicReference

class ConsecutiveMatchCodeDetector(
    private val delegate: CodeDetector
) : CodeDetector by delegate {

    private val consecutiveScans = AtomicReference(ConsecutiveScans())

    override fun onCodeFound(codeValue: String, hasStrongErrorCorrection: Boolean) {
        if (hasStrongErrorCorrection) {
            delegate.onCodeFound(codeValue, true)
            return
        }

        // CAS loop instead of AtomicReference.updateAndGet which requires API 24+:
        while (true) {
            val previous = consecutiveScans.get()
            val current = ConsecutiveScans(
                codeValue = codeValue,
                count = if (previous.codeValue == codeValue) previous.count + 1 else 1
            )

            if (consecutiveScans.compareAndSet(previous, current)) {
                if (current.count >= REQUIRED_MATCHES) {
                    delegate.onCodeFound(codeValue, false)
                }
                return
            }
        }
    }

    private data class ConsecutiveScans(
        val codeValue: String? = null,
        val count: Int = 0
    )

    companion object {
        private const val REQUIRED_MATCHES = 3
    }
}
