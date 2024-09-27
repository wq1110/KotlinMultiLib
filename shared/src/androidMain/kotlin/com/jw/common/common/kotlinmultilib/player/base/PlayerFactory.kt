package com.jw.common.common.kotlinmultilib.player.base

/**
 *Created by Joyce.wang on 2024/9/9 10:29
 *@Description 播放器内核工厂，根据对应的播放器管理类，创建对应的播放器管理对象
 *             A factory class responsible for creating instances of IPlayerManager.
 */
object PlayerFactory {
    var sPlayerManagerClass: Class<out IPlayerManager>? = null

    fun setPlayManager(playerManagerClass: Class<out IPlayerManager>?) {
        sPlayerManagerClass = playerManagerClass
    }

    fun getPlayManager(): IPlayerManager? {
        if (sPlayerManagerClass == null) {
            sPlayerManagerClass = IjkPlayerManager::class.java
        }
        try {
            return sPlayerManagerClass!!.newInstance()
        } catch (e: InstantiationException) {
            e.printStackTrace()
            return null
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            return null
        }
    }
}