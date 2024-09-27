package com.jw.common.common.kotlinmultilib.player.base

import android.content.Context
import android.view.Surface
import tv.danmaku.ijk.media.player.IMediaPlayer

/**
 *Created by Joyce.wang on 2024/9/6 15:27
 *
 *@Description Manages Player
 */
interface IPlayerManager {
    fun getMediaPlayer(): IMediaPlayer?

    /**
     * Initializes the video player with necessary configurations.
     *
     * @param context The application context.
     * @param config  Initialization configurations for the player.
     */
    fun initVideoPlayer(context: Context, config: InitializationPlayerConfig)

    fun start()

    fun stop()

    fun pause()

    fun getVideoWidth(): Int

    fun getVideoHeight(): Int

    /**
     * 是否在播放中（不包含暂停，暂停返回false）
     */
    fun isPlaying(): Boolean

    fun seekTo(position: Long)

    fun getCurrentPosition(): Long

    fun getDuration(): Long

    /**
     * 视频横向采样数值（像素点数）
     */
    fun getVideoSarNum(): Int

    /**
     * 视频纵向采样数值（像素点数）
     */
    fun getVideoSarDen(): Int

    //设置渲染显示
    fun setDisplay(surface: Surface?)

    /**
     * @description Sets whether the video should be muted.
     * @param needMute True to mute, false to unmute.
     */
    fun setNeedMute(needMute: Boolean)

    /**
     * Sets the volume levels for left and right channels.
     *
     * @param leftVolume  Left channel volume (0.0 - 1.0).
     * @param rightVolume Right channel volume (0.0 - 1.0).
     */
    fun setVolume(leftVolume: Float, rightVolume: Float)

    //Releases the rendering surface.（释放渲染）
    fun releaseSurface()

    //释放内核
    fun release()
}