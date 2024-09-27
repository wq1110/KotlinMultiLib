package com.jw.common.common.kotlinmultilib.player.base

/**
 *Created by Joyce.wang on 2024/9/6 15:14
 *
 *@Description Initialization configurations for the player.
 */
class InitializationPlayerConfig(url: String,
                                 headers: Map<String, String>?,
                                 loop: Boolean,
                                 speed: Float,
                                 isAudioOnly: Boolean) {
    private var mUrl: String? = url
    private var mHeaders: Map<String, String>? = headers
    private var looping: Boolean = loop
    private var mSpeed: Float = speed
    private var mIsAudioOnly: Boolean = isAudioOnly

    fun getUrl(): String? {
        return mUrl
    }

    fun setUrl(url: String) {
        this.mUrl = url
    }

    fun getHeaders(): Map<String, String>? {
        return mHeaders
    }

    fun setHeaders(headers: Map<String, String>) {
        this.mHeaders = headers
    }

    fun isLooping(): Boolean {
        return looping
    }

    fun setLooping(looping: Boolean) {
        this.looping = looping
    }

    fun getSpeed(): Float {
        return mSpeed
    }

    fun setSpeed(speed: Float) {
        this.mSpeed = speed
    }

    fun isAudioOnly(): Boolean {
        return mIsAudioOnly
    }

    fun setAudioOnly(isAudioOnly: Boolean) {
        mIsAudioOnly = isAudioOnly
    }
}