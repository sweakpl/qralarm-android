package com.sweak.qralarm.core.recognition.custom

import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.max
import kotlin.math.roundToInt

/** The image handling shared by enrollment, live recognition and the offline evaluation. */
object ReferenceBuilder {
    const val REFERENCE_MAX_SIDE = 480
    const val FRAME_MAX_SIDE = 640

    /** The guide box: a centred square, [EnrollmentRules.GUIDE_BOX_FRACTION] of the short side. */
    fun guideBox(width: Int, height: Int): Rect {
        val side = (minOf(width, height) * EnrollmentRules.GUIDE_BOX_FRACTION).roundToInt()
        return Rect((width - side) / 2, (height - side) / 2, side, side)
    }

    /** Crops [gray] to the guide box, so only the object - never the room - becomes the reference. */
    fun cropToGuideBox(gray: Mat): Mat {
        val box = guideBox(gray.cols(), gray.rows())
        return resizeToMaxSide(gray.submat(box), REFERENCE_MAX_SIDE)
    }

    fun resizeToMaxSide(image: Mat, maxSide: Int): Mat {
        val longest = max(image.cols(), image.rows())
        val result = Mat()
        if (longest <= maxSide) {
            image.copyTo(result)
        } else {
            val scale = maxSide.toDouble() / longest
            Imgproc.resize(
                image, result,
                Size((image.cols() * scale).roundToInt().toDouble(), (image.rows() * scale).roundToInt().toDouble()),
                0.0, 0.0, Imgproc.INTER_AREA
            )
        }
        return result
    }

    class Built(val features: ReferenceFeatures, val sharpness: Double)

    /** [crop] is the already cropped single-channel reference image. */
    fun build(crop: Mat, extractor: OrbExtractor, embedding: FloatArray?): Built {
        val orb = extractor.extract(crop)
        val thumbnail = resizeToMaxSide(crop, PhotometricCheck.THUMBNAIL_MAX_SIDE)
        val thumbnailBytes = ByteArray(thumbnail.cols() * thumbnail.rows()).also { thumbnail.get(0, 0, it) }
        val thumbnailWidth = thumbnail.cols()
        val thumbnailHeight = thumbnail.rows()
        thumbnail.release()
        return Built(
            ReferenceFeatures(
                width = crop.cols(),
                height = crop.rows(),
                keypoints = orb.keypoints,
                descriptors = orb.descriptors,
                embedding = embedding?.let(CustomObjectRecognizer::normalize) ?: FloatArray(0),
                thumbnailWidth = thumbnailWidth,
                thumbnailHeight = thumbnailHeight,
                thumbnail = thumbnailBytes
            ),
            EnrollmentRules.sharpness(crop)
        )
    }
}
