package com.example.learnmate.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // ── Configurable properties ────────────────────────────────────────
    var progress: Int = 0
        set(value) {
            field = value.coerceIn(0, 100)
            invalidate()
        }

    var trackColor: Int = Color.parseColor("#EDE9FE")
        set(value) { field = value; invalidate() }

    var progressColor: Int = Color.parseColor("#7C3AED")
        set(value) { field = value; invalidate() }

    var progressColorEnd: Int = Color.parseColor("#2563EB")
        set(value) { field = value; invalidate() }

    var strokeWidth: Float = 18f
        set(value) { field = value; invalidate() }

    var centerText: String = ""
        set(value) { field = value; invalidate() }

    var centerSubText: String = ""
        set(value) { field = value; invalidate() }

    // ── Paints ─────────────────────────────────────────────────────────
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1F1F2E")
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9CA3AF")
        textAlign = Paint.Align.CENTER
    }

    private val oval = RectF()

    // ── Draw ───────────────────────────────────────────────────────────
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val stroke = strokeWidth
        val radius = (minOf(w, h) / 2f) - stroke

        val cx = w / 2f
        val cy = h / 2f

        oval.set(cx - radius, cy - radius, cx + radius, cy + radius)

        // Track (background circle)
        trackPaint.color = trackColor
        trackPaint.strokeWidth = stroke
        canvas.drawArc(oval, -90f, 360f, false, trackPaint)

        // Progress arc
        val sweepAngle = 360f * progress / 100f
        progressPaint.strokeWidth = stroke
        progressPaint.shader = SweepGradient(
            cx, cy,
            intArrayOf(progressColor, progressColorEnd, progressColor),
            floatArrayOf(0f, sweepAngle / 360f, 1f)
        )
        canvas.rotate(-90f, cx, cy)
        canvas.drawArc(oval, 0f, sweepAngle, false, progressPaint)
        canvas.rotate(90f, cx, cy)

        // Center main text
        if (centerText.isNotEmpty()) {
            textPaint.textSize = minOf(w, h) * 0.18f
            canvas.drawText(centerText, cx, cy + textPaint.textSize * 0.35f, textPaint)
        }

        // Center sub text
        if (centerSubText.isNotEmpty()) {
            subTextPaint.textSize = minOf(w, h) * 0.11f
            canvas.drawText(
                centerSubText, cx,
                cy + textPaint.textSize * 0.35f + subTextPaint.textSize * 1.4f,
                subTextPaint
            )
        }
    }
}