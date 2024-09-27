package com.jw.common.common.kotlinmultilib.player.listener

/**
 *Created by Joyce.wang on 2024/9/11 17:31
 *@Description TODO
 */
interface OnStateAndUiListener {
    fun onStateChange(status: Int, isResetLastPlayTime: Boolean)
}