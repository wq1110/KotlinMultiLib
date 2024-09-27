package com.jw.media.lib.utils.appstat

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Build
import android.os.Bundle
import android.view.ActionMode
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.SearchEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import com.jw.media.lib.utils.appstat.features.IEventsFeatures
import com.jw.media.lib.utils.rxjava.StreamController
import io.reactivex.Observable
import io.reactivex.functions.Function
import java.util.concurrent.TimeUnit

/**
 *Created by Joyce.wang on 2024/9/24 10:50
 *@Description TODO
 */
class EventsFeatureImpl(application: Application) : IEventsFeatures,
    ActivityLifecycleCallbacks {
    var scUserKeyEvent: StreamController<KeyEvent> = StreamController() //按键
    var scUserTouchEvent: StreamController<MotionEvent> = StreamController() //触控 鼠标点击
    var scUserGenericEvent: StreamController<MotionEvent> = StreamController() //鼠标
    var scUserTrackballEvent: StreamController<MotionEvent> = StreamController() //轨迹球

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activity.window.callback = CallBackWrapper(activity.window.callback)
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity.window.callback is CallBackWrapper) {
            activity.window.callback =
                (activity.window.callback as CallBackWrapper).originalCallback
        }
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun userOperationHappening(duration: Int, timeUnit: TimeUnit): Observable<Any> {
        return Observable.merge(
            scUserTouchEvent.stream(),
            scUserKeyEvent.stream(),
            scUserGenericEvent.stream(),
            scUserTrackballEvent.stream()
        ).throttleFirst(duration.toLong(), timeUnit).map(Function { it -> Any() })
    }

    private inner class CallBackWrapper(var originalCallback: Window.Callback) : Window.Callback {
        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            scUserKeyEvent.push(event)
            return originalCallback.dispatchKeyEvent(event)
        }

        override fun dispatchKeyShortcutEvent(event: KeyEvent): Boolean {
            return originalCallback.dispatchKeyShortcutEvent(event)
        }

        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            scUserTouchEvent.push(event)
            return originalCallback.dispatchTouchEvent(event)
        }

        override fun dispatchTrackballEvent(event: MotionEvent): Boolean {
            scUserTrackballEvent.push(event)
            return originalCallback.dispatchTrackballEvent(event)
        }

        override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
            scUserGenericEvent.push(event)
            return originalCallback.dispatchGenericMotionEvent(event)
        }

        override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent): Boolean {
            return originalCallback.dispatchPopulateAccessibilityEvent(event)
        }

        override fun onCreatePanelView(featureId: Int): View? {
            return originalCallback.onCreatePanelView(featureId)
        }

        override fun onCreatePanelMenu(featureId: Int, menu: Menu): Boolean {
            return originalCallback.onCreatePanelMenu(featureId, menu)
        }

        override fun onPreparePanel(featureId: Int, view: View?, menu: Menu): Boolean {
            return originalCallback.onPreparePanel(featureId, view, menu)
        }

        override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
            return originalCallback.onMenuOpened(featureId, menu)
        }

        override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
            return originalCallback.onMenuItemSelected(featureId, item)
        }

        override fun onWindowAttributesChanged(attrs: WindowManager.LayoutParams) {
            originalCallback.onWindowAttributesChanged(attrs)
        }

        override fun onContentChanged() {
            originalCallback.onContentChanged()
        }

        override fun onWindowFocusChanged(hasFocus: Boolean) {
            originalCallback.onWindowFocusChanged(hasFocus)
        }

        override fun onAttachedToWindow() {
            originalCallback.onAttachedToWindow()
        }

        override fun onDetachedFromWindow() {
            originalCallback.onDetachedFromWindow()
        }

        override fun onPanelClosed(featureId: Int, menu: Menu) {
            originalCallback.onPanelClosed(featureId, menu)
        }

        override fun onSearchRequested(): Boolean {
            return originalCallback.onSearchRequested()
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        override fun onSearchRequested(searchEvent: SearchEvent): Boolean {
            return originalCallback.onSearchRequested(searchEvent)
        }

        override fun onWindowStartingActionMode(callback: ActionMode.Callback): ActionMode? {
            return originalCallback.onWindowStartingActionMode(callback)
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        override fun onWindowStartingActionMode(
            callback: ActionMode.Callback,
            type: Int
        ): ActionMode? {
            return originalCallback.onWindowStartingActionMode(callback, type)
        }

        override fun onActionModeStarted(mode: ActionMode) {
            originalCallback.onActionModeStarted(mode)
        }

        override fun onActionModeFinished(mode: ActionMode) {
            originalCallback.onActionModeFinished(mode)
        }
    }
}