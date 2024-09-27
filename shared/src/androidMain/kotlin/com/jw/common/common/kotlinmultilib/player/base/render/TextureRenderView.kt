package com.jw.common.common.kotlinmultilib.player.base.render

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import com.jw.common.common.kotlinmultilib.player.base.VideoMeasureHelper
import com.jw.common.common.kotlinmultilib.player.base.VideoType

/**
 *Created by Joyce.wang on 2024/9/5 10:54
 *@Description Renders video onto a TextureView.
 */
class TextureRenderView : TextureView, TextureView.SurfaceTextureListener, IRenderView, VideoSizeChangeListener {

    private var videoMeasureHelper: VideoMeasureHelper? = null
    private var videoSizeChangeListener: VideoSizeChangeListener? = null
    private var renderCallback: IRenderView.IRenderCallback? = null
    private var saveTexture: SurfaceTexture? = null
    private var surface: Surface? = null
    private var usingMediaCodec = true //是否使用硬解渲染

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
        videoMeasureHelper!!.doMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            videoMeasureHelper!!.getMeasuredWidth(),
            videoMeasureHelper!!.getMeasuredHeight()
        )
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        if (usingMediaCodec) {
            if (saveTexture == null) {
                saveTexture = surfaceTexture
                surface = Surface(surfaceTexture)
            } else {
                setSurfaceTexture(saveTexture!!)
            }
            if (renderCallback != null) {
                renderCallback!!.onSurfaceCreated(surface)
            }
        } else {
            surface = Surface(surfaceTexture)
            if (renderCallback != null) {
                renderCallback!!.onSurfaceCreated(surface)
            }
        }
    }

    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        renderCallback?.onSurfaceSizeChanged(surface, width, height)
    }

    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
        //清空释放
        renderCallback?.onSurfaceUpdated(surface)
        return if (usingMediaCodec) {
            surfaceTexture == null
        } else {
            true
        }
    }

    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
        //如果播放的是暂停全屏了
        renderCallback?.onSurfaceUpdated(surface)
    }

    override fun shouldWaitForResize(): Boolean {
        return false
    }

    override fun setRenderCallback(callback: IRenderView.IRenderCallback?) {
        surfaceTextureListener = this
        renderCallback = callback
    }

    override fun getRenderView(): View {
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
         * Creates and adds a TextureRenderView to the given ViewGroup.
         *
         * @param context                The context.
         * @param renderViewContainer    The ViewGroup to add the TextureRenderView to.
         * @param rotate                 The rotation angle for the TextureRenderView.
         * @param renderCallback         The IRenderCallback for the TextureRenderView.
         * @param videoSizeChangeListener The VideoSizeChangeListener for the TextureRenderView.
         * @return The created TextureRenderView.
         */
        fun addTextureView(
            context: Context,
            renderViewContainer: ViewGroup,
            rotate: Int,
            renderCallback: IRenderView.IRenderCallback?,
            videoSizeChangeListener: VideoSizeChangeListener?
        ): TextureRenderView {
            val mediaTextureView = TextureRenderView(context)
            mediaTextureView.setRenderCallback(renderCallback)
            mediaTextureView.setVideoSizeChangeListener(videoSizeChangeListener)
            mediaTextureView.rotation = rotate.toFloat()
            RenderView.addToParent(renderViewContainer, mediaTextureView)
            return mediaTextureView
        }
    }
}