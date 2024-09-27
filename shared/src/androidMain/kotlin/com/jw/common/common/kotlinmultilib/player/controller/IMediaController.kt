package com.jw.common.common.kotlinmultilib.player.controller

import android.text.Spanned
import com.jw.common.common.kotlinmultilib.player.base.PlayerStatus
import com.jw.common.common.kotlinmultilib.player.listener.OnMediaControllerListener

/**
 *Created by Joyce.wang on 2024/9/11 17:06
 *@Description TODO
 */
interface IMediaController {
    //Device State(区分手机还是盒子)
    fun isMobileDevice(): Boolean

    // Playback State
    fun isPlaying(): Boolean
    fun isInPlaybackState(): Boolean
    fun getDuration(): Long
    fun getCurrentPosition(): Long
    fun getCurrentPlayState(): PlayerStatus?
    fun getCurrentBufferPercentage(): Int

    //UI Control
    fun hideLoadingBox()
    fun showLoadingBox()
    fun hideMediaPlay()
    fun showMediaPlay()
    fun setMediaTitle(title: String?)

    //Content Meta data
    fun setIsSeries(isSeries: Boolean)
    fun setIsTrailer(isTrailer: Boolean)
    fun setIsCachedFilm(isCacheFinishFilm: Boolean)
    fun setIsFromCachePage(isFromCachePage: Boolean)
    fun setAllowDisplayNextEpisode(isAllowDisplayNextEpisode: Boolean)

    //Loading Information
    fun setLoadingPercent(loadingPercent: String?)
    fun setLoadingSpeed(loadingSpeed: String?)
    fun setLoadingExtraInfo(txt: String?)

    //Focus and Subtitles
    fun getMediaAssView(): AssView?
    fun setSubtitleTextSize(size: Float)
    fun setSubtitleTextColor(textColor: Int)
    fun setSubtitleBackgroundColor(backColor: Int)
    fun toggleSubtitleView(show: Boolean)
    fun updateSubtitleText(subtitleInShifting: Boolean, spannedText: Spanned?)

    //Time Tracking
    fun getActualPlaybackTime(): Long
    fun resetPlaybackTime()
    fun setStatisticSub(isStatisticSub: Boolean)
    fun getActualSubtitleTime(): Long
    fun resetSubtitleTime(isResetLastTime: Boolean)

    //Player Control
    fun resetPlayer()
    fun enterFullScreen()
    fun exitFullScreen()
    fun setOnMediaControllerListener(onPlayerListener: OnMediaControllerListener?)
}