package com.jw.media.lib.utils.anotation

import kotlin.reflect.KClass

/**
 *Created by Joyce.wang on 2024/9/23 16:40
 *@Description TODO
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class BindView(val value: KClass<*> = Any::class)