package com.jw.media.lib.utils.ass

import android.graphics.Bitmap
import androidx.annotation.Keep

/**
 *Created by Joyce.wang on 2024/9/27 16:34
 *@Description TODO
 */
@Keep
internal class AssBitmapPool {
    var lbp: LruBitmapPool = LruBitmapPool(
        ContextProvider.getContext().getResources()
            .getDisplayMetrics().widthPixels as Long * ContextProvider.getContext().getResources()
            .getDisplayMetrics().heightPixels * 4
    )

    companion object {
        var singleton: Singleton<AssBitmapPool> = object : Singleton<AssBitmapPool?>() {
            protected fun create(): AssBitmapPool {
                return AssBitmapPool()
            }
        }

        val instance: AssBitmapPool
            get() = singleton.get()

        fun recycle(bitmap: Bitmap?) {
            instance.lbp.put(bitmap)
        }


        fun get(width: Int, height: Int, config: Bitmap.Config?): Bitmap {
            return instance.lbp.get(width, height, config)
        }

        fun clear() {
            instance.lbp.clearMemory()
        }
    }
}