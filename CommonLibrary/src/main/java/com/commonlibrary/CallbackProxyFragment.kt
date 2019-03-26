package com.commonlibrary

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


class CallbackProxyFragment: Fragment() {

    companion object {

        private val TAG: String? = CallbackProxyFragment::class.java.canonicalName

        fun getInstance(context: AppCompatActivity): Fragment {
            var fragment = context.supportFragmentManager.findFragmentByTag(CallbackProxyFragment.TAG)
            if (fragment == null) {
                fragment = CallbackProxyFragment()
                context.supportFragmentManager.beginTransaction().apply {
                    add(fragment, CallbackProxyFragment.TAG)
                    commitNow()
                }
            }
            return fragment
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) {
            PermissionManager.callbackMap[requestCode]?.onCancel?.invoke()
        } else {
            val deniedList = ArrayList<String>()
            val grantedList = ArrayList<String>()

            for ((index, grantResult) in grantResults.withIndex()) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    grantedList.add(permissions[index])
                } else {
                    deniedList.add(permissions[index])
                }
            }

            if (!grantedList.isEmpty()) {
                PermissionManager.callbackMap[requestCode]?.onGranted?.invoke(grantedList)
            }

            if (!deniedList.isEmpty()) {
                PermissionManager.callbackMap[requestCode]?.onDenied?.invoke(deniedList)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        ActivityResultManager.resultCallbackMap[requestCode].also {
            when (resultCode) {
                Activity.RESULT_OK -> it.onResultOk.invoke(data)
                Activity.RESULT_CANCELED -> it.onResultCancel.invoke(data)
                else -> it.onCustomResult.invoke(data, resultCode)
            }
        }

    }
}