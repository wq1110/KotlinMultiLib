package com.jw.common.common.kotlinmultilib.player.base.render

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import com.jw.common.common.kotlinmultilib.player.base.VideoMeasureHelper
import com.jw.common.common.kotlinmultilib.player.base.VideoType

/**
 *Created by Joyce.wang on 2024/9/5 8:44
 *@Description Renders video onto a SurfaceView.
 */
class SurfaceRenderView : SurfaceView, SurfaceHolder.Callback2, IRenderView, VideoSizeChangeListener {
    var videoMeasureHelper: VideoMeasureHelper? = null
    var videoSizeChangeListener: VideoSizeChangeListener? = null
    var iRenderCallback: IRenderView.IRenderCallback? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context,
                attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context,
                attrs: AttributeSet?,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context,
                attrs: AttributeSet?,
                defStyleAttr: Int,
                defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        videoMeasureHelper = VideoMeasureHelper(this, this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        videoMeasureHelper!!.prepareMeasure(widthMeasureSpec, heightMeasureSpec, rotation.toInt())
        setMeasuredDimension(
            videoMeasureHelper!!.getMeasuredWidth(),
            videoMeasureHelper!!.getMeasuredHeight()
        )
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        iRenderCallback?.onSurfaceCreated(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        iRenderCallback?.onSurfaceSizeChanged(holder.surface, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        //清空释放
        iRenderCallback?.onSurfaceDestroyed(holder.surface)
    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
        TODO("Not yet implemented")
    }

    override fun shouldWaitForResize(): Boolean {
        return true
    }

    override fun setRenderCallback(callback: IRenderView.IRenderCallback?) {
        holder.addCallback(this)
        iRenderCallback = callback
    }

    override fun getRenderView(): View? {
        return this
    }

    override fun setVideoSizeChangeListener(listener: VideoSizeChangeListener?) {
        videoSizeChangeListener = listener
    }

    override fun getCurrentVideoWidth(): Int {
        return videoSizeChangeListener?.getCurrentVideoWidth() ?: 0
    }

    override fun getCurrentVideoHeight(): Int {
        return videoSizeChangeListener?.getCurrentVideoHeight() ?: 0
    }

    override fun getVideoSarNum(): Int {
        return videoSizeChangeListener?.getVideoSarNum() ?: 0
    }

    override fun getVideoSarDen(): Int {
        return videoSizeChangeListener?.getVideoSarDen() ?: 0
    }

    override fun getAspectRatioMode(): Int {
        return videoSizeChangeListener?.getAspectRatioMode() ?: VideoType.AR_ASPECT_FIT_PARENT
    }


    companion object {
        /**
         * Creates and adds a SurfaceRenderView to the given parent.
         *
         * @param context The context.
         * @param renderViewContainer The parent ViewGroup.
         * @param rotate The rotation angle.
         * @param renderCallback The render callback.
         * @param videoSizeChangeListener The video size change listener.
         * @return The created SurfaceRenderView.
         */
        fun addSurfaceView(
            context: Context,
            renderViewContainer: ViewGroup,
            rotate: Int,
            renderCallback: IRenderView.IRenderCallback?,
            videoSizeChangeListener: VideoSizeChangeListener?
        ): SurfaceRenderView {
            val surfaceRenderView = SurfaceRenderView(context)
            surfaceRenderView.setRenderCallback(renderCallback)
            surfaceRenderView.setVideoSizeChangeListener(videoSizeChangeListener)
            surfaceRenderView.setRotation(rotate.toFloat())
            RenderView.addToParent(renderViewContainer, surfaceRenderView)
            return surfaceRenderView
        }
    }
}