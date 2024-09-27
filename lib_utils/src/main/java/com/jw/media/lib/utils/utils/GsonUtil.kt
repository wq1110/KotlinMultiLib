package com.jw.media.lib.utils.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 *Created by Joyce.wang on 2024/9/27 10:15
 *@Description TODO
 */
class GsonUtil {
    companion object {
        fun getGsonBuilder(): GsonBuilder {
            return GsonBuilder()
        }

        fun getGson(): Gson {
            return getGsonBuilder().create()
        }
    }
}