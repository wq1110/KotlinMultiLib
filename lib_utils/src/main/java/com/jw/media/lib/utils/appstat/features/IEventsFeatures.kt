package com.jw.media.lib.utils.appstat.features

import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 *Created by Joyce.wang on 2024/9/23 17:00
 *@Description TODO
 */
interface IEventsFeatures {
    fun userOperationHappening(duration: Int, timeUnit: TimeUnit): Observable<Any>
}