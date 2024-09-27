package com.jw.media.lib.utils.ass

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 *Created by Joyce.wang on 2024/9/27 16:38
 *@Description TODO
 */
object ResourcesUtil {
    @Throws(IOException::class)
    fun resourceToFile(resources: Resources?, resourceId: Int, file: File) {
        val bitmap = BitmapFactory.decodeResource(resources, resourceId)

        if (file.exists()) {
            file.delete()
        }

        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
    }

    @Throws(IOException::class)
    fun rawResourceToFile(resources: Resources, resourceId: Int, file: File) {
        val inputStream = resources.openRawResource(resourceId)
        if (file.exists()) {
            file.delete()
        }
        val outputStream = FileOutputStream(file)

        try {
            val buffer = ByteArray(1024)
            var readSize: Int

            while ((inputStream.read(buffer).also { readSize = it }) > 0) {
                outputStream.write(buffer, 0, readSize)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Log.e(TAG, String.format("Saving raw resource failed.%s", Exceptions.getStackTraceString(e)));
        } finally {
            inputStream.close()
            outputStream.flush()
            outputStream.close()
        }
    }
}