package com.jw.media.lib.utils.log

/**
 *Created by Joyce.wang on 2024/9/24 14:48
 *@Description TODO
 */
interface LoggerInterface {
    fun log(priority: Int, tag: String?, message: String?, t: Throwable?)
}