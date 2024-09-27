package com.jw.media.lib.utils.ass

import java.io.File
import java.io.FileOutputStream

/**
 *Created by Joyce.wang on 2024/9/27 16:39
 *@Description TODO
 */
internal object Utils {
    fun storeToCache(dir: File?, data: ByteArray?, fileName: String?) {
        try {
            val fileOutputStream = FileOutputStream(File(dir, fileName))
            fileOutputStream.write(data)
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}