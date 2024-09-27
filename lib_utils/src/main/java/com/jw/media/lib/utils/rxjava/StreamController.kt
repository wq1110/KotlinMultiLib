package com.jw.media.lib.utils.rxjava

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable

/**
 *Created by Joyce.wang on 2024/9/23 17:23
 *@Description 流
 */
class StreamController<T : Any> {
    private var observable: Observable<T>? = null
    private var isDispose = false
    private val emitters: MutableList<ObservableEmitter<T>> = ArrayList()

    constructor() {
        this.observable = Observable.create<T> { emitter ->
            emitters.add(emitter)
            emitter.setDisposable(Dispose(emitter))
        }
    }

    fun push(`object`: T) {
        for (emitter in emitters) {
            emitter.onNext(`object`)
        }
    }

    fun error(throwable: Throwable) {
        for (emitter in emitters) {
            emitter.onError(throwable)
        }
    }

    fun stream(): Observable<T> {
        return observable!!
    }

    inner class Dispose(emitter: ObservableEmitter<T>) : Disposable {
        private var emitter: ObservableEmitter<T>?

        init {
            this.emitter = emitter
        }

        override fun dispose() {
            emitters.remove(emitter)
            emitter = null
            isDispose = true
        }

        override fun isDisposed(): Boolean {
            return isDispose
        }
    }
}