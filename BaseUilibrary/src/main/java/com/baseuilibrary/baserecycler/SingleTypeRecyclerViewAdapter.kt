package com.baseuilibrary.baserecycler

import androidx.recyclerview.widget.RecyclerView

abstract class SingleTypeRecyclerViewAdapter<T>(recyclerView: RecyclerView)
    : BaseRecyclerViewAdapter<T>(recyclerView) {
    override fun getContentViewType(position: Int) = 0
}