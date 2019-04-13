package com.mylibrary

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baseuilibrary.baserecycler.BaseRecyclerViewAdapter
import com.baseuilibrary.baserecycler.BaseViewHolder
import com.baseuilibrary.baserecycler.SingleLineItemDecoration
import com.baseuilibrary.baserecycler.SingleTypeRecyclerViewAdapter
import com.mylibrary.likeaction.LikeActionActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.recycler_item_single_text.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var mRecyclerAdapter: BaseRecyclerViewAdapter<String>
    private lateinit var mData: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()
        initView()
    }

    private fun initData() {
        mData = listOf(
            MultiTypeRecyclerViewActivity::class.java.simpleName,
            LikeActionActivity::class.java.name
            )
    }

    private fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this,
            RecyclerView.VERTICAL, false)
        initRecyclerAdapter()
        recyclerView.adapter = mRecyclerAdapter
        recyclerView.addItemDecoration(SingleLineItemDecoration(Color.GRAY, 2.0f))
    }

    private fun initRecyclerAdapter() {

        mRecyclerAdapter = object : SingleTypeRecyclerViewAdapter<String>(recyclerView) {
            override fun getContentViewResId(viewType: Int) = R.layout.recycler_item_single_text
            override fun onBindFooter(holder: BaseViewHolder) {
            }

            override fun onBindHeader(holder: BaseViewHolder) {
            }

            override fun onBindContent(holder: BaseViewHolder, position: Int) {
                data?.apply {
                    val textView = holder.getView(R.id.textView) as TextView
                    textView.text = get(position)
                }
            }
        }

        mRecyclerAdapter.onItemClick = { _, position ->
            when (position) {
                0 -> startActivity(Intent(this, MultiTypeRecyclerViewActivity::class.java))
                1 -> startActivity(Intent(this, LikeActionActivity::class.java))
            }
        }

        val headerView = LayoutInflater.from(this)
            .inflate(R.layout.recycler_item_single_text, recyclerView, false)
        headerView.textView.text = "Header view"
        mRecyclerAdapter.mHeaderViewHolder = BaseViewHolder(headerView)
        mRecyclerAdapter.onHeaderClick = {
            Toast.makeText(this, "On Header Click!", Toast.LENGTH_SHORT).show()
        }

        val footerView = LayoutInflater.from(this)
            .inflate(R.layout.recycler_item_single_text, recyclerView, false)
        footerView.textView.text = "Footer view"
        mRecyclerAdapter.mFooterViewHolder = BaseViewHolder(footerView)
        mRecyclerAdapter.onFooterClick = {
            Toast.makeText(this, "On Footer Click!", Toast.LENGTH_SHORT).show()
        }

        mRecyclerAdapter.data = mData
    }
}
