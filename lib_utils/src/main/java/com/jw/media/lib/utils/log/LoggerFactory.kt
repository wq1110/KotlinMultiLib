package com.jw.media.lib.utils.log

/**
 *Created by Joyce.wang on 2024/9/24 14:49
 *@Description TODO
 */
class LoggerFactory {
    companion object {
        fun getLogger(clazz: Class<*>): Logger {
            return getLogger(clazz.canonicalName)
        }

        fun getLogger(tag: String?): Logger {
            if (Logger.getLogger() == null) Logger.setLogger(DefaultLogger())
            return Logger.newInstance(tag)
        }
    }
}