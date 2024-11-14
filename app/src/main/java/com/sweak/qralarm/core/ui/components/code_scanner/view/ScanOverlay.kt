package com.sweak.qralarm.core.ui.components.code_scanner.view

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class ScanOverlay  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    companion object {
        const val RATIO = 0.7f
    }

    private val backgroundPaint: Paint

    private val viewFinderPaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val viewFinderCornerPaint: Paint
    private val viewFinderRect: RectF = RectF()

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        viewFinderCornerPaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF5C54A4")
            style = Paint.Style.STROKE
            strokeWidth = 2f * Resources.getSystem().displayMetrics.density
        }

        backgroundPaint = Paint().apply {
            color = Color.parseColor("#770C1445")
            style = Paint.Style.FILL
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        calculateRectangleDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // ViewFinder
        canvas.drawRect(viewFinderRect, viewFinderPaint)

        // Corner
        val cornerLength = 48f * Resources.getSystem().displayMetrics.density

        with(viewFinderRect) {
            // Top-left corner
            canvas.drawLine(left, top, left + cornerLength, top, viewFinderCornerPaint)
            canvas.drawLine(left, top, left, top + cornerLength, viewFinderCornerPaint)

            // Top-right corner
            canvas.drawLine(right, top, right - cornerLength, top, viewFinderCornerPaint)
            canvas.drawLine(right, top, right, top + cornerLength, viewFinderCornerPaint)

            // Bottom-right corner
            canvas.drawLine(right, bottom, right - cornerLength, bottom, viewFinderCornerPaint)
            canvas.drawLine(right, bottom, right, bottom - cornerLength, viewFinderCornerPaint)

            // Bottom-left corner
            canvas.drawLine(left, bottom, left + cornerLength, bottom, viewFinderCornerPaint)
            canvas.drawLine(left, bottom, left, bottom - cornerLength, viewFinderCornerPaint)
        }
    }

    private fun calculateRectangleDimension(width: Int, height: Int) {
        val overlayWidth = width.toFloat()
        val overlayHeight = height.toFloat()

        val viewfinderSize = overlayHeight.coerceAtMost(overlayWidth) * RATIO

        val centerX = overlayWidth / 2f
        val centerY = overlayHeight / 2f

        val left = centerX - viewfinderSize / 2f
        val right = centerX + viewfinderSize / 2f
        val top = centerY - viewfinderSize / 2f
        val bottom = centerY + viewfinderSize / 2f

        viewFinderRect.set(left, top, right, bottom)
    }
}