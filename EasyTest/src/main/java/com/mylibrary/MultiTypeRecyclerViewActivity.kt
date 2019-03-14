package com.mylibrary

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baseuilibrary.baserecycler.BaseRecyclerViewAdapter
import com.baseuilibrary.baserecycler.BaseViewHolder
import com.baseuilibrary.baserecycler.SingleLineItemDecoration
import kotlinx.android.synthetic.main.activity_multi_type_recycler.*

class MultiTypeRecyclerViewActivity : AppCompatActivity() {

    private lateinit var mData: List<Int>
    private lateinit var mAdapter: BaseRecyclerViewAdapter<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_type_recycler)
        initData()
        initView()
    }

    private fun initData() {
        mData = listOf(
            R.string.app_name,
            R.mipmap.ic_launcher,
            R.string.app_name,
            R.mipmap.ic_launcher_round,
            R.string.app_name,
            R.mipmap.ic_launcher,
            R.string.app_name,
            R.mipmap.ic_launcher_round,
            R.string.app_name,
            R.mipmap.ic_launcher,
            R.string.app_name,
            R.mipmap.ic_launcher_round
        )
    }

    private fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        mAdapter = object : BaseRecyclerViewAdapter<Int>(recyclerView) {
            override fun onBindFooter(holder: BaseViewHolder) {

            }

            override fun onBindHeader(holder: BaseViewHolder) {
            }

            override fun onBindContent(holder: BaseViewHolder, position: Int) {
                data?.apply {
                    if (position % 2 == 0) {
                        holder.getTextView(R.id.textView).text = getString(get(position))
                    } else {
                        holder.getImageView(R.id.imageView).setImageResource(get(position))
                    }
                }

            }

            override fun getContentViewType(position: Int): Int {
                return if (position % 2 == 0) 0 else 1
            }

            override fun getContentViewResId(viewType: Int): Int {
                return if (viewType == 0) R.layout.recycler_item_single_text else R.layout.recycler_item_single_image
            }
        }
        mAdapter.data = mData

        recyclerView.adapter = mAdapter
        recyclerView.addItemDecoration(SingleLineItemDecoration(Color.BLUE, 2.0f))
    }
}