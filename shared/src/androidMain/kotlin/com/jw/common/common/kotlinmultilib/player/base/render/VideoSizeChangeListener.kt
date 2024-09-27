package com.jw.common.common.kotlinmultilib.player.base.render

/**
 *Created by Joyce.wang on 2024/9/4 17:39
 *@Description Provides information about the dimensions of a video.
 */
interface VideoSizeChangeListener {
    fun getCurrentVideoWidth(): Int

    fun getCurrentVideoHeight(): Int

    fun getVideoSarNum(): Int

    fun getVideoSarDen(): Int

    fun getAspectRatioMode(): Int
}