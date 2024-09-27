package com.jw.common.common.kotlinmultilib.player.base

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Surface
import com.jw.media.lib.utils.provider.ContextProvider
import tv.danmaku.ijk.media.player.IMediaPlayer
import java.lang.ref.WeakReference
import java.util.logging.Logger

/**
 *Created by Joyce.wang on 2024/9/10 11:19
 *@Description TODO
 */
object PlayerManager : IMediaPlayer.OnPreparedListener, IMediaPlayer.OnCompletionListener,
    IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnSeekCompleteListener,
    IMediaPlayer.OnErrorListener,
    IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnInfoListener, VideoPlayerBridge {

    private val TAG: String = PlayerManager::class.java.simpleName

    const val MSG_PREPARE: Int = 0
    const val MSG_SET_DISPLAY: Int = 1
    const val MSG_RELEASE: Int = 2
    const val MSG_RELEASE_SURFACE: Int = 3

    var currentVideoWidth: Int = 0 //当前播放的视频宽的高
    var currentVideoHeight: Int = 0 //当前播放的视屏的高
    var currentVideoSarNum: Int = 0
    var currentVideoSarDen: Int = 0
    var currentAspectRatioMode: Int = VideoType.AR_ASPECT_FIT_PARENT

    var playerManager: IPlayerManager? = null //播放器内核管理
    var needMute: Boolean = false //是否需要静音
    var mediaPlayerListenerRef: WeakReference<MediaPlayerListener>? = null
    var selfVideoSizeFlag = false

    private val mainHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_PREPARE -> initVideo(msg.obj as InitializationPlayerConfig)
                MSG_SET_DISPLAY -> handleSetDisplay(msg.obj)
                MSG_RELEASE -> {
                    selfVideoSizeFlag = false
                    releaseMediaPlayerInternal()
                }

                MSG_RELEASE_SURFACE -> releaseSurfaceInternal(msg.obj)
            }
        }
    }

    // Extract setDisplay logic into a separate method
    private fun handleSetDisplay(displayObj: Any) {
        if (displayObj is Surface && playerManager != null) {
            playerManager!!.setDisplay(displayObj as Surface)
        } else {
            // Handle invalid display object, log an error, etc.
        }
    }

    private fun releaseMediaPlayerInternal() {
        if (playerManager != null) {
            playerManager!!.release()
            playerManager = null
        }
        setNeedMute(false)
    }

    private fun releaseSurfaceInternal(obj: Any?) {
        if (obj != null && playerManager != null) {
            playerManager!!.releaseSurface()
        }
    }

    fun getPlayManager(): IPlayerManager? {
        return PlayerFactory.getPlayManager()
    }

    private fun initVideo(config: InitializationPlayerConfig) {
        try {
            currentVideoWidth = 0
            currentVideoHeight = 0
            currentVideoSarNum = 0
            currentVideoSarDen = 0
            selfVideoSizeFlag = false

            if (playerManager != null) {
                playerManager!!.release()
            }
            playerManager = getPlayManager()
            if (playerManager == null) {
                throw RuntimeException("PlayerManager is null.")
            }

            playerManager!!.initVideoPlayer(ContextProvider.getContext(), config)
            playerManager!!.setNeedMute(needMute)

            val mediaPlayer = playerManager!!.getMediaPlayer()
            if (mediaPlayer == null) {
                throw RuntimeException("MediaPlayer is null.")
            }

            mediaPlayer.setOnCompletionListener(this)
            mediaPlayer.setOnBufferingUpdateListener(this)
            mediaPlayer.setScreenOnWhilePlaying(true)
            mediaPlayer.setOnPreparedListener(this)
            mediaPlayer.setOnSeekCompleteListener(this)
            mediaPlayer.setOnErrorListener(this)
            mediaPlayer.setOnInfoListener(this)
            mediaPlayer.setOnVideoSizeChangedListener(this)
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            Logger.getLogger(TAG).info("Error initializing video player.")
            e.printStackTrace()
        }
    }

    override fun onPrepared(mp: IMediaPlayer?) {
        getMediaPlayerListener()?.onPrepared(mp)
    }

    override fun onCompletion(mp: IMediaPlayer?) {
        getMediaPlayerListener()?.onCompletion(mp)
    }

    override fun onBufferingUpdate(mp: IMediaPlayer?, percent: Int) {
        getMediaPlayerListener()?.onBufferingUpdate(mp, percent)
    }

    override fun onSeekComplete(mp: IMediaPlayer?) {
        getMediaPlayerListener()?.onSeekComplete(mp)
    }

    override fun onError(mp: IMediaPlayer?, what: Int, extra: Int): Boolean {
        getMediaPlayerListener()?.onError(mp, what, extra)
        return false
    }

    override fun onVideoSizeChanged(
        mp: IMediaPlayer?,
        width: Int,
        height: Int,
        sar_num: Int,
        sar_den: Int
    ) {
        if (mp == null || selfVideoSizeFlag) {
            return
        } else {
            currentVideoWidth = mp.videoWidth
            currentVideoHeight = mp.videoHeight
            currentVideoSarNum = mp.videoSarNum
            currentVideoSarDen = mp.videoSarDen
        }

        getMediaPlayerListener()?.onVideoSizeChanged()
    }

    override fun onInfo(mp: IMediaPlayer?, what: Int, extra: Int): Boolean {
        getMediaPlayerListener()?.onInfo(mp, what, extra)
        return true
    }

    override fun prepare(
        url: String?,
        headers: Map<String, String>?,
        loop: Boolean,
        speed: Float,
        isAudioOnly: Boolean
    ) {
        if (url == null || url.isEmpty()) {
            return
        }
        var msg = mainHandler.obtainMessage(MSG_PREPARE, InitializationPlayerConfig(url, headers, loop, speed, isAudioOnly))
        mainHandler.sendMessage(msg)
    }

    override fun getPlayer(): IPlayerManager? {
        return playerManager
    }

    override fun start() {
        playerManager?.start()
    }

    override fun stop() {
        playerManager?.stop()
    }

    override fun pause() {
        playerManager?.pause()
    }

    override fun getVideoWidth(): Int {
        return playerManager?.getVideoWidth() ?: 0
    }

    override fun getVideoHeight(): Int {
        return playerManager?.getVideoHeight() ?: 0
    }

    override fun isPlaying(): Boolean {
        return playerManager?.isPlaying() ?: false
    }

    override fun seekTo(time: Long) {
        playerManager?.seekTo(time)
    }

    override fun getCurrentPosition(): Long {
        return playerManager?.getCurrentPosition() ?: 0
    }

    override fun getDuration(): Long {
        return playerManager?.getDuration() ?: -1
    }

    override fun getVideoSarNum(): Int {
        return playerManager?.getVideoSarNum() ?: 0
    }

    override fun getVideoSarDen(): Int {
        return playerManager?.getVideoSarDen() ?: 0
    }

    override fun setAspectRatioMode(aspectRatioMode: Int) {
        currentAspectRatioMode = aspectRatioMode
    }

    override fun getAspectRatioMode(): Int {
        return currentAspectRatioMode
    }

    override fun releaseMediaPlayer() {
        val msg = Message()
        msg.what = MSG_RELEASE
        sendMessage(msg)
    }

    override fun setSelfVideoSizeFlag(selfVideoSizeFlag: Boolean) {
         this.selfVideoSizeFlag = selfVideoSizeFlag
    }

    override fun setCurrentVideoSize(videoWidth: Int, videoHeight: Int) {
        this.currentVideoWidth = videoWidth
        this.currentVideoHeight = videoHeight
    }

    override fun getCurrentVideoWidth(): Int {
        return currentVideoWidth
    }

    override fun getCurrentVideoHeight(): Int {
        return currentVideoHeight
    }

    override fun setVideoSampleAspectRatio(videoSarNum: Int, videoSarDen: Int) {
        this.currentVideoSarNum = videoSarNum
        this.currentVideoSarDen = videoSarDen
    }

    override fun getCurrentVideoSarNum(): Int {
        return currentVideoSarNum
    }

    override fun getCurrentVideoSarDen(): Int {
        return currentVideoSarDen
    }

    override fun setNeedMute(needMute: Boolean) {
        this.needMute = needMute
        playerManager?.setNeedMute(needMute)
    }

    override fun setDisplay(holder: Surface?) {
        val msg = mainHandler.obtainMessage(MSG_SET_DISPLAY, holder)
        mainHandler.sendMessage(msg)
    }

    override fun releaseSurface(surface: Surface?) {
        val msg = mainHandler.obtainMessage(MSG_RELEASE_SURFACE, surface)
        mainHandler.sendMessage(msg)
    }

    override fun getMediaPlayerListener(): MediaPlayerListener? {
        return mediaPlayerListenerRef?.get()
    }

    override fun setMediaPlayerListener(listener: MediaPlayerListener?) {
        mediaPlayerListenerRef = if (listener != null) WeakReference(listener) else null
    }

    private fun sendMessage(message: Message) {
        mainHandler.sendMessage(message)
    }
}