package com.jw.common.common.kotlinmultilib.player.base

import tv.danmaku.ijk.media.player.misc.IMediaFormat

/**
 *Created by Joyce.wang on 2024/9/6 15:02
 *@Description 轨道信息
 */
class TrackInfoEx {
    var format: IMediaFormat? = null
    var language: String? = null
    var trackType: Int = 0
    var infoInline: String? = null
    var trackId: Int = 0
    var lable: String? = null
    var isExtraAudio: Boolean = false

    fun getFormat(): IMediaFormat? {
        return format
    }

    fun setFormat(format: IMediaFormat?) {
        this.format = format
    }

    fun getLanguage(): String? {
        return language
    }

    fun setLanguage(language: String?) {
        this.language = language
    }

    fun getTrackType(): Int {
        return trackType
    }

    fun setTrackType(trackType: Int) {
        this.trackType = trackType
    }

    fun getInfoInline(): String? {
        return infoInline
    }

    fun setInfoInline(infoInline: String?) {
        this.infoInline = infoInline
    }

    fun getTrackId(): Int {
        return trackId
    }

    fun setTrackId(trackId: Int) {
        this.trackId = trackId
    }

    fun getLable(): String? {
        return lable
    }

    fun setLable(lable: String?) {
        this.lable = lable
    }

    fun isExtraAudio(): Boolean {
        return isExtraAudio
    }

    fun setExtraAudio(extraAudio: Boolean) {
        isExtraAudio = extraAudio
    }
}