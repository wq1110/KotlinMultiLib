package com.jw.media.lib.utils.utils

import kotlin.concurrent.Volatile

/**
 *Created by Joyce.wang on 2024/9/27 15:24
 *@Description TODO
 */
abstract class Singleton<T> {
    @Volatile
    private var mInstance: T? = null

    protected abstract fun create(): T

    fun get(): T? {
        synchronized(this) {
            if (mInstance == null) {
                mInstance = create()
            }
            return mInstance
        }
    }

    fun clear() {
        synchronized(this) {
            mInstance = null
        }
    }
}