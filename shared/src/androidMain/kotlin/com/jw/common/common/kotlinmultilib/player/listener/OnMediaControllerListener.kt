package com.jw.common.common.kotlinmultilib.player.listener

import android.app.Activity
import androidx.fragment.app.FragmentManager
import tv.danmaku.ijk.media.player.IMediaPlayer

/**
 *Created by Joyce.wang on 2024/9/11 17:26
 *@Description TODO
 */
interface OnMediaControllerListener {
    fun onNetworkChange(activity: Activity?, action: Action<Boolean?>?)
    fun onNetworkChange(type: OnConnectionChangeListener.ConnectionType?)
    fun onNetworkDisconnected(activity: Activity?, action: Action<Boolean?>?)

    fun onBufferingStart(mp: IMediaPlayer?)
    fun onBufferingEnd(
        mp: IMediaPlayer?, isPrepare: Boolean, bufferingStartTime: Long,
        bufferingTime: Long, prepareFirstBufferingTime: Long,
        seekFirstBuffingTime: Long, startBufferingPosition: Long
    )

    fun isMobileTraffic(): Boolean
    fun showSubtitleDialog(fragmentManager: FragmentManager?)
    fun showEpisodesDialog(activity: Activity?, fragmentManager: FragmentManager?)
    fun showResourceDialog(activity: Activity?, fragmentManager: FragmentManager?)
    fun showResolutionDialog(activity: Activity?, fragmentManager: FragmentManager?)
    fun showMoreDialog(activity: Activity?, fragmentManager: FragmentManager?)
    fun onPreparePlay(activity: Activity?)
    fun onResetPlayer()
    fun onNextEpisode(activity: Activity?)
    fun onToggleRatio()
    fun onPlayerType(playerType: Int)
    fun onFromBeginning()
    fun onBack()
    fun finish()
}