package com.jw.common.common.kotlinmultilib.player.base.render

import android.content.Context
import android.util.AttributeSet
import android.view.Surface
import android.view.ViewGroup
import android.widget.FrameLayout
import com.jw.common.common.kotlinmultilib.player.base.VideoType

/**
 *Created by Joyce.wang on 2024/9/6 14:40
 *@Description A view responsible for rendering media.
 *             播放绘制view
 */
abstract class MediaRenderView : FrameLayout, IRenderView.IRenderCallback, VideoSizeChangeListener {
    //native绘制
    protected var mSurface: Surface? = null
    //渲染控件
    protected var mRenderView: RenderView? = null
    //渲染控件父类
    protected var mRenderContainer: ViewGroup? = null
    //画面选择角度
    protected var mRotate: Int = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr)

    /**
     * 添加播放的view
     * 继承后重载addRenderView，继承RenderView后实现自己的RenderView类
     */
    protected fun addRenderView() {
        mRenderView = RenderView()
        if (mRenderContainer != null) {
            mRenderView!!.addView(
                context, mRenderContainer!!, VideoType.RENDER_SURFACE_VIEW, mRotate,
                this,
                this
            )
        }
    }

    override fun onSurfaceCreated(surface: Surface?) {
        mSurface = surface
        setDisplay(surface)

        onSurfaceCreatedEvent(surface)
    }

    override fun onSurfaceSizeChanged(surface: Surface?, width: Int, height: Int) {
        onSurfaceChangedEvent(surface, width, height)
    }

    override fun onSurfaceDestroyed(surface: Surface?): Boolean {
        //清空释放
        setDisplay(null)

        //同一消息队列中去release
        onSurfaceDestroyedEvent(surface)
        return true
    }

    override fun onSurfaceUpdated(surface: Surface?) {
        TODO("Not yet implemented")
    }

    //设置播放
    protected abstract fun setDisplay(surface: Surface?)
    protected abstract fun onSurfaceCreatedEvent(surface: Surface?)
    protected abstract fun onSurfaceChangedEvent(surface: Surface?, width: Int, height: Int)
    protected abstract fun onSurfaceDestroyedEvent(surface: Surface?)
}