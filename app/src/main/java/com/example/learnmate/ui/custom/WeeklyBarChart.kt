package com.example.learnmate.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class WeeklyBarChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // ── Data ───────────────────────────────────────────────────────────
    var data: List<Int> = List(7) { 0 }
        set(value) { field = value; invalidate() }

    var labels: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        set(value) { field = value; invalidate() }

    var highlightIndex: Int = getCurrentDayIndex()
        set(value) { field = value; invalidate() }

    // ── Paints ─────────────────────────────────────────────────────────
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = Color.parseColor("#9CA3AF")
        textAlign = Paint.Align.CENTER
        textSize  = 28f
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color        = Color.parseColor("#7C3AED")
        textAlign    = Paint.Align.CENTER
        textSize     = 24f
        isFakeBoldText = true
    }

    private val rect = RectF()

    // ── Draw ───────────────────────────────────────────────────────────
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w          = width.toFloat()
        val h          = height.toFloat()
        val count      = data.size
        val barWidth   = w / (count * 2f)
        val maxVal     = data.maxOrNull()?.takeIf { it > 0 } ?: 1
        val chartH     = h - 80f   // Leave space for labels at bottom
        val topPadding = 40f

        data.forEachIndexed { i, value ->
            val cx      = (i * 2 + 1) * barWidth
            val barH    = ((value.toFloat() / maxVal) * (chartH - topPadding))
                .coerceAtLeast(if (value > 0) 8f else 4f)
            val left    = cx - barWidth * 0.6f
            val right   = cx + barWidth * 0.6f
            val top     = chartH - barH
            val bottom  = chartH

            rect.set(left, top, right, bottom)

            // Gradient fill for highlighted bar, solid for others
            barPaint.shader = if (i == highlightIndex) {
                LinearGradient(
                    left, top, left, bottom,
                    Color.parseColor("#7C3AED"),
                    Color.parseColor("#2563EB"),
                    Shader.TileMode.CLAMP
                )
            } else {
                LinearGradient(
                    left, top, left, bottom,
                    Color.parseColor("#EDE9FE"),
                    Color.parseColor("#DDD6FE"),
                    Shader.TileMode.CLAMP
                )
            }

            canvas.drawRoundRect(rect, 12f, 12f, barPaint)

            // Value label above bar (only if > 0)
            if (value > 0) {
                val label = if (value >= 60) "${value / 60}h" else "${value}m"
                valuePaint.color = if (i == highlightIndex)
                    Color.parseColor("#7C3AED") else Color.parseColor("#9CA3AF")
                canvas.drawText(label, cx, top - 8f, valuePaint)
            }

            // Day label below bar
            labelPaint.color = if (i == highlightIndex)
                Color.parseColor("#7C3AED") else Color.parseColor("#9CA3AF")
            labelPaint.isFakeBoldText = i == highlightIndex
            canvas.drawText(labels[i], cx, h - 8f, labelPaint)
        }
    }

    private fun getCurrentDayIndex(): Int {
        val cal = java.util.Calendar.getInstance()
        return (cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
    }
}
