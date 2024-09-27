package com.jw.media.lib.utils.ass

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import java.util.ArrayDeque
import java.util.Queue
import kotlin.concurrent.Volatile

/**
 *Created by Joyce.wang on 2024/9/27 16:39
 *@Description TODO
 */
object Util {
    private val HASH_MULTIPLIER = 31
    private val HASH_ACCUMULATOR = 17
    private val HEX_CHAR_ARRAY = "0123456789abcdef".toCharArray()

    // 32 bytes from sha-256 -> 64 hex chars.
    private val SHA_256_CHARS = CharArray(64)

    @Volatile
    private var mainThreadHandler: Handler? = null

    /** Returns the hex string of the given byte array representing a SHA256 hash.  */
    fun sha256BytesToHex(bytes: ByteArray): String {
        synchronized(SHA_256_CHARS) {
            return bytesToHex(bytes, SHA_256_CHARS)
        }
    }

    // Taken from:
    // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    // /9655275#9655275
    private fun bytesToHex(bytes: ByteArray, hexChars: CharArray): String {
        var v: Int
        for (j in bytes.indices) {
            v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = HEX_CHAR_ARRAY[v ushr 4]
            hexChars[j * 2 + 1] = HEX_CHAR_ARRAY[v and 0x0F]
        }
        return String(hexChars)
    }

    /**
     * Returns the allocated byte size of the given bitmap.
     *
     * @see .getBitmapByteSize
     */
    @Deprecated("Use {@link #getBitmapByteSize(Bitmap)} instead. Scheduled to be\n" + "        removed in Glide 4.0.")
    fun getSize(bitmap: Bitmap): Int {
        return getBitmapByteSize(bitmap)
    }

    /** Returns the in memory size of the given [Bitmap] in bytes.  */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getBitmapByteSize(bitmap: Bitmap): Int {
        // The return value of getAllocationByteCount silently changes for recycled bitmaps from the
        // internal buffer size to row bytes * height. To avoid random inconsistencies in caches, we
        // instead assert here.
        if (bitmap.isRecycled) {
            throw IllegalStateException(
                "Cannot obtain size for recycled Bitmap: "
                        + bitmap
                        + "["
                        + bitmap.width
                        + "x"
                        + bitmap.height
                        + "] "
                        + bitmap.config
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Workaround for KitKat initial release NPE in Bitmap, fixed in MR1. See issue #148.
            try {
                return bitmap.allocationByteCount
            } catch (e: NullPointerException) {
                // Do nothing.
            }
        }
        return bitmap.height * bitmap.rowBytes
    }

    /**
     * Returns the in memory size of [Bitmap] with the given width, height, and
     * [Bitmap.Config].
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getBitmapByteSize(width: Int, height: Int, config: Bitmap.Config?): Int {
        return width * height * getBytesPerPixel(config)
    }

    /**
     * Returns the number of bytes required to store each pixel of a [Bitmap] with the given
     * `config`.
     *
     *
     * Defaults to [Bitmap.Config.ARGB_8888] if `config` is `null`.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getBytesPerPixel(config: Bitmap.Config?): Int {
        // A bitmap by decoding a GIF has null "config" in certain environments.
        var config = config
        if (config == null) {
            config = Bitmap.Config.ARGB_8888
        }

        val bytesPerPixel: Int
        when (config) {
            Bitmap.Config.ALPHA_8 -> bytesPerPixel = 1
            Bitmap.Config.RGB_565, Bitmap.Config.ARGB_4444 -> bytesPerPixel = 2
            Bitmap.Config.RGBA_F16 -> bytesPerPixel = 8
            Bitmap.Config.ARGB_8888 -> bytesPerPixel = 4
            else -> bytesPerPixel = 4
        }
        return bytesPerPixel
    }


    /** Posts the given `runnable` to the UI thread using a shared [Handler].  */
    fun postOnUiThread(runnable: Runnable?) {
        getUiThreadHandler()!!.post((runnable)!!)
    }

    /** Removes the given `runnable` from the UI threads queue if it is still queued.  */
    fun removeCallbacksOnUiThread(runnable: Runnable?) {
        getUiThreadHandler()!!.removeCallbacks((runnable)!!)
    }

    private fun getUiThreadHandler(): Handler? {
        if (mainThreadHandler == null) {
            synchronized(Util::class.java) {
                if (mainThreadHandler == null) {
                    mainThreadHandler = Handler(Looper.getMainLooper())
                }
            }
        }
        return mainThreadHandler
    }

    /**
     * Throws an [IllegalArgumentException] if called on a thread other than the main
     * thread.
     */
    fun assertMainThread() {
        if (!isOnMainThread()) {
            throw IllegalArgumentException("You must call this method on the main thread")
        }
    }

    /** Throws an [IllegalArgumentException] if called on the main thread.  */
    fun assertBackgroundThread() {
        if (!isOnBackgroundThread()) {
            throw IllegalArgumentException("You must call this method on a background thread")
        }
    }

    /** Returns `true` if called on the main thread, `false` otherwise.  */
    fun isOnMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    /** Returns `true` if called on a background thread, `false` otherwise.  */
    fun isOnBackgroundThread(): Boolean {
        return !isOnMainThread()
    }

    /** Creates a [Queue] of the given size using Glide's preferred implementation.  */
    fun <T> createQueue(size: Int): Queue<T> {
        return ArrayDeque(size)
    }

    /**
     * Returns a copy of the given list that is safe to iterate over and perform actions that may
     * modify the original list.
     *
     *
     * See #303, #375, #322, #2262.
     */
    fun <T> getSnapshot(other: Collection<T>): List<T> {
        // toArray creates a new ArrayList internally and does not guarantee that the values it contains
        // are non-null. Collections.addAll in ArrayList uses toArray internally and therefore also
        // doesn't guarantee that entries are non-null. WeakHashMap's iterator does avoid returning null
        // and is therefore safe to use. See #322, #2262.
        val result: MutableList<T> = ArrayList(other.size)
        for (item: T? in other) {
            if (item != null) {
                result.add(item)
            }
        }
        return result
    }

    /**
     * Null-safe equivalent of `a.equals(b)`.
     *
     * @see java.util.Objects.equals
     */
    fun bothNullOrEqual(a: Any?, b: Any?): Boolean {
        return if (a == null) b == null else (a == b)
    }

    @JvmOverloads
    fun hashCode(value: Int, accumulator: Int = HASH_ACCUMULATOR): Int {
        return accumulator * HASH_MULTIPLIER + value
    }

    @JvmOverloads
    fun hashCode(value: Float, accumulator: Int = HASH_ACCUMULATOR): Int {
        return hashCode(java.lang.Float.floatToIntBits(value), accumulator)
    }

    fun hashCode(`object`: Any?, accumulator: Int): Int {
        return hashCode(`object`?.hashCode() ?: 0, accumulator)
    }

    @JvmOverloads
    fun hashCode(value: Boolean, accumulator: Int = HASH_ACCUMULATOR): Int {
        return hashCode(if (value) 1 else 0, accumulator)
    }
}