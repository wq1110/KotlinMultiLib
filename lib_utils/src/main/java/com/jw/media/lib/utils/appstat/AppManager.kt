package com.jw.media.lib.utils.appstat

import android.app.Application
import com.jw.media.lib.utils.appstat.features.IActivitiesFeatures
import com.jw.media.lib.utils.appstat.features.IAppFeatures
import com.jw.media.lib.utils.appstat.features.IEventsFeatures

/**
 *Created by Joyce.wang on 2024/9/24 10:50
 *@Description 应用 运行状态 管理类
 */
enum class AppManager {
    INSTANCE;

    private var mActivitiesImpl: ActivitiesFeatureImpl? = null
    private var mAppImpl: AppFeatureImpl? = null
    private var mEventsImpl: EventsFeatureImpl? = null

    @Synchronized
    fun init(application: Application) {
        if (mActivitiesImpl == null) mActivitiesImpl = ActivitiesFeatureImpl(application)
        if (mAppImpl == null) mAppImpl = AppFeatureImpl(application)
        if (mEventsImpl == null) mEventsImpl = EventsFeatureImpl(application)
    }


    fun getActivities(): IActivitiesFeatures? {
        return mActivitiesImpl
    }

    fun getApp(): IAppFeatures? {
        return mAppImpl
    }

    fun getEvents(): IEventsFeatures? {
        return mEventsImpl
    }
}