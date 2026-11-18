package com.sweak.qralarm.core.domain.recognition

/** How many confident frames, over how long, confirm an object. */
data class ConfirmationPolicy(
    val minConfidence: Float,
    val requiredMatches: Int,
    val minSpanMillis: Long,
    val maxFrameGapMillis: Long
)

/** Receives only distinct, processed frames, using monotonic camera timestamps. */
class ObjectConfirmation(private val policy: ConfirmationPolicy = COCO_POLICY) {
    private var firstMatchMillis = 0L
    private var lastFrameMillis: Long? = null
    private var matches = 0
    private var completed = false

    val progress: Float get() = (matches.toFloat() / policy.requiredMatches).coerceAtMost(1f)

    fun observe(confidence: Float?, timestampMillis: Long): Boolean {
        if (completed) return false
        val previous = lastFrameMillis
        if (previous != null && timestampMillis <= previous) return false
        if (previous != null && timestampMillis - previous > policy.maxFrameGapMillis) reset()
        lastFrameMillis = timestampMillis
        if (confidence == null || !confidence.isFinite() || confidence < policy.minConfidence) {
            matches = 0
            return false
        }
        if (matches == 0) firstMatchMillis = timestampMillis
        matches++
        if (matches >= policy.requiredMatches && timestampMillis - firstMatchMillis >= policy.minSpanMillis) {
            completed = true
            return true
        }
        return false
    }

    fun reset() {
        matches = 0
        lastFrameMillis = null
        completed = false
    }

    companion object {
        const val MIN_CONFIDENCE = 0.60f
        const val REQUIRED_MATCHES = 3
        const val MIN_SPAN_MILLIS = 150L
        const val MAX_FRAME_GAP_MILLIS = 500L

        val COCO_POLICY = ConfirmationPolicy(
            MIN_CONFIDENCE, REQUIRED_MATCHES, MIN_SPAN_MILLIS, MAX_FRAME_GAP_MILLIS
        )

        /** Custom objects pass a per-frame geometric check, so confidence is 1 or absent. */
        val CUSTOM_OBJECT_POLICY = ConfirmationPolicy(
            minConfidence = 0.5f, requiredMatches = 4, minSpanMillis = 400L, maxFrameGapMillis = 700L
        )
    }
}
