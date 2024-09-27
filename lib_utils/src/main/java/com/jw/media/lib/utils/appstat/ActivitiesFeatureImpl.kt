package com.jw.media.lib.utils.appstat

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.jw.media.lib.utils.appstat.features.IActivitiesFeatures
import java.lang.ref.WeakReference

/**
 *Created by Joyce.wang on 2024/9/23 17:01
 *@Description 处理Activity范围的事情
 */
class ActivitiesFeatureImpl(application: Application) : Application.ActivityLifecycleCallbacks, IActivitiesFeatures {
    private val activities: MutableList<Activity> = ArrayList()
    private var atyRef: WeakReference<Activity>? = null
    private var visibleScene: String? = "default"
    private val activitiesEventListeners: MutableList<IActivitiesFeatures.IActivitiesEventListener> = ArrayList()

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    private fun updateScene(activity: Activity) {
        visibleScene = activity.javaClass.name
        atyRef = WeakReference(activity)
    }

    fun getVisibleScene(): String? {
        return visibleScene
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activities.add(activity)
        for (listener in activitiesEventListeners) {
            listener.onNewActivityIn(activity.componentName, activity.callingActivity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        updateScene(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityPaused(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityStopped(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        TODO("Not yet implemented")
    }

    override fun onActivityDestroyed(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun getTopActivity(): Activity? {
        return atyRef?.get()
    }

    override fun getAllActivity(): MutableList<Activity> {
        return ArrayList(activities)
    }

    override fun clearAllActivity() {
        for (activity in activities) {
            activity.finish()
        }
        visibleScene = null
        atyRef = null
        activities.clear()
    }

    override fun addActivitiesEventListener(listener: IActivitiesFeatures.IActivitiesEventListener?) {
        if (listener != null) {
            activitiesEventListeners.add(listener)
        }
    }

    override fun removeActivitiesEventListener(listener: IActivitiesFeatures.IActivitiesEventListener?) {
        if (listener != null) {
            activitiesEventListeners.remove(listener)
        }
    }
}