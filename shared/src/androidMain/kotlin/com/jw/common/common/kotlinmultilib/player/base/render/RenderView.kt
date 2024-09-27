package com.jw.common.common.kotlinmultilib.player.base.render

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.jw.common.common.kotlinmultilib.player.base.VideoType

/**
 *Created by Joyce.wang on 2024/9/5 10:41
 *@Description TODO
 */
class RenderView {
    var renderView: IRenderView? = null

    fun requestLayout() {
        renderView?.getRenderView()?.requestLayout()
    }

    /**
     * Gets the rotation of the underlying render view.
     *
     * @return The rotation in degrees.
     */
    fun getRotation(): Float {
        return renderView?.getRenderView()?.rotation ?: 0f
    }

    /**
     * Sets the rotation of the underlying render view.
     *
     * @param rotation The rotation in degrees.
     */
    fun setRotation(rotation: Float) {
        renderView?.getRenderView()?.rotation = rotation
    }


    /**
     * Invalidates the underlying render view, causing it to redraw.
     */
    fun invalidate() {
        renderView?.getRenderView()?.invalidate()
    }

    /**
     * Gets the underlying render view.
     *
     * @return The underlying render view, or null if not initialized.
     */
    fun getRenderView(): View? {
        return renderView?.getRenderView()
    }

    fun shouldWaitForResize(): Boolean {
        return renderView == null || renderView!!.shouldWaitForResize()
    }

    /**
     * Gets the layout parameters of the underlying render view.
     *
     * @return The layout parameters.
     */
    fun getLayoutParams(): ViewGroup.LayoutParams? {
        return renderView?.getRenderView()?.layoutParams
    }

    /**
     * Sets the layout parameters of the underlying render view.
     *
     * @param layoutParams The layout parameters to set.
     */
    fun setLayoutParams(layoutParams: ViewGroup.LayoutParams?) {
        renderView?.getRenderView()?.layoutParams = layoutParams
    }

    /**
     * Adds a render view to the specified container.
     *
     * @param context The application context.
     * @param renderViewContainer The container to add the render view to.
     * @param renderType The type of render view to create.
     * @param rotate The initial rotation of the render view.
     * @param iRenderCallback A callback for render events.
     * @param videoSizeChangeListener A listener for video size changes.
     */
    fun addView(
        context: Context,
        renderViewContainer: ViewGroup,
        renderType: Int,
        rotate: Int,
        iRenderCallback: IRenderView.IRenderCallback?,
        videoSizeChangeListener: VideoSizeChangeListener?
    ) {
        renderView = if (renderType == VideoType.RENDER_SURFACE_VIEW) {
            SurfaceRenderView.addSurfaceView(
                context,
                renderViewContainer,
                rotate,
                iRenderCallback,
                videoSizeChangeListener
            )
        } else {
            TextureRenderView.addTextureView(
                context,
                renderViewContainer,
                rotate,
                iRenderCallback,
                videoSizeChangeListener
            )
        }
    }

    companion object {
        private val TAG: String = RenderView::class.java.simpleName
        fun addToParent(renderViewContainer: ViewGroup, render: View) {
            val params = getRenderViewParams()
            if (renderViewContainer is RelativeLayout) {
                renderViewContainer.removeView(render)
                val layoutParams = RelativeLayout.LayoutParams(params, params)
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
                renderViewContainer.addView(render, 0, layoutParams)
            } else if (renderViewContainer is FrameLayout) {
                renderViewContainer.removeView(render)
                val layoutParams = FrameLayout.LayoutParams(params, params)
                layoutParams.gravity = Gravity.CENTER
                renderViewContainer.addView(render, 0, layoutParams)
            }
        }

        /**
         * 获取布局参数
         * @return
         */
        fun getRenderViewParams(): Int {
            return ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }
}