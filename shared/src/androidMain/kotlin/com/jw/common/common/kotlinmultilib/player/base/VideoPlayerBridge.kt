package com.jw.common.common.kotlinmultilib.player.base

import android.view.Surface

/**
 *Created by Joyce.wang on 2024/9/6 15:04
 *@Description TODO
 */
interface VideoPlayerBridge {
    /**
     * Starts preparing for playback.
     *
     * @param url     The playback URL.
     * @param headers Headers for the request.
     * @param loop    Whether to loop the playback.
     * @param speed   the playback speed.
     */
    fun prepare(
        url: String?, headers: Map<String, String>?, loop: Boolean, speed: Float, isAudioOnly: Boolean
    )

    /**
     * Gets the current player core.
     *
     * @return The current [IPlayerManager] instance.
     */
    fun getPlayer(): IPlayerManager?

    fun start()

    fun stop()

    fun pause()

    fun getVideoWidth(): Int

    fun getVideoHeight(): Int

    fun isPlaying(): Boolean

    fun seekTo(time: Long)

    fun getCurrentPosition(): Long

    fun getDuration(): Long

    fun getVideoSarNum(): Int

    fun getVideoSarDen(): Int

    fun setAspectRatioMode(aspectRatioMode: Int)

    fun getAspectRatioMode(): Int

    //释放播放器
    fun releaseMediaPlayer()

    fun setSelfVideoSizeFlag(selfVideoSizeFlag: Boolean)

    fun setCurrentVideoSize(videoWidth: Int, videoHeight: Int)

    fun getCurrentVideoWidth(): Int

    fun getCurrentVideoHeight(): Int

    fun setVideoSampleAspectRatio(videoSarNum: Int, videoSarDen: Int)

    fun getCurrentVideoSarNum(): Int

    fun getCurrentVideoSarDen(): Int

    /**
     * Sets whether the video should be muted.
     */
    fun setNeedMute(needMute: Boolean)

    /**
     * Sets the display surface for video rendering.
     */
    fun setDisplay(holder: Surface?)

    /**
     * Releases the display surface.
     */
    fun releaseSurface(surface: Surface?)

    fun getMediaPlayerListener(): MediaPlayerListener?

    fun setMediaPlayerListener(listener: MediaPlayerListener?)
}