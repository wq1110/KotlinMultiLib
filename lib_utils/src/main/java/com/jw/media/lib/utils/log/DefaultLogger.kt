package com.jw.media.lib.utils.log

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date

/**
 *Created by Joyce.wang on 2024/9/24 14:55
 *@Description TODO
 */
class DefaultLogger : LoggerInterface {
    @SuppressLint("SimpleDateFormat")
    override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        var level = "N"
        when (priority) {
            2 -> level = "V"
            3 -> level = "D"
            4 -> level = "I"
            5 -> level = "W"
            6 -> level = "E"
        }
        println("[" + sdf.format(Date()) + "]" + "[" + level + "]" + "[" + tag + "] " + message)
        t?.printStackTrace()
    }
}