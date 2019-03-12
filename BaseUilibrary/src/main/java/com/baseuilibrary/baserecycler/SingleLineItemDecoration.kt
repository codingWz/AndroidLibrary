package com.baseuilibrary.baserecycler

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView

class SingleLineItemDecoration(
    @ColorInt private val decorationColor: Int,
    private val decorationHeight: Float
    ) : RecyclerView.ItemDecoration() {
    
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val paint = Paint()
        paint.strokeWidth = decorationHeight
        paint.color = decorationColor
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            c.drawLine(0.0f, child.bottom.toFloat(), child.measuredWidth.toFloat(), child.bottom.toFloat(), paint)
        }
    }
}