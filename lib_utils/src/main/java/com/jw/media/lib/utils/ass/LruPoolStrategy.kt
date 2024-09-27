package com.jw.media.lib.utils.ass

import android.graphics.Bitmap

/**
 *Created by Joyce.wang on 2024/9/27 16:38
 *@Description TODO
 */
interface LruPoolStrategy {
    fun put(bitmap: Bitmap)

    fun get(width: Int, height: Int, config: Bitmap.Config?): Bitmap?

    fun removeLast(): Bitmap?

    fun logBitmap(bitmap: Bitmap): String

    fun logBitmap(width: Int, height: Int, config: Bitmap.Config?): String?

    fun getSize(bitmap: Bitmap?): Int
}