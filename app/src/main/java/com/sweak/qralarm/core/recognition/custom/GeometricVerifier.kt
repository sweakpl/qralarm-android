package com.sweak.qralarm.core.recognition.custom

import org.opencv.calib3d.Calib3d
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDMatch
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.features2d.DescriptorMatcher
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Decides whether a frame shows the same physical item as one of the reference photos: descriptor
 * matches must agree on a single plausible perspective transform (a homography found with RANSAC).
 * A different product of the same shape fails here because its printed details do not line up.
 */
class GeometricVerifier(
    private val references: List<ReferenceFeatures>,
    private val params: Params = Params()
) : AutoCloseable {

    /**
     * Defaults were chosen on real, labelled photos (eval/RESULTS.md): the strictest setting that
     * still lets most true views through, with zero false dismissals on every test set measured.
     */
    data class Params(
        val ratio: Double = 0.75,
        val reprojectionThreshold: Double = 5.0,
        val minGoodMatches: Int = 12,
        val minInliers: Int = 15,
        val minInlierRatio: Double = 0.0,
        /** Inliers must cover this share of the reference's keypoint area, so one logo is not enough. */
        val minInlierSpread: Double = 0.25,
        /** Inliers as a share of the reference's keypoints. */
        val minCoverage: Double = 0.0,
        val minScale: Double = 0.03,
        val maxScale: Double = 30.0,
        /** Share of textured reference cells that must line up; see [PhotometricCheck]. */
        val minPhotometricScore: Double = 0.4
    )

    data class Match(
        val referenceIndex: Int,
        val inliers: Int,
        val goodMatches: Int,
        /** Projected reference corners in frame pixels: x0, y0, ... x3, y3. */
        val corners: FloatArray,
        /** Null when no frame image or no reference thumbnail was available. */
        val photometricScore: Double?,
        /** Area of the inliers' hull as a share of the reference keypoints' hull. */
        val inlierSpread: Double = 0.0,
        /** Inliers as a share of the reference's keypoints. */
        val coverage: Double = 0.0
    )

    private val referenceDescriptors: List<Mat> = references.map {
        KeypointFeatures(it.keypoints, it.descriptors).descriptorMat()
    }

    /** One trained index per reference, built once: frames are matched against it many times. */
    private val matchers: List<DescriptorMatcher?> = referenceDescriptors.map { descriptors ->
        if (descriptors.rows() < params.minGoodMatches) null
        else DescriptorMatcher.create(DescriptorMatcher.FLANNBASED).apply {
            add(listOf(descriptors))
            train()
        }
    }

    /**
     * The best passing reference, or null when none passes. [frameGray] enables the photometric
     * check; without it only the keypoint geometry is tested.
     */
    fun verify(frame: KeypointFeatures, frameWidth: Int, frameHeight: Int, frameGray: Mat? = null): Match? =
        candidates(frame, frameWidth, frameHeight, frameGray)
            .filter { (it.photometricScore ?: 1.0) >= params.minPhotometricScore }
            .maxByOrNull { it.inliers }

    /** Every reference that passes the geometric checks, with its photometric score, unfiltered. */
    fun candidates(frame: KeypointFeatures, frameWidth: Int, frameHeight: Int, frameGray: Mat?): List<Match> {
        if (frame.count < params.minGoodMatches) return emptyList()
        val frameDescriptors = frame.descriptorMat()
        try {
            return references.indices
                .mapNotNull { verifyAgainst(it, frame, frameDescriptors, frameWidth, frameHeight, frameGray) }
        } finally {
            frameDescriptors.release()
        }
    }

    /** Diagnostic: inlier count of the best reference whether or not it passes. */
    fun bestInliers(frame: KeypointFeatures): Int {
        if (frame.count < params.minGoodMatches) return 0
        val frameDescriptors = frame.descriptorMat()
        try {
            return references.indices.maxOfOrNull { index ->
                homography(index, frame, frameDescriptors)?.inliers ?: 0
            } ?: 0
        } finally {
            frameDescriptors.release()
        }
    }

    private class Homography(val matrix: Mat, val inliers: Int, val good: Int, val inlierRefPoints: List<Point>)

    private fun homography(index: Int, frame: KeypointFeatures, frameDescriptors: Mat): Homography? {
        val reference = references[index]
        if (reference.keypointCount < params.minGoodMatches) return null
        val knn = mutableListOf<MatOfDMatch>()
        val matcher = matchers[index] ?: return null
        matcher.knnMatch(frameDescriptors, knn, 2)
        // Ratio test, then keep a single frame match per reference keypoint.
        val best = HashMap<Int, org.opencv.core.DMatch>()
        knn.forEach { pair ->
            val matches = pair.toArray()
            pair.release()
            if (matches.size == 2 && matches[0].distance < params.ratio * matches[1].distance) {
                val match = matches[0]
                val previous = best[match.trainIdx]
                if (previous == null || match.distance < previous.distance) best[match.trainIdx] = match
            }
        }
        if (best.size < params.minGoodMatches) return null
        val good = best.values.toList()
        val referencePoints = good.map {
            Point(reference.keypoints[it.trainIdx * 2].toDouble(), reference.keypoints[it.trainIdx * 2 + 1].toDouble())
        }
        val framePoints = good.map {
            Point(frame.keypoints[it.queryIdx * 2].toDouble(), frame.keypoints[it.queryIdx * 2 + 1].toDouble())
        }
        val source = MatOfPoint2f(*referencePoints.toTypedArray())
        val destination = MatOfPoint2f(*framePoints.toTypedArray())
        val mask = Mat()
        try {
            val matrix = Calib3d.findHomography(
                source, destination, Calib3d.USAC_MAGSAC, params.reprojectionThreshold, mask, 5000, 0.999
            )
            if (matrix.empty()) return null
            val inlierFlags = ByteArray(good.size).also { mask.get(0, 0, it) }
            val inlierPoints = referencePoints.filterIndexed { i, _ -> inlierFlags[i].toInt() != 0 }
            return Homography(matrix, inlierPoints.size, good.size, inlierPoints)
        } finally {
            source.release()
            destination.release()
            mask.release()
        }
    }

    private fun verifyAgainst(
        index: Int,
        frame: KeypointFeatures,
        frameDescriptors: Mat,
        frameWidth: Int,
        frameHeight: Int,
        frameGray: Mat?
    ): Match? {
        val result = homography(index, frame, frameDescriptors) ?: return null
        try {
            if (result.inliers < params.minInliers) return null
            if (result.inliers.toDouble() / result.good < params.minInlierRatio) return null
            val reference = references[index]
            val inlierSpread = spread(result.inlierRefPoints) / keypointArea(reference).coerceAtLeast(1.0)
            if (inlierSpread < params.minInlierSpread) return null
            if (result.inliers.toDouble() / reference.keypointCount < params.minCoverage) return null
            val corners = projectCorners(result.matrix, reference.width, reference.height) ?: return null
            if (!isPlausible(corners, reference, frameWidth, frameHeight)) return null
            val photometric = frameGray?.let { PhotometricCheck.score(reference, result.matrix, it) }
            return Match(
                index, result.inliers, result.good, corners, photometric,
                inlierSpread, result.inliers.toDouble() / reference.keypointCount
            )
        } finally {
            result.matrix.release()
        }
    }

    private fun projectCorners(matrix: Mat, width: Int, height: Int): FloatArray? {
        val h = DoubleArray(9)
        val m = Mat()
        matrix.convertTo(m, CvType.CV_64F)
        m.get(0, 0, h)
        m.release()
        val input = listOf(0.0 to 0.0, width.toDouble() to 0.0, width.toDouble() to height.toDouble(), 0.0 to height.toDouble())
        val out = FloatArray(8)
        input.forEachIndexed { i, (x, y) ->
            val w = h[6] * x + h[7] * y + h[8]
            // A corner behind the camera means the transform folded the image over itself.
            if (w <= 1e-6) return null
            out[i * 2] = ((h[0] * x + h[1] * y + h[2]) / w).toFloat()
            out[i * 2 + 1] = ((h[3] * x + h[4] * y + h[5]) / w).toFloat()
        }
        return out
    }

    private fun isPlausible(c: FloatArray, reference: ReferenceFeatures, frameWidth: Int, frameHeight: Int): Boolean {
        // Convex and not mirrored: every turn of the quad goes the same way as the original.
        var sign = 0
        for (i in 0 until 4) {
            val ax = c[((i + 1) % 4) * 2] - c[i * 2]
            val ay = c[((i + 1) % 4) * 2 + 1] - c[i * 2 + 1]
            val bx = c[((i + 2) % 4) * 2] - c[((i + 1) % 4) * 2]
            val by = c[((i + 2) % 4) * 2 + 1] - c[((i + 1) % 4) * 2 + 1]
            val cross = ax * by - ay * bx
            val s = if (cross > 0) 1 else if (cross < 0) -1 else 0
            if (s == 0 || (sign != 0 && s != sign)) return false
            sign = s
        }
        if (sign < 0) return false
        val area = abs(polygonArea(c))
        val scale = area / (reference.width.toDouble() * reference.height)
        if (scale < params.minScale || scale > params.maxScale) return false
        // Opposite sides should not differ wildly in length (extreme perspective is a RANSAC artefact).
        val sides = (0 until 4).map { i ->
            val dx = c[((i + 1) % 4) * 2] - c[i * 2]
            val dy = c[((i + 1) % 4) * 2 + 1] - c[i * 2 + 1]
            kotlin.math.sqrt((dx * dx + dy * dy).toDouble())
        }
        if (max(sides[0], sides[2]) > 4 * min(sides[0], sides[2])) return false
        if (max(sides[1], sides[3]) > 4 * min(sides[1], sides[3])) return false
        // Must overlap the frame.
        val minX = (0 until 4).minOf { c[it * 2] }
        val maxX = (0 until 4).maxOf { c[it * 2] }
        val minY = (0 until 4).minOf { c[it * 2 + 1] }
        val maxY = (0 until 4).maxOf { c[it * 2 + 1] }
        return maxX > 0 && maxY > 0 && minX < frameWidth && minY < frameHeight
    }

    private fun polygonArea(c: FloatArray): Double {
        var sum = 0.0
        for (i in 0 until 4) {
            val j = (i + 1) % 4
            sum += c[i * 2].toDouble() * c[j * 2 + 1] - c[j * 2].toDouble() * c[i * 2 + 1]
        }
        return sum / 2
    }

    private fun spread(points: List<Point>): Double {
        if (points.size < 3) return 0.0
        val hull = org.opencv.core.MatOfPoint(*points.map { org.opencv.core.Point(it.x, it.y) }.toTypedArray())
        val hullIndices = org.opencv.core.MatOfInt()
        try {
            org.opencv.imgproc.Imgproc.convexHull(hull, hullIndices)
            val hullPoints = hullIndices.toArray().map { points[it] }
            var sum = 0.0
            for (i in hullPoints.indices) {
                val a = hullPoints[i]
                val b = hullPoints[(i + 1) % hullPoints.size]
                sum += a.x * b.y - b.x * a.y
            }
            return abs(sum) / 2
        } finally {
            hull.release()
            hullIndices.release()
        }
    }

    private val areaCache = HashMap<ReferenceFeatures, Double>()

    private fun keypointArea(reference: ReferenceFeatures): Double = areaCache.getOrPut(reference) {
        spread((0 until reference.keypointCount).map {
            Point(reference.keypoints[it * 2].toDouble(), reference.keypoints[it * 2 + 1].toDouble())
        })
    }

    override fun close() {
        matchers.forEach { it?.clear() }
        referenceDescriptors.forEach { it.release() }
    }
}
