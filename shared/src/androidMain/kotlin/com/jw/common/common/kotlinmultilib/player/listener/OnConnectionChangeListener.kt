package com.jw.common.common.kotlinmultilib.player.listener

/**
 *Created by Joyce.wang on 2024/9/11 17:25
 *@Description TODO
 */
interface OnConnectionChangeListener {
    fun onLocaleChange()

    fun onConnectionChange(type: ConnectionType?)

    enum class ConnectionType {
        WIFI, MOBILE, ETHERNET, NONE, UNKNOWN
    }
}