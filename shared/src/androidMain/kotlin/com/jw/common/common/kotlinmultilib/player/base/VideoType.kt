package com.jw.common.common.kotlinmultilib.player.base

/**
 *Created by Joyce.wang on 2024/9/4 17:10
 *@Description TODO
 */
object VideoType {
    // 播放器类型
    const val PV_PLAYER__IjkExoMediaPlayer: Int = 0
    const val PV_PLAYER__AndroidMediaPlayer: Int = 1
    const val PV_PLAYER__IjkMediaPlayer: Int = 2

    const val MODE_NORMAL: Int = 10 //普通模式
    const val MODE_FULL_SCREEN: Int = 11 //全屏模式
    const val MODE_SMALL_WINDOW: Int = 12 //小窗口模式


    const val AR_ASPECT_FIT_PARENT: Int = 0 // without clip, 自适应屏幕等比例缩放，不变型（保证画面完整显示且画面贴近窗口，但不一定能铺满窗口，可能一边留有黑边）
    const val AR_ASPECT_FILL_PARENT: Int = 1 // may clip 填充，不变型，切割画面（等比例缩放铺满窗口，画面可能会被裁掉）
    const val AR_ASPECT_WRAP_CONTENT: Int = 2 //自适应 等比例缩小（保证画面完整显示），不进行等比例放大（当视频大小小于屏幕宽高时，以视频宽高为基础展示）
    const val AR_MATCH_PARENT: Int = 3 //拉伸（非等比例缩放铺满窗口，画面可能会变形）
    const val AR_16_9_FIT_PARENT: Int = 4 //16:9
    const val AR_4_3_FIT_PARENT: Int = 5 //4:3

    const val RENDER_SURFACE_VIEW: Int = 1 //渲染类型，SurfaceView
    const val RENDER_TEXTURE_VIEW: Int = 2 //渲染类型，TextureView

    //audiotrack block error
    const val FFP_MSG_ERROR_997: Int = 997
    const val FFP_MSG_ERROR_998: Int = 998

    const val MEDIA_INFO_PLAYER_TYPE: Int = 1000001

    const val ERROR_TRAILER_URL_NOT_EXIST: Int = 5000
    const val ERROR_TRAILER_PARSE_URL_EXCEPTION: Int = 5001

    const val TYPE_SOURCE_YOUTUBE: String = "youtube"
}