package com.jw.media.lib.utils.provider

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 *Created by Joyce.wang on 2024/9/24 15:20
 *@Description 全局Context提供者
 */
class ContextProvider private constructor(mContext: Context) {
    private lateinit var mContext: Context

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: ContextProvider? = null

        fun getContext(): Context {
            return get().mContext
        }

        fun getApplication(): Application {
            return get().mContext.applicationContext as Application
        }

        private fun get(): ContextProvider {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        val context = ACProvider.mContext
                        if (context == null) {
                            throw IllegalStateException("context == null")
                        }
                        instance = ContextProvider(context)
                    }
                }
            }
            return instance!!
        }
    }
}