package com.commonlibrary

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicInteger


class PermissionManager {

    companion object {

        private val autoRequestCode: AtomicInteger = AtomicInteger()
        internal val callbackMap: HashMap<Int, RequestResultCallback> = HashMap()

        fun requestPermissions(
            receiver: AppCompatActivity,
            permissions: Array<String>,
            onGranted: (permissions: ArrayList<String>) -> Unit
        ): RequestResultCallback {
            return RequestResultCallback(onGranted).also {
                val unGrantedPermissions = ArrayList<String>()
                for (permission in permissions) {
                    if (ContextCompat.checkSelfPermission(receiver, permission) != PackageManager.PERMISSION_GRANTED) {
                        unGrantedPermissions.add(permission)
                    }
                }

                if (!unGrantedPermissions.isEmpty()) {
                    val tempArray = arrayOfNulls<String>(unGrantedPermissions.size)
                    unGrantedPermissions.toArray(tempArray)
                    val proxy = CallbackProxyFragment.getInstance(receiver)
                    proxy.requestPermissions(tempArray, autoRequestCode.getAndIncrement().also {requestCode ->
                        callbackMap[requestCode] = it
                    })
                }

            }
        }

    }

    class RequestResultCallback(@JvmField val onGranted: (permissions: ArrayList<String>) -> Unit) {

        internal lateinit var onDenied: (permissions: ArrayList<String>) -> Unit
        internal lateinit var onCancel: () -> Unit

        fun onDenied(onDenied: (permissions: ArrayList<String>) -> Unit): RequestResultCallback {
            this.onDenied = onDenied
            return this
        }

        fun onCancel(onCancel: () -> Unit): RequestResultCallback {
            this.onCancel = onCancel
            return this
        }

    }

}