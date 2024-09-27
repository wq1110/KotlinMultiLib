package com.jw.media.lib.utils.utils

import android.content.Context
import android.util.TypedValue

/**
 *Created by Joyce.wang on 2024/9/24 17:10
 *@Description TODO
 */
class DensityUtils {
    companion object {
        var screenWidthPx: Int = 0 //屏幕宽 px
        var screenHeightPx: Int = 0 //屏幕高 px

        /**
         * dp转px
         * @param context
         * @param dpVal
         * @return
         */
        fun dp2px(context: Context, dpVal: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.resources.displayMetrics
            ).toInt()
        }

        /**
         * sp转px
         *
         * @param context
         * @param spVal
         * @return
         */
        fun sp2px(context: Context, spVal: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                spVal, context.resources.displayMetrics
            ).toInt()
        }

        /**
         * px转dp
         *
         * @param context
         * @param pxVal
         * @return
         */
        fun px2dp(context: Context, pxVal: Float): Float {
            val scale = context.resources.displayMetrics.density
            return (pxVal / scale)
        }

        /**
         * px转sp
         *
         * @param context
         * @param pxVal
         * @return
         */
        fun px2sp(context: Context, pxVal: Float): Float {
            return (pxVal / context.resources.displayMetrics.scaledDensity)
        }

        fun dip2px(context: Context, var1: Float): Int {
            val var2 = context.resources.displayMetrics.density
            return (var1 * var2 + 0.5f).toInt()
        }
    }
}