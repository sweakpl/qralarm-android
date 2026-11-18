package com.sweak.qralarm.core.recognition.custom

import org.opencv.core.Mat
import kotlin.math.sqrt

/**
 * Per-frame decision for one custom object. The embedding gate is a cheap first look at overall
 * appearance; the geometric check is what has to pass for a frame to count.
 */
class CustomObjectRecognizer(
    private val references: List<ReferenceFeatures>,
    private val embeddingThreshold: Float,
    params: GeometricVerifier.Params = GeometricVerifier.Params()
) : AutoCloseable {

    data class FrameResult(
        val verified: Boolean,
        val match: GeometricVerifier.Match?,
        val embeddingSimilarity: Float?
    )

    private val verifier = GeometricVerifier(references, params)

    /** [frameEmbeddings] are embeddings of the frame and of crops of it; null skips the gate. */
    fun evaluate(
        frame: OrbFeatures,
        frameWidth: Int,
        frameHeight: Int,
        frameGray: Mat? = null,
        frameEmbeddings: List<FloatArray>? = null
    ): FrameResult {
        val similarity = frameEmbeddings?.let { bestSimilarity(references, it) }
        if (similarity != null && similarity < embeddingThreshold) {
            return FrameResult(verified = false, match = null, embeddingSimilarity = similarity)
        }
        val match = verifier.verify(frame, frameWidth, frameHeight, frameGray)
        return FrameResult(verified = match != null, match = match, embeddingSimilarity = similarity)
    }

    override fun close() = verifier.close()

    companion object {
        fun bestSimilarity(references: List<ReferenceFeatures>, frameEmbeddings: List<FloatArray>): Float? {
            var best: Float? = null
            references.forEach { reference ->
                if (reference.embedding.isEmpty()) return@forEach
                frameEmbeddings.forEach { embedding ->
                    if (embedding.size == reference.embedding.size) {
                        val similarity = cosine(reference.embedding, embedding)
                        if (best == null || similarity > best!!) best = similarity
                    }
                }
            }
            return best
        }

        fun cosine(a: FloatArray, b: FloatArray): Float {
            var dot = 0.0
            var normA = 0.0
            var normB = 0.0
            for (i in a.indices) {
                dot += a[i] * b[i]
                normA += a[i] * a[i]
                normB += b[i] * b[i]
            }
            if (normA == 0.0 || normB == 0.0) return 0f
            return (dot / (sqrt(normA) * sqrt(normB))).toFloat()
        }

        fun normalize(vector: FloatArray): FloatArray {
            val norm = sqrt(vector.fold(0.0) { sum, v -> sum + v * v })
            return if (norm == 0.0) vector else FloatArray(vector.size) { (vector[it] / norm).toFloat() }
        }
    }
}
