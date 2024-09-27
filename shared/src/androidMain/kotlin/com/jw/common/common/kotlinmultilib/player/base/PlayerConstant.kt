package com.jw.common.common.kotlinmultilib.player.base

/**
 *Created by Joyce.wang on 2024/9/6 15:05
 *@Description TODO
 */
class PlayerConstant {
    companion object {
        const val PLAYER_ANALYZE_DURATION_KEY: String = "player_analyze_duration_key"
        const val PLAYER_ANALYZE_DURATION_DEFAULT: Long = 10000000

        //audiotrack 阻塞自动恢复配置
        const val ENABLE_AUTO_RESTART_AUDIO_KEY: String = "enable_auto_restart_audio_key"
    }
}