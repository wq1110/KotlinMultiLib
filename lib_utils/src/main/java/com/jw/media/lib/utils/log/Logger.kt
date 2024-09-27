package com.jw.media.lib.utils.log

import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException
import java.util.regex.Pattern

/**
 *Created by Joyce.wang on 2024/9/24 14:49
 *@Description TODO
 */
class Logger {
    companion object {
        private val NONE: Int = 1
        private val VERBOSE: Int = 2
        private val DEBUG: Int = 3
        private val INFO: Int = 4
        private val WARN: Int = 5
        private val ERROR: Int = 6

        private val ANONYMOUS_CLASS: Pattern = Pattern.compile("(\\$\\d+)+$")
        private var listener: LoggerInterface? = null
        private var LOG_LEVEL: Int = DEBUG

        fun newInstance(tag: String?): Logger {
            return Logger(tag)
        }

        fun setLogger(logger: LoggerInterface?) {
            listener = logger
        }

        fun getLogger(): LoggerInterface? {
            return listener
        }

        fun logLevel(level: Int) {
            require(!(level < NONE || level > ERROR)) { "log level should between NONE-2 and ERROR-6" }
            LOG_LEVEL = level
        }
    }

    private var tag: String? = null

    constructor(tag: String?) {
        this.tag = tag
    }

    private fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
        if (listener != null) listener!!.log(priority, tag, message, t)
    }

    fun v(message: String?, vararg args: Any?) {
        prepareLog(VERBOSE, null, message, args)
    }

    fun v(t: Throwable?, message: String?, vararg args: Any?) {
        prepareLog(VERBOSE, t, message, args)
    }

    fun d(message: String?, vararg args: Any?) {
        prepareLog(DEBUG, null, message, *args)
    }

    fun d(t: Throwable?, message: String?, vararg args: Any?) {
        prepareLog(DEBUG, t, message, *args)
    }

    fun isDebug(): Boolean {
        return LOG_LEVEL <= DEBUG
    }

    fun isInfo(): Boolean {
        return LOG_LEVEL <= INFO
    }

    fun i(message: String?, vararg args: Any?) {
        prepareLog(INFO, null, message, *args)
    }

    fun i(t: Throwable?, message: String?, vararg args: Any?) {
        prepareLog(INFO, t, message, *args)
    }


    fun w(message: String?, vararg args: Any?) {
        prepareLog(WARN, null, message, *args)
    }

    fun w(t: Throwable?, message: String?, vararg args: Any?) {
        prepareLog(WARN, t, message, *args)
    }

    fun e(message: String?, vararg args: Any?) {
        prepareLog(ERROR, null, message, *args)
    }

    fun e(t: Throwable?, message: String?, vararg args: Any?) {
        prepareLog(ERROR, t, message, *args)
    }


    private fun prepareLog(priority: Int, t: Throwable?, message: String?, vararg args: Any?) {
        var message: String? = message
        try {
            if (listener != null && isLoggable(priority)) {
                if (message != null && message.length == 0) {
                    message = null
                }

                if (message == null) {
                    if (t == null) {
                        return
                    }

                    message = getStackTraceString(t)
                } else {
                    if (args.size > 0) {
                        message = String.format(message, *args)
                    }

                    if (t != null) {
                        message = """
                        $message
                        ${getStackTraceString(t)}
                        """.trimIndent()
                    }
                }

                log(priority, getTag(), message, t)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isLoggable(priority: Int): Boolean {
        return priority >= LOG_LEVEL
    }

    private fun getStackTraceString(tr: Throwable?): String {
        if (tr == null) return ""
        var t = tr
        while (t != null) {
            if (t is UnknownHostException) return ""
            t = t.cause
        }
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    private fun getTag(): String {
        if (tag != null && tag!!.length > 0) {
            return tag.toString()
        }
        val stackTrace = Throwable().stackTrace
        if (stackTrace.size <= 5) {
            return ""
        } else {
            var tag = stackTrace[5].className
            val m = ANONYMOUS_CLASS.matcher(tag)
            if (m.find()) {
                tag = m.replaceAll("")
            }
            return tag.substring(tag.lastIndexOf(46.toChar()) + 1)
        }
    }
}