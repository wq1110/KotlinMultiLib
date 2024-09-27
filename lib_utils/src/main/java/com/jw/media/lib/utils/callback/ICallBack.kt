package com.jw.media.lib.utils.callback

import android.os.Handler
import android.os.Looper

/**
 *Created by Joyce.wang on 2024/9/24 13:38
 *@Description 通用单数据回调
 */
fun interface ICallBack<T> {
    companion object {
        private val mainHandler = Handler(Looper.getMainLooper())
    }

    //执行体
    fun call(t: T)

    // 一定在UI线程中执行
    fun inUIThread(): ICallBack<T> = if (Looper.myLooper() == Looper.getMainLooper()) this else ICallBack {
        mainHandler.post { call(it) }
    }
}