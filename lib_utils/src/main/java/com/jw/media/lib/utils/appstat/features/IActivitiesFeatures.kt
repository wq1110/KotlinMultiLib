package com.jw.media.lib.utils.appstat.features

import android.app.Activity
import android.content.ComponentName

/**
 *Created by Joyce.wang on 2024/9/23 16:51
 *@Description Activity层面
 */
interface IActivitiesFeatures {
    fun getTopActivity(): Activity?
    fun getAllActivity(): List<Activity?>?
    fun clearAllActivity()
    fun addActivitiesEventListener(listener: IActivitiesEventListener?)
    fun removeActivitiesEventListener(listener: IActivitiesEventListener?)

    interface IActivitiesEventListener {
        fun onNewActivityIn(newActivity: ComponentName?, invokeActivity: ComponentName?)
    }
}