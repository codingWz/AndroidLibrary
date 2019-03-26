package com.commonlibrary

import android.content.Intent
import android.util.SparseArray
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.atomic.AtomicInteger


class ActivityResultManager {
    companion object {
        internal val resultCallbackMap: SparseArray<ActivityResultCallback> = SparseArray()
        private val autoRequestCode: AtomicInteger = AtomicInteger()
        fun startActivityForResult(
            receiver: AppCompatActivity,
            intent: Intent,
            callback: (intent: Intent?) -> Unit
        ): ActivityResultCallback {
            return ActivityResultCallback(callback).also {
                val proxy = CallbackProxyFragment.getInstance(receiver)
                proxy.startActivityForResult(
                    intent,
                    autoRequestCode.getAndIncrement().also { requestCode ->
                        resultCallbackMap.put(requestCode, it)
                    }
                )
            }
        }
    }

    class ActivityResultCallback(@JvmField val onResultOk: (intent: Intent?) -> Unit) {

        internal lateinit var onResultCancel: (intent: Intent?) -> Unit
        internal lateinit var onCustomResult: (intent: Intent?, resultCode: Int) -> Unit

        fun onResultCancel(onResultCancel: (intent: Intent?) -> Unit): ActivityResultCallback {
            this.onResultCancel = onResultCancel
            return this
        }

        fun onCustomResult(onCustomResult: (intent: Intent?, resultCode: Int) -> Unit): ActivityResultCallback {
            this.onCustomResult = onCustomResult
            return this
        }
    }
}