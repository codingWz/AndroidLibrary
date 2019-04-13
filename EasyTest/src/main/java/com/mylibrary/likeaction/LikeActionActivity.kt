package com.mylibrary.likeaction

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mylibrary.R
import kotlinx.android.synthetic.main.activity_like_action.*

class LikeActionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like_action)
        bindEvent()
    }

    private fun bindEvent() {
        likeActionView.animationFinishedListener = {
            minusBtn.isEnabled = true
            addBtn.isEnabled = true
            //refTv.text = likeActionView.addNum()
        }
        addBtn.setOnClickListener{view ->
            refTv.text = likeActionView.addNum()
            view.isEnabled = false
            minusBtn.isEnabled = false
        }
        minusBtn.setOnClickListener{view ->
            refTv.text = likeActionView.minusNum()
            view.isEnabled = false
            addBtn.isEnabled = false
        }
    }
}