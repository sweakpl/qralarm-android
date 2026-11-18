package com.sweak.qralarm.core.recognition.custom

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/**
 * After the keypoints agree on a homography, the frame is warped onto the reference and compared
 * cell by cell with normalised cross-correlation. Products that share a brand's design but differ
 * in their printed details (flavour, strength, a pharmacy label) match on keypoints from the logo
 * yet fail here, because the rest of the surface does not line up.
 */
object PhotometricCheck {
    const val THUMBNAIL_MAX_SIDE = 192
    const val GRID = 6
    /** Cells with less texture than this in the reference carry no evidence either way. */
    const val MIN_CELL_STD = 10.0
    const val CELL_MATCH_NCC = 0.45

    /**
     * Share of textured reference cells that correlate with the warped frame, or null when the
     * reference has no thumbnail. [homography] maps reference crop pixels to frame pixels.
     */
    fun score(reference: ReferenceFeatures, homography: Mat, frameGray: Mat): Double? {
        if (reference.thumbnail.isEmpty()) return null
        val width = reference.thumbnailWidth
        val height = reference.thumbnailHeight
        val thumb = Mat(height, width, CvType.CV_8UC1).apply { put(0, 0, reference.thumbnail) }
        val scale = Mat.eye(3, 3, CvType.CV_64F).apply {
            put(0, 0, reference.width.toDouble() / width)
            put(1, 1, reference.height.toDouble() / height)
        }
        val h64 = Mat().also { homography.convertTo(it, CvType.CV_64F) }
        val thumbToFrame = Mat()
        Core.gemm(h64, scale, 1.0, Mat(), 0.0, thumbToFrame)
        val warped = Mat()
        val valid = Mat()
        val ones = Mat(frameGray.rows(), frameGray.cols(), CvType.CV_8UC1).apply { setTo(org.opencv.core.Scalar(255.0)) }
        try {
            val size = Size(width.toDouble(), height.toDouble())
            Imgproc.warpPerspective(frameGray, warped, thumbToFrame, size, Imgproc.INTER_LINEAR or Imgproc.WARP_INVERSE_MAP)
            Imgproc.warpPerspective(ones, valid, thumbToFrame, size, Imgproc.INTER_NEAREST or Imgproc.WARP_INVERSE_MAP)
            Imgproc.GaussianBlur(warped, warped, Size(3.0, 3.0), 0.0)
            Imgproc.GaussianBlur(thumb, thumb, Size(3.0, 3.0), 0.0)
            var textured = 0
            var matched = 0
            val cellW = width / GRID
            val cellH = height / GRID
            for (gy in 0 until GRID) for (gx in 0 until GRID) {
                val cell = Rect(gx * cellW, gy * cellH, cellW, cellH)
                val refCell = thumb.submat(cell)
                val refStd = std(refCell)
                if (refStd < MIN_CELL_STD) continue
                textured++
                // A cell the frame does not cover counts against the match.
                if (Core.countNonZero(valid.submat(cell)) < cell.area() * 0.9) continue
                if (ncc(refCell, warped.submat(cell)) >= CELL_MATCH_NCC) matched++
            }
            return if (textured == 0) 0.0 else matched.toDouble() / textured
        } finally {
            thumb.release(); scale.release(); h64.release(); thumbToFrame.release()
            warped.release(); valid.release(); ones.release()
        }
    }

    private fun std(image: Mat): Double {
        val mean = MatOfDouble()
        val deviation = MatOfDouble()
        Core.meanStdDev(image, mean, deviation)
        return deviation.toArray()[0].also { mean.release(); deviation.release() }
    }

    private fun ncc(a: Mat, b: Mat): Double {
        val result = Mat()
        Imgproc.matchTemplate(b, a, result, Imgproc.TM_CCOEFF_NORMED)
        return result.get(0, 0)[0].also { result.release() }
    }
}
