package com.baseuilibrary.baserecycler

import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView

class BaseViewHolder(val rootView: View) : RecyclerView.ViewHolder(rootView) {

    private val mViews: SparseArray<View> = SparseArray()

    fun getView(@IdRes resId: Int): View {
        var view = mViews.get(resId)
        if (view == null) {
            view = rootView.findViewById(resId)
            if (view == null) {
                throw IllegalArgumentException("resource id not found in $rootView !")
            }
        }
        return view
    }

    fun getTextView(@IdRes resId: Int) = getView(resId) as TextView
    fun getImageView(@IdRes resId: Int) = getView(resId) as ImageView

}