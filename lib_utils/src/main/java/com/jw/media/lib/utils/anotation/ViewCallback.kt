package com.jw.media.lib.utils.anotation

/**
 *Created by Joyce.wang on 2024/9/23 16:47
 *@Description Presenter绑定View
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(
    AnnotationRetention.RUNTIME
)
annotation class ViewCallback
