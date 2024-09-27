package com.jw.common.common.kotlinmultilib.player.base

import tv.danmaku.ijk.media.player.IMediaPlayer

/**
 *Created by Joyce.wang on 2024/9/6 15:25
 *@Description TODO
 */
interface MediaPlayerListener {
    fun onPrepared(mp: IMediaPlayer?)

    fun onCompletion(mp: IMediaPlayer?)

    fun onBufferingUpdate(mp: IMediaPlayer?, percent: Int)

    fun onSeekComplete(mp: IMediaPlayer?)

    fun onError(mp: IMediaPlayer?, what: Int, extra: Int)

    fun onInfo(mp: IMediaPlayer?, what: Int, extra: Int)

    fun onVideoSizeChanged()

    fun pause()

    fun resumePlay()

    fun isPlaying(): Boolean

    fun setNeedMute(needMute: Boolean)
}