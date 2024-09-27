package com.jw.common.common.kotlinmultilib.player.base

/**
 *Created by Joyce.wang on 2024/9/6 15:07
 *@Description TODO
 */
enum class PlayerStatus {
    STATE_IDLE,
    STATE_PREPARING,
    STATE_PREPARED,
    STATE_PLAYING,
    STATE_PAUSED,
    STATE_COMPLETED,
    STATE_ERROR
}