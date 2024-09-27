package com.jw.media.lib.utils.utils

import android.annotation.SuppressLint
import android.content.Context

/**
 *Created by Joyce.wang on 2024/9/24 16:11
 *@Description TODO
 */
class AppContextUtils {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mContext: Context? = null
        private var launcherAppId: String = ""

        /**
         * 初始化工具类
         *
         * @param context 上下文
         */
        fun init(context: Context) {
            this.mContext = context.applicationContext
        }

        fun getContext(): Context {
            if (mContext != null) return mContext!!
            throw NullPointerException("You must init first")
        }

        fun getLauncherAppId(): String {
            return launcherAppId
        }

        fun setLauncherAppId(launcherAppId: String) {
            this.launcherAppId = launcherAppId
        }
    }
}