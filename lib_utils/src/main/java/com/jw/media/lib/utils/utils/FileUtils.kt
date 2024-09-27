package com.jw.media.lib.utils.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import com.jw.media.lib.utils.log.LoggerFactory
import org.apache.commons.io.FilenameUtils
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.math.max

/**
 *Created by Joyce.wang on 2024/9/24 17:14
 *@Description TODO
 */
object FileUtils {
    private val logger = LoggerFactory.getLogger(FileUtils::class.java)
    private var sOverrideMap: HashMap<String, String>? = null

    init {
        sOverrideMap = HashMap<String, String>()
        sOverrideMap?.put("tr", "ISO-8859-9")
        sOverrideMap?.put("sr", "Windows-1250")
    }

    /**
     * Get contents of a file as String
     *
     * @param filePath File path as String
     * @return Contents of the file
     */
    @Throws(IOException::class)
    fun getContentsAsString(filePath: String): String {
        val fl = File(filePath)
        val fin = FileInputStream(fl)
        val ret = convertStreamToString(fin)
        //Make sure you close all streams.
        fin.close()
        return ret
    }

    /**
     * Convert an [InputStream] to a String
     *
     * @param inputStream InputStream
     * @return String contents of the InputStream
     */
    @Throws(IOException::class)
    private fun convertStreamToString(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        var line: String?
        while ((reader.readLine().also { line = it }) != null) {
            sb.append(line).append("\n")
        }
        reader.close()
        return sb.toString()
    }

    /**
     * Delete every item below the File location
     *
     * @param file Location
     */
    fun recursiveDelete(file: File): Boolean {
        if (file.isDirectory) {
            val children = file.list() ?: return false
            for (child in children) {
                recursiveDelete(File(file, child))
            }
        }

        return file.delete()
    }


    /**
     * Save [String] to [File] witht the specified encoding
     *
     * @param string [String]
     * @param path   Path of the file
     * @param string Encoding
     */
    @Throws(IOException::class)
    fun saveStringToFile(string: String?, path: File, encoding: String?) {
        if (path.exists()) {
            path.delete()
        }

        if ((path.parentFile.mkdirs() || path.parentFile.exists()) && (path.exists() || path.createNewFile())) {
            val writer: Writer =
                BufferedWriter(OutputStreamWriter(FileOutputStream(path), encoding))
            writer.write(string)
            writer.close()
        }
    }

    @Throws(IOException::class)
    fun stringToFile(file: File?, string: String?) {
        stringToFile(file, string, false)
    }

    @Throws(IOException::class)
    fun stringToFile(file: File?, string: String?, append: Boolean) {
        val out = FileWriter(file, append)
        try {
            out.write(string)
        } finally {
            out.close()
        }
    }

    /**
     * Get the extension of the file
     *
     * @param fileName Name (and location) of the file
     * @return Extension
     */
    fun getFileExtension(fileName: String): String {
        var extension = ""

        val i = fileName.lastIndexOf('.')
        val p =
            max(fileName.lastIndexOf('/').toDouble(), fileName.lastIndexOf('\\').toDouble())
                .toInt()

        if (i > p) {
            extension = fileName.substring(i + 1)
        }

        return extension
    }


    /**
     * 选择的文件是名称
     *
     * @param uri
     * @return
     */
    fun getFileNameByUri(context: Context?, uri: Uri?): String {
        if (context == null || uri == null) {
            return ""
        }
        val cursor = context.contentResolver.query(uri, null, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val name =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
            cursor.close()
            return name
        }
        return ""
    }


    /**
     * Copy file (only use for files smaller than 2GB)
     *
     * @param src Source
     * @param dst Destionation
     */
    @Throws(IOException::class)
    fun copy(src: File?, dst: File?) {
        val inStream = FileInputStream(src)
        val outStream = FileOutputStream(dst)
        val inChannel = inStream.channel
        val outChannel = outStream.channel
        inChannel.transferTo(0, inChannel.size(), outChannel)
        inStream.close()
        outStream.close()
    }


    /**
     * 设计到外部sd卡存储的，要使用这个方法，通过文件选择器反馈的uri来读取文件
     *
     * @param context
     * @param uri
     * @param dest
     */
    fun copy(context: Context, uri: Uri?, dest: File?) {
        try {
            val inStream = context.contentResolver.openInputStream(uri!!)
            if (inStream != null) {
                createParent(dest)
                val fos = FileOutputStream(dest)

                if (inStream is FileInputStream) {
                    val inChannel = inStream.channel
                    val outChannel = fos.channel
                    inChannel.transferTo(0, inChannel.size(), outChannel)
                    inChannel.close()
                    outChannel.close()
                } else {
                    val bis = BufferedInputStream(inStream)
                    val bos = BufferedOutputStream(fos)
                    val byteArray = ByteArray(1024)
                    var bytes = 0
                    while ((bis.read(byteArray).also { bytes = it }) != -1) {
                        bos.write(byteArray, 0, bytes)
                    }
                    bos.close()
                    fos.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createParent(file: File?) {
        try {
            if (file == null) {
                return
            }
            if (file.parentFile != null) {
                if (!file.parentFile.exists()) {
                    file.parentFile.mkdirs()
                }
            } else {
                if (!TextUtils.isEmpty(file.parent)) {
                    File(file.parent).mkdirs()
                }
            }
        } catch (e: Exception) {
            logger.e("create parent error : $e")
        }
    }

    fun findFilesWithExtension(baseDir: String?, extention: String?, result: MutableList<File?>) {
        val dir = File(baseDir)
        if (!dir.exists() || !dir.isDirectory) return

        val files = dir.listFiles() ?: return

        for (file in files) {
            if (!file.isDirectory) {
                if (FilenameUtils.getExtension(file.absolutePath)
                        .equals(extention, ignoreCase = true)
                ) {
                    result.add(file)
                }
            } else {
                findFilesWithExtension(file.absolutePath, extention, result)
            }
        }
    }

    fun makeDir(path: String?): Boolean {
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdir()
            return true
        } else if (dir.exists() && !dir.isDirectory) {
            return false
        } else {
            return true
        }
    }

    @Throws(IOException::class)
    fun unzip(zipFile: File?, targetDirectory: File?) {
        val zis = ZipInputStream(
            BufferedInputStream(FileInputStream(zipFile))
        )
        try {
            var ze: ZipEntry
            var count: Int
            val buffer = ByteArray(8192)
            while ((zis.nextEntry.also { ze = it }) != null) {
                val file = File(targetDirectory, ze.name)
                val dir = if (ze.isDirectory) file else file.parentFile
                if (!dir.isDirectory && !dir.mkdirs()) throw FileNotFoundException("Failed to ensure directory: " + dir.absolutePath)
                if (ze.isDirectory) continue
                val fout = FileOutputStream(file)
                try {
                    while ((zis.read(buffer).also { count = it }) != -1) fout.write(
                        buffer,
                        0,
                        count
                    )
                } finally {
                    fout.close()
                }
            }
        } finally {
            zis.close()
        }
    }

    fun save2File(filename: String?, data: ByteArray?, offset: Int, len: Int) {
        val dumpFile = File(filename)
        if (!dumpFile.exists()) {
            try {
                dumpFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        var bos: BufferedOutputStream? = null
        var fos: FileOutputStream? = null
        val fileWriter: FileWriter? = null
        try {
            fos = FileOutputStream(dumpFile, true)
            bos = BufferedOutputStream(fos)
            bos.write(data, offset, len)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (bos != null) {
                try {
                    bos.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
            }
            if (fos != null) {
                try {
                    fos.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
            }
        }
    }

    fun deleteQuietly(path: String?): Boolean {
        val file = File(path)
        return deleteQuietly(file)
    }

    fun deleteQuietly(file: File?): Boolean {
        if (file == null) {
            return false
        }
        try {
            if (file.isDirectory) {
                org.apache.commons.io.FileUtils.cleanDirectory(file)
            }
        } catch (ignored: Exception) {
        }

        return try {
            file.delete()
        } catch (ignored: Exception) {
            false
        }
    }


    //delete file or Directory
    fun deleteFileOrDirectory(filePath: String): Boolean {
        val file = File(filePath)
        return if (!file.exists()) {
            false
        } else {
            if (file.isFile) {
                // 为文件时调用删除文件方法
                deleteFile(filePath)
            } else {
                // 为目录时调用删除目录方法
                deleteDirectory(filePath)
            }
        }
    }

    //delete file
    fun deleteFile(filePath: String?): Boolean {
        if (TextUtils.isEmpty(filePath)) {
            return true
        }
        val file = File(filePath)
        if (file.isFile && file.exists()) {
            return file.delete()
        }
        return false
    }

    //delete Directory
    fun deleteDirectory(filePath: String): Boolean {
        var filePath = filePath
        var flag = false
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator
        }
        val dirFile = File(filePath)
        if (!dirFile.exists() || !dirFile.isDirectory) {
            return false
        }
        flag = true
        val files = dirFile.listFiles()
        //遍历删除文件夹下的所有文件(包括子目录)
        if (files != null) {
            for (file in files) {
                if (file.isFile) {
                    //删除子文件
                    flag = deleteFile(file.absolutePath)
                    if (!flag) break
                } else {
                    //删除子目录
                    flag = deleteDirectory(file.absolutePath)
                    if (!flag) break
                }
            }
        }

        if (!flag) return false
        //删除当前空目录
        return dirFile.delete()
    }

    @Throws(IOException::class)
    fun copyFileFromAssets(context: Context, filename: String?, destinationPath: String?) {
        val assetManager = context.assets
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            `in` = assetManager.open(filename!!)
            val outFile = File(destinationPath, filename)
            if (!outFile.parentFile.exists()) {
                outFile.parentFile.mkdirs() // 如果目标目录不存在，则创建
            }
            out = FileOutputStream(outFile)
            copyFile(`in`, out)
        } finally {
            `in`?.close()
            out?.close()
        }
    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while ((`in`.read(buffer).also { read = it }) != -1) {
            out.write(buffer, 0, read)
        }
    }
}