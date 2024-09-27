package com.jw.media.lib.utils.async

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import android.util.Printer
import com.jw.media.lib.utils.appstat.AppManager
import com.jw.media.lib.utils.appstat.features.IAppFeatures
import com.jw.media.lib.utils.appstat.features.IAppForeground
import io.reactivex.functions.Consumer
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger
import kotlin.concurrent.Volatile

/**
 *Created by Joyce.wang on 2024/9/24 10:58
 *@Description 线程的管理: 池化
 *             提供默认的执行线程
 *             提供新的执行线程
 */
class ThreadsBox {
    companion object {
        private val TAG: String = ThreadsBox::class.java.simpleName
        private val DEBUG_THREAD_NAME: String = "debug_thread"

        /**
         * unite defaultHandlerThread for lightweight work,
         * if you have heavy work checking, you can create a new thread
         */
        @Volatile
        private var defaultHandlerThread: HandlerThread? = null
        @Volatile
        private var defaultHandler: Handler? = null
        @Volatile
        private var defaultMainHandler: Handler = Handler(Looper.getMainLooper())
        private val handlerThreads: HashSet<HandlerThread> = HashSet()
        private val isDebug: Boolean = false

        fun getDefaultMainHandler(): Handler {
            return defaultMainHandler;
        }

        fun getDefaultHandlerThread(): HandlerThread {
            synchronized(ThreadsBox::class.java) {
                if (null == defaultHandlerThread) {
                    defaultHandlerThread = HandlerThread(DEBUG_THREAD_NAME)
                    defaultHandlerThread!!.priority = Process.THREAD_PRIORITY_BACKGROUND
                    defaultHandlerThread!!.start()
                    defaultHandler = Handler(defaultHandlerThread!!.looper)
                    defaultHandlerThread!!.looper
                        .setMessageLogging(if (isDebug) LooperPrinter() else null)
                }
                return defaultHandlerThread!!
            }
        }

        fun getDefaultHandler(): Handler? {
            if (defaultHandler == null) {
                getDefaultHandlerThread()
            }
            return defaultHandler
        }

        @Synchronized
        fun getNewHandlerThread(name: String): HandlerThread {
            val i = handlerThreads.iterator()
            while (i.hasNext()) {
                val element = i.next()
                if (!element.isAlive) {
                    i.remove()
                }
            }
            val handlerThread = HandlerThread(name)
            handlerThread.priority = Process.THREAD_PRIORITY_BACKGROUND
            handlerThread.start()
            handlerThread.looper.setMessageLogging(if (isDebug) LooperPrinter() else null)
            handlerThreads.add(handlerThread)
            return handlerThread
        }
    }

    class LooperPrinter internal constructor() : Printer, IAppForeground {
        private val hashMap = ConcurrentHashMap<String, Info>()
        private var isForeground: Boolean

        init {
            AppManager.INSTANCE.getApp()?.appStatChanged()?.subscribe(Consumer<Any> { appStat -> onForeground(appStat === IAppFeatures.AppStat.Foreground) })

            this.isForeground = AppManager.INSTANCE.getApp()?.isAppForeground() ?: false
        }

        override fun println(x: String) {
            if (isForeground) {
                return
            }
            if (x[0] == '>') {
                val start = x.indexOf("} ")
                val end = x.indexOf("@", start)
                if (start < 0 || end < 0) {
                    return
                }
                val content = x.substring(start, end)
                var info = hashMap[content]
                if (info == null) {
                    info = Info()
                    info.key = content
                    hashMap[content] = info
                }
                ++info.count
            }
        }

        override fun onForeground(isForeground: Boolean) {
            this.isForeground = isForeground
            if (isForeground) {
                val start = System.currentTimeMillis()
                val list = LinkedList<Info>()
                for (info in hashMap.values) {
                    if (info.count > 1) {
                        list.add(info)
                    }
                }

                list.sortWith(Comparator { o1, o2 -> o2.count - o1.count })

                hashMap.clear()
                if (!list.isEmpty()) {
                    Logger.getLogger(TAG).info(
                        String.format(
                            "matrix default thread has exec in background! %s cost:%s",
                            list,
                            System.currentTimeMillis() - start
                        )
                    )
                }
            } else {
                hashMap.clear()
            }
        }

        internal inner class Info {
            var key: String? = null
            var count: Int = 0

            override fun toString(): String {
                return "$key:$count"
            }
        }
    }
}