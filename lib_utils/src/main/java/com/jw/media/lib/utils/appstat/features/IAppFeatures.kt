package com.jw.media.lib.utils.appstat.features

import io.reactivex.Observable

/**
 *Created by Joyce.wang on 2024/9/23 16:52
 *@Description 应用 层面
 */
interface IAppFeatures {
    fun getAppRunningTime(): Long

    fun getCurrentAppStat(): AppStat?

    fun isAppForeground(): Boolean

    fun isAppBackground(): Boolean

    fun appStatChanged(): Observable<AppStat>?

    fun exitApp()

    fun collectionAppStatusSnapShot(): String?

    fun getSysStatus(): List<SysStat>?

    enum class SysStat {
        LOW_MEMORY,
        IO_BUSY
    }

    enum class AppStat {
        Background,
        Foreground
    }

    enum class AppMemoryWaring
}