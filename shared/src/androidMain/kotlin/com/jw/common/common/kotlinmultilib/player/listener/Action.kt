package com.jw.common.common.kotlinmultilib.player.listener

/**
 *Created by Joyce.wang on 2024/9/11 17:27
 *@Description TODO
 */
interface Action<T> {
    fun call(t: T)
}