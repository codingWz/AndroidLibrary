package com.baseuilibrary.baserecycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerViewAdapter<T>(val mRecycler: RecyclerView) : RecyclerView.Adapter<BaseViewHolder>(),
    View.OnClickListener {

    companion object {
        const val ITEM_VIEW_TYPE_HEADER = Int.MIN_VALUE
        const val ITEM_VIEW_TYPE_FOOTER = Int.MAX_VALUE
    }

    var data: List<T>? = null
    var mHeaderViewHolder: BaseViewHolder? = null
    set(value) {
        field = value
        mHeaderViewHolder?.rootView?.setOnClickListener{
            onHeaderClick?.invoke(mHeaderViewHolder!!)
        }
    }
    var mFooterViewHolder: BaseViewHolder? = null
    set(value) {
        field = value
        mFooterViewHolder?.rootView?.setOnClickListener{
            onFooterClick?.invoke(mFooterViewHolder!!)
        }
    }
    var onItemClick: ((holder: BaseViewHolder, position: Int) -> Unit)? = null
    var onHeaderClick: ((holder: BaseViewHolder) -> Unit)? = null
    var onFooterClick: ((holder: BaseViewHolder) -> Unit)? = null

    override fun onClick(v: View) {
        val position = mRecycler.getChildAdapterPosition(v)
        if (mHeaderViewHolder != null && position == 0) {
            onHeaderClick?.invoke(mHeaderViewHolder!!)
        } else if (mFooterViewHolder != null && position == itemCount - 1) {
            onFooterClick?.invoke(mFooterViewHolder!!)
        } else {
            val holder = mRecycler.getChildViewHolder(v) as BaseViewHolder
            onItemClick?.invoke(holder, getContentDataPosition(position))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        if (viewType == ITEM_VIEW_TYPE_HEADER && mHeaderViewHolder != null) {
            return mHeaderViewHolder!!
        }

        if (viewType == ITEM_VIEW_TYPE_FOOTER && mFooterViewHolder != null) {
            return mFooterViewHolder!!
        }

        return onCreateContentViewHolder(parent, viewType)
    }



    override fun getItemCount(): Int {
        val headerCount = if (mHeaderViewHolder == null) 0 else 1
        val footerCount = if (mFooterViewHolder == null) 0 else 1
        val dataCount = data?.size ?: 0
        return dataCount + headerCount + footerCount
    }

    override fun getItemViewType(position: Int): Int {

        if (mHeaderViewHolder != null && position == 0) {
            return ITEM_VIEW_TYPE_HEADER
        }

        if (mFooterViewHolder != null && data != null && position == data!!.size + 1) {
            return ITEM_VIEW_TYPE_FOOTER
        }

        return getContentViewType(getContentDataPosition(position))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val itemType = getItemViewType(position)
        when (itemType) {
            ITEM_VIEW_TYPE_HEADER -> onBindHeader(holder)
            ITEM_VIEW_TYPE_FOOTER -> onBindFooter(holder)
            else -> onBindContent(holder, getContentDataPosition(position))
        }
    }

    /**
     * 获取和mData对应的position
     */
    private fun getContentDataPosition(adapterPosition: Int): Int {
        return if (mHeaderViewHolder == null) adapterPosition else adapterPosition - 1
    }

    private fun onCreateContentViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(getContentViewResId(viewType), parent, false)
        val holder = BaseViewHolder(view)
        view.setOnClickListener(this)
        return holder
    }

    abstract fun onBindFooter(holder: BaseViewHolder)
    abstract fun onBindHeader(holder: BaseViewHolder)
    abstract fun onBindContent(holder: BaseViewHolder, position: Int)

    abstract fun getContentViewType(position: Int): Int
    abstract fun getContentViewResId(viewType: Int): Int
}