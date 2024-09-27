package com.jw.media.lib.utils.appstat

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Debug
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.text.TextUtils
import com.jw.media.lib.utils.appstat.features.IAppFeatures
import com.jw.media.lib.utils.async.ThreadsBox
import com.jw.media.lib.utils.provider.ContextProvider
import com.jw.media.lib.utils.rxjava.StreamController
import com.jw.media.lib.utils.utils.ProcessUtils
import io.reactivex.Observable
import java.util.Locale
import kotlin.concurrent.Volatile

/**
 *Created by Joyce.wang on 2024/9/23 17:21
 *@Description TODO
 */
class AppFeatureImpl(application: Application) : IAppFeatures,
    ActivityLifecycleCallbacks, ComponentCallbacks2 {
    private val scAppStat: StreamController<IAppFeatures.AppStat> = StreamController()
    private var resumeActivityCount = 0
    private var currentStat: IAppFeatures.AppStat = IAppFeatures.AppStat.Foreground
    var handlerThread: HandlerThread
    var handler: Handler
    var topCmd: TopCmd

    init {
        application.registerActivityLifecycleCallbacks(this)
        application.registerComponentCallbacks(this)
        handlerThread = ThreadsBox.getNewHandlerThread(AppFeatureImpl::class.java.simpleName)
        handler = Handler(handlerThread.looper, null)
        topCmd = TopCmd(handler)
        topCmd.start()
    }

    override fun getAppRunningTime(): Long {
        return System.currentTimeMillis() - appStartTimeStamp
    }

    override fun getCurrentAppStat(): IAppFeatures.AppStat {
        return currentStat
    }

    override fun isAppForeground(): Boolean {
        return currentStat === IAppFeatures.AppStat.Foreground
    }

    override fun isAppBackground(): Boolean {
        return currentStat === IAppFeatures.AppStat.Background
    }

    override fun appStatChanged(): Observable<IAppFeatures.AppStat> {
        return scAppStat.stream()
    }

    override fun exitApp() {
        AppManager.INSTANCE.getActivities()?.clearAllActivity()
        onDispatchBackground()
        Process.killProcess(Process.myPid())
    }

    override fun collectionAppStatusSnapShot(): String {
        val pid = Process.myPid()
        val builder = StringBuilder()
        val activityManager = (ContextProvider.getContext()
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val set: Set<Thread> = Thread.getAllStackTraces().keys

        builder.append("System:\n")
            .append(ProcessUtils.exeCommandsWithFormat("cat /proc/meminfo\n"))
            .append("\nApp:\n")
            .append("\nJava Threads:").append(set.size).append("\n")
            .append(ProcessUtils.exeCommandsWithFormat("cat /proc/$pid/status | grep Threads"))
            .append("\n")
            .append("\nApp Pss: ").append(Debug.getPss() / 1024).append(" mb\n")
            .append("Android System memoryInfo ").append("availMem: ")
            .append(memoryInfo.availMem / 1024 / 1024).append("mb").append(" totalMem: ")
            .append(memoryInfo.totalMem / 1024 / 1024).append("mb").append(" threshold:")
            .append(memoryInfo.threshold / 1024 / 1024).append("mb\n")
            .append("\nJavaHeap total: ").append(Runtime.getRuntime().totalMemory() / 1024 / 1024)
            .append("mb").append(" used: ").append(
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
                    .freeMemory()) / 1024 / 1024
            ).append("mb\n")
            .append("Android System JavaHeap limit: ").append(activityManager.memoryClass)
            .append("mb\n")
            .append("\nThreads:\n")
        for (thread in set) {
            builder.append(thread.name)
            builder.append("\t")
            builder.append(thread.state)
            builder.append("\n")
        }
        return builder.toString()
    }

    override fun getSysStatus(): List<IAppFeatures.SysStat> {
        val sysStats: ArrayList<IAppFeatures.SysStat> = ArrayList<IAppFeatures.SysStat>()
        if (topCmd.isIOBusy) sysStats.add(IAppFeatures.SysStat.IO_BUSY)
        if (topCmd.isLowMem) sysStats.add(IAppFeatures.SysStat.LOW_MEMORY)
        return sysStats
    }

    private fun onDispatchForeground() {
        currentStat = IAppFeatures.AppStat.Foreground
        scAppStat.push(currentStat)
    }

    private fun onDispatchBackground() {
        currentStat = IAppFeatures.AppStat.Background
        scAppStat.push(currentStat)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        if (resumeActivityCount++ == 0) {
            onDispatchForeground()
        }
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        if (--resumeActivityCount == 0) {
            onDispatchBackground()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onTrimMemory(level: Int) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN && currentStat === IAppFeatures.AppStat.Foreground) { // fallback
            onDispatchBackground()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
    }

    override fun onLowMemory() {
    }

    class TopCmd(var handler: Handler) : Runnable {
        var memoryInfo: ActivityManager.MemoryInfo = ActivityManager.MemoryInfo()
        var Ob_Duration_Sec: Long = 3
        var argForDisplayThread: String? = null
        var argForDisplayMax: String? = null
        var argForDisplayTimes: String? = null

        @Volatile
        var isIOBusy: Boolean = false

        @Volatile
        var isLowMem: Boolean = false

        var exceptionTimes: Int = 0

        fun initArg(): Boolean {
            try {
                var top: String? = ProcessUtils.exeCommandsWithFormat("top -h")
                if (top != null && !top.lowercase(Locale.getDefault()).startsWith("usage")) {
                    top = ProcessUtils.exeCommandsWithFormat("top --help")
                }

                if (top != null) {
                    val h = top.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    var index: Int = 0
                    for (str in h) {
                        if (str.contains("threads") && (str.indexOf("-").also {
                                index = it
                            }) >= 0) {
                            argForDisplayThread = str.substring(index, index + 2)
                        } else if (str.contains("Maximum") && (str.indexOf("-").also {
                                index = it
                            }) >= 0) {
                            argForDisplayMax = str.substring(index, index + 2)
                        } else if (str.lowercase(Locale.getDefault())
                                .contains("exit") && (str.indexOf("-").also {
                                index = it
                            }) >= 0
                        ) {
                            argForDisplayTimes = str.substring(index, index + 2)
                        }
                    }
                }
            } catch (e: Exception) {
                exceptionTimes++
            }
            return exceptionTimes >= 3
        }

        fun start() {
            handler.postDelayed(this, Duration)
        }

        fun startInternal(Duration: Long) {
            handler.postDelayed(this, Duration)
        }

        override fun run() {
            if (argForDisplayTimes != null && argForDisplayThread != null && argForDisplayMax != null) {
                val top: String? =
                    ProcessUtils.exeCommandsWithFormat("top $argForDisplayThread $argForDisplayMax 5 $argForDisplayTimes 1")
                if (top != null) {
                    val contents = top.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    var isLowMem = false
                    var isIOBusy = false
                    for (str in contents) {
                        if (str.contains(" kswapd")) {
                            //linux mem
                            isLowMem = true
                        } else if (str.contains(" mmcqd") //B9 E9....
                            || str.contains(" nand") //MXQ:dolphin_fvd_p1, RedOne...
                        ) {
                            //linux io
                            isIOBusy = true
                        }
                    }
                    this.isIOBusy = isIOBusy
                    this.isLowMem = isLowMem
                    if (isIOBusy || isLowMem) {
                        Ob_Duration_Sec += 2
                        startInternal(Ob_Duration_Sec)
                        return
                    }
                    Ob_Duration_Sec = 3
                }
            } else if (initArg()) {
                return
            }
            start()
        }

        companion object {
            const val Duration: Long = (1000 * 1).toLong()
        }
    }

    companion object {
        private val appStartTimeStamp = System.currentTimeMillis()
    }
}