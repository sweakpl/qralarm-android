package com.sweak.qralarm.core.domain.recognition

/** How many confident frames, over how long, confirm an object. */
data class ConfirmationPolicy(
    val minConfidence: Float,
    val requiredMatches: Int,
    val minSpanMillis: Long,
    val maxFrameGapMillis: Long,
    /**
     * Confirmation needs [requiredMatches] confident frames among the last [windowFrames]
     * frames; equal to [requiredMatches] means consecutive frames.
     */
    val windowFrames: Int = requiredMatches
)

/** Receives only distinct, processed frames, using monotonic camera timestamps. */
class ObjectConfirmation(private val policy: ConfirmationPolicy = COCO_POLICY) {
    private var lastFrameMillis: Long? = null
    /** Timestamps of the recent frames, null for frames that did not match. */
    private val window = ArrayDeque<Long?>()
    private var completed = false

    private val matches: Int get() = window.count { it != null }

    val progress: Float get() = (matches.toFloat() / policy.requiredMatches).coerceAtMost(1f)

    fun observe(confidence: Float?, timestampMillis: Long): Boolean {
        if (completed) return false
        val previous = lastFrameMillis
        if (previous != null && timestampMillis <= previous) return false
        if (previous != null && timestampMillis - previous > policy.maxFrameGapMillis) reset()
        lastFrameMillis = timestampMillis
        val matched = confidence != null && confidence.isFinite() && confidence >= policy.minConfidence
        if (!matched && policy.windowFrames == policy.requiredMatches) {
            // Consecutive rule: a miss starts over.
            window.clear()
            return false
        }
        window.addLast(if (matched) timestampMillis else null)
        // A consecutive run keeps growing so its span counts from its first frame.
        if (policy.windowFrames > policy.requiredMatches) {
            while (window.size > policy.windowFrames) window.removeFirst()
        }
        // A window may not start with a miss: it would only shorten the span that counts.
        while (window.firstOrNull() == null && window.isNotEmpty()) window.removeFirst()
        if (!matched) return false
        val first = window.first() ?: return false
        if (matches >= policy.requiredMatches && timestampMillis - first >= policy.minSpanMillis) {
            completed = true
            return true
        }
        return false
    }

    fun reset() {
        window.clear()
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
