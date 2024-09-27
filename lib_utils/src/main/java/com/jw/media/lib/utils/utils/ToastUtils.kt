package com.jw.media.lib.utils.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import androidx.annotation.StringRes
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import me.drakeet.support.toast.ToastCompat

/**
 *Created by Joyce.wang on 2024/9/27 15:25
 *@Description TODO
 */
class ToastUtils {
    companion object {
        private var toast: Toast? = null

        /**
         * 显示土司
         */
        fun showToast(context: Context, text: String, image: Int) {
            showToastOnMainThread(context, text, image)
        }

        fun showToast(context: Context, @StringRes resId: Int, image: Int) {
            try {
                showToastOnMainThread(context, context.resources.getString(resId), image)
            } catch (e: Exception) {
            }
        }

        fun showToast(context: Context, text: String) {
            showToastOnMainThread(context, text, -1)
        }

        fun showToast(context: Context?, textId: Int) {
            if (context == null) {
                return
            }
            showToastOnMainThread(context, context.getString(textId), -1)
        }

        @SuppressLint("CheckResult")
        private fun showToastOnMainThread(context: Context, text: String, image: Int) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                showToastUi(context, text, image)
            } else if (context is Activity) {
                context.runOnUiThread {
                    showToastUi(
                        context,
                        text,
                        image
                    )
                }
            } else {
                Observable.just(1).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ showToastUi(context, text, image) },
                        { })
            }
        }

        /**
         * 显示土司
         */
        private fun showToastUi(context: Context?, text: String, image: Int) {
            if (context == null || TextUtils.isEmpty(text)) return
            if (context is Activity && (context.isFinishing || context.isDestroyed)) return
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    if (toast == null) {
                        toast = ToastCompat.makeText(
                            context.applicationContext,
                            text,
                            Toast.LENGTH_LONG
                        )
                    }
                } else {
                    if (null != toast) {
                        toast!!.cancel()
                        toast = null
                    }
                    toast =
                        ToastCompat.makeText(context.applicationContext, text, Toast.LENGTH_LONG)
                }
                toast!!.setText(text)
                toast!!.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}