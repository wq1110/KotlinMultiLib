package com.jw.media.lib.utils.callback

/**
 *Created by Joyce.wang on 2024/9/24 13:37
 *@Description TODO
 */
interface IAction<T: Any> {
    fun get(): T
}