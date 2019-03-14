package com.baseuilibrary.baserecycler

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
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
            c.drawLine(child.left.toFloat(), child.bottom.toFloat() + 16, child.right.toFloat(), child.bottom.toFloat() + 16, paint)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(0, 0, 0, 32)
    }
}