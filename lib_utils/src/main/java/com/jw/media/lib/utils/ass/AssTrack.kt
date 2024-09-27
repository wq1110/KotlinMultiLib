package com.jw.media.lib.utils.ass

import androidx.annotation.Keep

/**
 *Created by Joyce.wang on 2024/9/27 16:35
 *@Description TODO
 */
@Keep
class AssTrack {
    @Keep
    var ptr: Long = 0
    external fun destory()
}