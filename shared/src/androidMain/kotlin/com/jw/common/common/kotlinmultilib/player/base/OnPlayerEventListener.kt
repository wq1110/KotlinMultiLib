package com.jw.common.common.kotlinmultilib.player.base

import tv.danmaku.ijk.media.player.IMediaPlayer

/**
 *Created by Joyce.wang on 2024/9/6 15:26
 *@Description TODO
 */
interface OnPlayerEventListener {
    fun setStateAndUi(state: PlayerStatus?, isResetLastPlayTime: Boolean)
    fun onPreparedEvent(mp: IMediaPlayer?)
    fun onCompletionEvent(mp: IMediaPlayer?)
    fun onSeekCompleteEvent(mp: IMediaPlayer?)
    fun onErrorEvent(mp: IMediaPlayer?, what: Int, extra: Int)
    fun onInfoEvent(mp: IMediaPlayer?, what: Int, extra: Int)
    fun onBufferingUpdateEvent(mp: IMediaPlayer?, percent: Int)
    fun getPlayerType(): Int
}