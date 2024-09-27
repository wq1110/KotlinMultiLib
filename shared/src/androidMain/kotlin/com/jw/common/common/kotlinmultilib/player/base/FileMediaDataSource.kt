package com.jw.common.common.kotlinmultilib.player.base

import tv.danmaku.ijk.media.player.misc.IMediaDataSource
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

/**
 *Created by Joyce.wang on 2024/9/6 16:17
 *@Description A data source that reads from a file.
 */
class FileMediaDataSource(file: File) : IMediaDataSource {
    private var mFile: RandomAccessFile = RandomAccessFile(file, "r")
    private var mFileSize: Long = file.length()


    @Throws(IOException::class)
    override fun readAt(position: Long, buffer: ByteArray?, offset: Int, size: Int): Int {
        if (mFile.getFilePointer() != position) mFile.seek(position)

        if (size == 0) {
            return 0
        }

        return mFile.read(buffer, 0, size)
    }

    @Throws(IOException::class)
    override fun getSize(): Long {
        return mFileSize
    }

    @Throws(IOException::class)
    override fun close() {
        mFileSize = 0
        mFile.close()
    }
}