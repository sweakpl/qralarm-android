package com.sweak.qralarm.core.recognition.custom

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * What is kept from one reference photo: ORB keypoints and descriptors from inside the guide box,
 * and the image embedding of that crop. Plain arrays so it can be stored and tested without OpenCV.
 */
class ReferenceFeatures(
    val width: Int,
    val height: Int,
    /** Interleaved x, y keypoint coordinates in crop pixels. */
    val keypoints: FloatArray,
    /** [DESCRIPTOR_BYTES] bytes per keypoint, in the same order as [keypoints]. */
    val descriptors: ByteArray,
    /** L2-normalised; empty when no embedder was available. */
    val embedding: FloatArray,
    /** Small grayscale copy of the crop for the photometric check; row-major, may be empty. */
    val thumbnailWidth: Int = 0,
    val thumbnailHeight: Int = 0,
    val thumbnail: ByteArray = ByteArray(0)
) {
    val keypointCount: Int get() = keypoints.size / 2

    init {
        require(keypoints.size % 2 == 0)
        require(descriptors.size == keypointCount * DESCRIPTOR_BYTES)
        require(thumbnail.size == thumbnailWidth * thumbnailHeight)
    }

    fun writeTo(output: OutputStream) {
        DataOutputStream(output.buffered()).run {
            writeInt(MAGIC)
            writeInt(FORMAT_VERSION)
            writeInt(width)
            writeInt(height)
            writeInt(keypointCount)
            keypoints.forEach(::writeFloat)
            write(descriptors)
            writeInt(embedding.size)
            embedding.forEach(::writeFloat)
            writeInt(thumbnailWidth)
            writeInt(thumbnailHeight)
            write(thumbnail)
            flush()
        }
    }

    companion object {
        const val DESCRIPTOR_BYTES = 32
        const val FORMAT_VERSION = 1
        private const val MAGIC = 0x51524F46 // "QROF"

        /** Returns null for files written by another format version, which are then recomputed. */
        fun readFrom(input: InputStream): ReferenceFeatures? = DataInputStream(input.buffered()).run {
            if (readInt() != MAGIC || readInt() != FORMAT_VERSION) return null
            val width = readInt()
            val height = readInt()
            val count = readInt()
            require(count in 0..100_000)
            val keypoints = FloatArray(count * 2) { readFloat() }
            val descriptors = ByteArray(count * DESCRIPTOR_BYTES).also(::readFully)
            val embeddingSize = readInt()
            require(embeddingSize in 0..10_000)
            val embedding = FloatArray(embeddingSize) { readFloat() }
            val thumbnailWidth = readInt()
            val thumbnailHeight = readInt()
            require(thumbnailWidth in 0..4096 && thumbnailHeight in 0..4096)
            val thumbnail = ByteArray(thumbnailWidth * thumbnailHeight).also(::readFully)
            ReferenceFeatures(
                width, height, keypoints, descriptors, embedding,
                thumbnailWidth, thumbnailHeight, thumbnail
            )
        }
    }
}
