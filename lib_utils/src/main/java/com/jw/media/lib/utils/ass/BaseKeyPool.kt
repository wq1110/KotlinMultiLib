package com.jw.media.lib.utils.ass

import java.util.Queue

/**
 *Created by Joyce.wang on 2024/9/27 16:35
 *@Description TODO
 */
internal abstract class BaseKeyPool<T : Poolable?> {
    private val keyPool: Queue<T> = Util.createQueue(MAX_SIZE)

    fun get(): T? {
        var result = keyPool.poll()
        if (result == null) {
            result = create()
        }
        return result
    }

    fun offer(key: T) {
        if (keyPool.size < MAX_SIZE) {
            keyPool.offer(key)
        }
    }

    abstract fun create(): T

    companion object {
        private const val MAX_SIZE = 20
    }
}