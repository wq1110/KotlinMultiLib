package com.jw.common.common.kotlinmultilib.player.base.render

import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import tv.danmaku.ijk.media.player.IMediaPlayer

/**
 *Created by Joyce.wang on 2024/9/4 17:30
 *@Description Represents a view responsible for rendering video content.
 */
interface IRenderView {
    fun shouldWaitForResize(): Boolean

    /**
     * Sets the callback to receive rendering events.
     *
     * @param callback The callback to be notified of rendering events.
     */
    fun setRenderCallback(callback: IRenderView.IRenderCallback?)

    /**
     * Retrieves the underlying view responsible for rendering.
     *
     * @return The rendering view.
     */
    fun getRenderView(): View?

    /**
     * Sets the listener to be notified of video size changes.
     *
     * @param listener The listener to receive video size change events.
     */
    fun setVideoSizeChangeListener(listener: VideoSizeChangeListener?)

    interface ISurfaceHolder {
        fun bindToMediaPlayer(mp: IMediaPlayer?)

        fun getRenderView(): IRenderView

        fun getSurfaceHolder(): SurfaceHolder?

        fun openSurface(): Surface?

        fun getSurfaceTexture(): SurfaceTexture?
    }

    interface IRenderCallback {
        /**
         * Called when a surface is created.
         *
         * @param surface The newly created surface.
         */
        fun onSurfaceCreated(surface: Surface?)

        /**
         * Called when the size of a surface changes.
         *
         * @param surface The surface whose size has changed.
         * @param width   The new width of the surface.
         * @param height  The new height of the surface.
         */
        fun onSurfaceSizeChanged(surface: Surface?, width: Int, height: Int)

        /**
         * Called when a surface is about to be destroyed.
         *
         * @param surface The surface being destroyed.
         */
        fun onSurfaceDestroyed(surface: Surface?): Boolean

        /**
         * Called when a surface's contents are updated.
         *
         * @param surface The surface whose contents have been updated.
         */
        fun onSurfaceUpdated(surface: Surface?)
    }
}