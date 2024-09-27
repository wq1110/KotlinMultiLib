package com.jw.media.lib.utils.ass

import android.content.Context

/**
 *Created by Joyce.wang on 2024/9/27 16:36
 *@Description TODO
 */
interface IAssView {
    fun render(assImage: Array<AssImage?>?)
    fun getWidth(): Int
    fun getHeight(): Int
    fun getContext(): Context?
}