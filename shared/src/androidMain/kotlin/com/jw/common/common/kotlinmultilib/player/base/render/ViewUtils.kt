package com.jw.common.common.kotlinmultilib.player.base.render

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView

/**
 *Created by Joyce.wang on 2024/9/6 14:18
 *
 *@Description simplifying view operation tool class
 */
class ViewUtils {
    private var context: Context? = null
    private var rootView: View? = null
    private var findView: View? = null

    /**
     * Constructor for use with a specific root view.
     *
     * @param context The context.
     * @param rootView The root view to search within.
     */
    constructor(context: Context, rootView: View) {
        this.context = context
        this.rootView = rootView
    }

    /**
     * Constructor for use within an Activity.
     *
     * @param activity The current Activity.
     */
    constructor(activity: Activity) {
        this.context = activity
        this.rootView = activity.window.decorView
    }

    /**
     * Finds a view by its ID within the current scope.
     *
     * @param id The ID of the view to find.
     * @return This ViewUtils instance for chaining.
     */
    fun id(id: Int): ViewUtils {
        if (rootView != null) {
            findView = rootView!!.findViewById(id)
        }
        return this
    }

    /**
     * Sets the image resource of an ImageView.
     *
     * @param resId The resource ID of the image.
     * @return This ViewUtils instance for chaining.
     */
    fun image(resId: Int): ViewUtils {
        if (findView is ImageView) {
            (findView as ImageView).setImageResource(resId)
        }
        return this
    }

    /**
     * Sets the visibility of the view to VISIBLE.
     *
     * @return This ViewUtils instance for chaining.
     */
    fun visible(): ViewUtils {
        if (findView != null) {
            findView!!.visibility = View.VISIBLE
        }
        return this
    }

    /**
     * Sets the visibility of the view to GONE.
     *
     * @return This ViewUtils instance for chaining.
     */
    fun gone(): ViewUtils {
        if (findView != null) {
            findView!!.visibility = View.GONE
        }
        return this
    }

    /**
     * Sets the visibility of the view to INVISIBLE.
     *
     * @return This ViewUtils instance for chaining.
     */
    fun invisible(): ViewUtils {
        if (findView != null) {
            findView!!.visibility = View.INVISIBLE
        }
        return this
    }

    /**
     * Sets an OnClickListener for the view.
     *
     * @param handler The OnClickListener to set.
     * @return This ViewUtils instance for chaining.
     */
    fun clicked(handler: View.OnClickListener?): ViewUtils {
        if (findView != null) {
            findView!!.setOnClickListener(handler)
        }
        return this
    }

    /**
     * Sets the text of a TextView.
     *
     * @param text The text to set.
     * @return This ViewUtils instance for chaining.
     */
    fun text(text: CharSequence?): ViewUtils {
        if (findView != null && findView is TextView) {
            (findView as TextView).text = text
        }
        return this
    }

    /**
     * Sets the visibility of the view.
     *
     * @param visibility The visibility value (e.g., View.VISIBLE).
     * @return This ViewUtils instance for chaining.
     */
    fun visibility(visibility: Int): ViewUtils {
        if (findView != null) {
            findView!!.visibility = visibility
        }
        return this
    }

    /**
     * Sets the width or height of the view.
     *
     * @param isWidth  True to set width, false to set height.
     * @param width   The width value.
     * @param isDip True if the size is in dips, false if in pixels.
     */
    private fun size(isWidth: Boolean, width: Int, isDip: Boolean) {
        var width = width
        if (findView != null) {
            val lp = findView!!.layoutParams
            if (width > 0 && isDip) {
                width = dip2pixel(context!!, width.toFloat())
            }
            if (isWidth) {
                lp.width = width
            } else {
                lp.height = width
            }
            findView!!.layoutParams = lp
        }
    }

    /**
     * Sets the width of the view.
     *
     * @param width  The width value.
     * @param isDip True if the width is in dips, false if in pixels.
     */
    fun width(width: Int, isDip: Boolean) {
        size(true, width, isDip)
    }

    /**
     * Sets the height of the view.
     *
     * @param height The height value.
     * @param isDip True if the height is in dips, false if in pixels.
     */
    fun height(height: Int, isDip: Boolean) {
        size(false, height, isDip)
    }

    /**
     * Converts dips to pixels.
     *
     * @param context The context.
     * @param dips    The value in dips.
     * @return The value in pixels.
     */
    fun dip2pixel(context: Context, dips: Float): Int {
        val value = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dips,
            context.resources.displayMetrics
        ).toInt()
        return value
    }

    /**
     * Converts pixels to dips.
     *
     * @param context The context.
     * @param pixels The value in pixels.
     * @return The value in dips.
     */
    fun pixel2dip(context: Context, pixels: Float): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        val dp = pixels / (metrics.densityDpi / 160f)
        return dp
    }
}