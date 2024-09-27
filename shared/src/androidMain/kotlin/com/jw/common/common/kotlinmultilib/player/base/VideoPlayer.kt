package com.jw.common.common.kotlinmultilib.player.base

import android.R
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.jw.common.common.kotlinmultilib.player.base.render.MediaRenderView
import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import java.util.logging.Logger
import kotlin.math.max

/**
 *Created by Joyce.wang on 2024/9/10 14:01
 *@Description 播放view（视频回调与状态处理等相关层）
 */
class VideoPlayer : MediaRenderView, MediaPlayerListener{
    companion object {
        private val TAG: String = VideoPlayer::class.java.simpleName
    }

    protected var mContext: Context? = null

    //音频焦点的监听
    protected var mAudioManager: AudioManager? = null
    protected var mMaxVolume: Int = 0
    protected var mVideoUrl: String? = null
    protected var mHeaders: Map<String, String>? = null

    //是否是预告片
    protected var isTrailer: Boolean = false

    //是否循环播放
    protected var mLooping: Boolean = false

    //播放速度
    protected var mSpeed: Float = 1f
    protected var mSeekWhenPrepared: Long = 0 //记录寻求位置而做准备

    //是否准备完成前调用了暂停
    protected var mPauseBeforePrepared: Boolean = false
    protected var isSeeking: Boolean = false //是否正在seek任务
    protected var preferSeekPos: Long = -1

    //播放缓冲监听
    protected var mCurrentBufferPercentage: Int = 0

    //当前的播放状态
    protected var mCurrentState: PlayerStatus = PlayerStatus.STATE_IDLE
    protected var mCurrentMode: Int = VideoType.MODE_NORMAL

    //是否纯音频播放
    protected var isAudioOnly: Boolean = false

    protected var mOnPlayerEventListener: OnPlayerEventListener? = null
    protected var mOnPreparedListener: IMediaPlayer.OnPreparedListener? = null
    protected var mOnBufferingUpdateListener: IMediaPlayer.OnBufferingUpdateListener? = null
    protected var mOnErrorListener: IMediaPlayer.OnErrorListener? = null
    protected var mOnCompletionListener: IMediaPlayer.OnCompletionListener? = null
    protected var mOnInfoListener: IMediaPlayer.OnInfoListener? = null
    protected var mOnSeekCompleteListener: IMediaPlayer.OnSeekCompleteListener? = null
    protected var mMainHandler: Handler = Handler(Looper.getMainLooper())

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init(context)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
        init(context)
    }

    fun init(context: Context) {
        mContext = context
        mRenderContainer = FrameLayout(mContext!!)
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        addView(mRenderContainer, params)
        if (isInEditMode) return
        mAudioManager = mContext!!.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mMaxVolume = mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    fun attachMediaController(mediaController: View) {
        if (mRenderContainer == null) {
            return
        }
        mRenderContainer!!.removeAllViews()
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        mRenderContainer!!.addView(mediaController, params)
    }

    /**
     * @description Enters full-screen mode， 将mRenderContainer(内部包含mTextureView（或者mSurfaceView）和mController)从当前容器中移除，并添加到android.R.content中
     * @param activity The activity to enter full-screen in.
     */
    fun enterFullScreen(activity: Activity?) {
        if (activity == null || mCurrentMode == VideoType.MODE_FULL_SCREEN) {
            return
        }

        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val contentView = activity.findViewById<ViewGroup>(R.id.content)
        if (mCurrentMode == VideoType.MODE_SMALL_WINDOW) {
            contentView.removeView(mRenderContainer)
        } else {
            this.removeView(mRenderContainer)
        }

        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        contentView.addView(mRenderContainer, params)
        mCurrentMode = VideoType.MODE_FULL_SCREEN
    }

    /**
     * @description Exits full-screen mode. 移除mTextureView（或者mSurfaceView）和mController，并添加到非全屏的容器中
     * @param activity The activity to exit full-screen from.
     * @return True if successful, false otherwise.
     */
    fun exitFullScreen(activity: Activity?): Boolean {
        if (activity == null || mCurrentMode != VideoType.MODE_FULL_SCREEN) {
            return false
        }
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val contentView = activity.findViewById<ViewGroup>(R.id.content)
        contentView.removeView(mRenderContainer)
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        this.addView(mRenderContainer, params)

        mCurrentMode = VideoType.MODE_NORMAL
        return true
    }

    /**
     * @description Sets the video URL to play.
     * @param url The URL of the video.
     */
    fun setVideoUrl(url: String?) {
        setVideoUrl(url, null, false)
    }

    /**
     * @description Sets the video URL to play with custom headers.
     * @param url     The URL of the video.
     * @param headers HTTP headers to use for the request.
     */
    fun setVideoUrl(url: String?, headers: Map<String, String>?) {
        setVideoUrl(url, headers, false)
    }

    /**
     * @description Sets the video URL to play with options.
     * @param url         The URL of the video.
     * @param headers     HTTP headers to use for the request.
     * @param isAudioOnly True if only audio should be played, false otherwise.
     */
    fun setVideoUrl(
        url: String?,
        headers: Map<String, String>?,
        isAudioOnly: Boolean
    ) {
        this.mVideoUrl = url
        this.mHeaders = headers
        this.isAudioOnly = isAudioOnly
        mCurrentState = PlayerStatus.STATE_PREPARING
        mSeekWhenPrepared = 0
        mCurrentBufferPercentage = 0
        prepareVideo()
    }

    /**
     * @Description Starts video preparation.
     */
    private fun prepareVideo() {
        startPrepare()
    }

    private fun startPrepare() {
        getVideoManager().setMediaPlayerListener(this)
        setPlayerType()
        mAudioManager!!.requestAudioFocus(
            null,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )
        getVideoManager().prepare(
            mVideoUrl,
            if ((mHeaders == null)) HashMap<String, String>() else mHeaders,
            mLooping,
            mSpeed,
            isAudioOnly
        )
        updatePlayerStatus(PlayerStatus.STATE_PREPARING)
    }

    private fun setPlayerType() {
        val playerType =
            if (mOnPlayerEventListener != null) mOnPlayerEventListener!!.getPlayerType() else VideoType.PV_PLAYER__IjkMediaPlayer
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener!!.onInfoEvent(
                null,
                VideoType.MEDIA_INFO_PLAYER_TYPE,
                playerType
            )
        }
        when (playerType) {
            VideoType.PV_PLAYER__IjkExoMediaPlayer -> PlayerFactory.setPlayManager(
                ExoPlayerManager::class.java
            )

            VideoType.PV_PLAYER__AndroidMediaPlayer -> PlayerFactory.setPlayManager(
                SystemPlayerManager::class.java
            )

            VideoType.PV_PLAYER__IjkMediaPlayer -> PlayerFactory.setPlayManager(IjkPlayerManager::class.java)
            else -> PlayerFactory.setPlayManager(IjkPlayerManager::class.java)
        }
    }

    override fun onPrepared(mp: IMediaPlayer?) {
        if (mCurrentState !== PlayerStatus.STATE_PREPARING) {
            return
        }
        updatePlayerStatus(PlayerStatus.STATE_PREPARED)

        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener!!.onPreparedEvent(mp)
        }
        startAfterPrepared()
        if (mOnPreparedListener != null) mOnPreparedListener!!.onPrepared(mp)
    }

    /**
     * @description prepared成功之后会开始播放
     * @return
     */
    private fun startAfterPrepared() {
        if (mCurrentState !== PlayerStatus.STATE_PREPARED) {
            prepareVideo()
        }

        try {
            getVideoManager().start()
            updatePlayerStatus(PlayerStatus.STATE_PLAYING)

            val seekToPosition = mSeekWhenPrepared

            if (seekToPosition > 0) {
                mSeekWhenPrepared = 0
                getVideoManager().seekTo(seekToPosition)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        addRenderView()

        if (mPauseBeforePrepared) {
            pause()
            mPauseBeforePrepared = false
        }
    }

    /**
     * @description 暂停播放
     * @return
     */
    override fun pause() {
        if (mCurrentState === PlayerStatus.STATE_PREPARING) {
            mPauseBeforePrepared = true
        }
        try {
            if (isInPlaybackState()) {
                if (getVideoManager().isPlaying()) {
                    getVideoManager().pause()

                    updatePlayerStatus(PlayerStatus.STATE_PAUSED)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * @description 恢复播放
     * @return
     */
    override fun resumePlay() {
        resumePlay(true)
    }

    private fun resumePlay(isResetLastPlayTime: Boolean) {
        mPauseBeforePrepared = false
        if (isInPlaybackState()) {
            try {
                getVideoManager().start()
                updatePlayerStatus(PlayerStatus.STATE_PLAYING)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * @description 释放播放
     * @return
     */
    fun release() {
        mMainHandler.post {
            if (mRenderView != null && mRenderView!!.getRenderView() != null) {
                mRenderView!!.getRenderView()!!.visibility = GONE //解决release后，最后一帧画面依然展示问题
            }
        }
        getVideoManager().releaseMediaPlayer()
        isSeeking = false
        preferSeekPos = -1
        updatePlayerStatus(PlayerStatus.STATE_IDLE)
    }

    /**
     * @description 正常播放完成回调
     * @return
     */
    override fun onCompletion(mp: IMediaPlayer?) {
        updatePlayerStatus(PlayerStatus.STATE_COMPLETED)

        if (mRenderView != null && mRenderView!!.getRenderView() != null) {
            mRenderContainer!!.removeView(mRenderView!!.getRenderView())
        }

        mAudioManager!!.abandonAudioFocus(null)

        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener!!.onCompletionEvent(mp)
        }
        if (mOnCompletionListener != null) mOnCompletionListener!!.onCompletion(mp)
    }

    override fun onSeekComplete(mp: IMediaPlayer?) {
        isSeeking = false
        if (preferSeekPos >= 0) {
            val pos = preferSeekPos
            preferSeekPos = -1
            mMainHandler.post {
                seekTo(pos)
            }
        }

        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener!!.onSeekCompleteEvent(mp)
        }
        if (mOnSeekCompleteListener != null) mOnSeekCompleteListener!!.onSeekComplete(mp)
    }

    override fun onError(mp: IMediaPlayer?, what: Int, extra: Int) {
        Logger.getLogger(TAG).info("onError what: " + what + ", extra: " + extra)
        if (VideoType.FFP_MSG_ERROR_997 != extra && VideoType.FFP_MSG_ERROR_998 != extra) {
            updatePlayerStatus(PlayerStatus.STATE_ERROR)
        }
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener!!.onErrorEvent(mp, what, extra)
        }
        if (mOnErrorListener != null) mOnErrorListener!!.onError(mp, what, extra)
    }

    override fun onInfo(mp: IMediaPlayer?, what: Int, extra: Int) {
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener!!.onInfoEvent(mp, what, extra)
        }
        if (mOnInfoListener != null) mOnInfoListener!!.onInfo(mp, what, extra)
    }

    override fun onBufferingUpdate(mp: IMediaPlayer?, percent: Int) {
        mCurrentBufferPercentage = percent
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener!!.onBufferingUpdateEvent(mp, percent)
        }
        if (mOnBufferingUpdateListener != null) mOnBufferingUpdateListener!!.onBufferingUpdate(
            mp,
            percent
        )
    }

    /**
     * @description Update the player's status and notify listeners.
     * @param newState The new player status.
     */
    private fun updatePlayerStatus(newState: PlayerStatus) {
        if (mCurrentState !== newState) {
            mCurrentState = newState
            if (mOnPlayerEventListener != null) {
                mOnPlayerEventListener!!.setStateAndUi(mCurrentState, true)
            }
        }
    }

    /**
     * @description Get the current buffer percentage
     * @return The current buffer percentage.
     */
    fun getCurrentBufferPercentage(): Int {
        return mCurrentBufferPercentage
    }

    /**
     * @description Get the total duration of the video.
     * @return The duration in milliseconds, or -1 if an error occurs.
     */
    fun getDuration(): Long {
        try {
            if (isInPlaybackState()) {
                return getVideoManager().getDuration()
            }
        } catch (e: Exception) {
            Logger.getLogger(TAG).info("Error getting duration: " + e.message)
            e.printStackTrace()
        }
        return -1
    }

    /**
     * @description Check if the video is currently playing. (Pause will return false)
     * @return True if playing, false otherwise.
     */
    override fun isPlaying(): Boolean {
        return isInPlaybackState() && getVideoManager().isPlaying()
    }

    override fun getCurrentVideoWidth(): Int {
        return getVideoManager().getCurrentVideoWidth()
    }

    override fun getCurrentVideoHeight(): Int {
        return getVideoManager().getCurrentVideoHeight()
    }

    override fun getVideoSarNum(): Int {
        return getVideoManager().getVideoSarNum()
    }

    override fun getVideoSarDen(): Int {
        return getVideoManager().getVideoSarDen()
    }

    override fun getAspectRatioMode(): Int {
        return getVideoManager().getAspectRatioMode()
    }

    /**
     * @description Set the aspect ratio mode for video playback.
     * @param aspectRatioMode The desired aspect ratio mode.
     */
    fun setAspectRatioMode(aspectRatioMode: Int) {
        mMainHandler.post {
            if (aspectRatioMode >= 0) {
                getVideoManager().setAspectRatioMode(aspectRatioMode)
                if (mRenderView != null) {
                    mRenderView!!.requestLayout()
                }
            }
        }
    }

    /**
     * @description Set the display surface for video rendering.
     * @param surface The surface to render the video on.
     */
    override fun setDisplay(surface: Surface?) {
        getVideoManager().setDisplay(surface)
    }

    /**
     * @description Set the video aspect ratio and dimensions.
     * @param aspectRatio The desired aspect ratio.
     * @param width       The width of the video.
     * @param height      The height of the video.
     */
    fun setVideoAspectRatio(aspectRatio: Double, width: Int, height: Int) {
        mMainHandler.post {
            if (width > 0 && height > 0) {
                getVideoManager().setSelfVideoSizeFlag(true)
                getVideoManager().setCurrentVideoSize(width, height)
                if (mRenderView != null) {
                    mRenderView!!.requestLayout()
                }
            }
        }
    }

    fun setSelfVideoSizeFlag(selfVideoSizeFlag: Boolean) {
        getVideoManager().setSelfVideoSizeFlag(selfVideoSizeFlag)
    }

    override fun setNeedMute(needMute: Boolean) {
        getVideoManager().setNeedMute(needMute)
    }

    fun setTrailer(isTrailer: Boolean) {
        this.isTrailer = isTrailer
    }

    /**
     * @description Get the estimated time of cached video ahead of the current position.
     * @return The cache time in seconds.
     */
    fun getPlayerCacheTime(): Int {
        try {
            if (isInPlaybackState()) {
                val currentPosition = getCurrentPosition().toInt()
                val cachePosition = (mCurrentBufferPercentage * getDuration() / 100).toInt()
                val playerCacheTime = (cachePosition - currentPosition) / 1000
                return max(playerCacheTime.toDouble(), 0.0).toInt()
            }
        } catch (e: Throwable) {
            Logger.getLogger(TAG).info("Error getting player cache time: " + e.message)
            e.printStackTrace()
        }
        return 0
    }

    /**
     * @description Get the current playback position.
     * @return The current position in milliseconds.
     */
    fun getCurrentPosition(): Long {
        try {
            if (isInPlaybackState()) {
                if (preferSeekPos >= 0) {
                    return preferSeekPos
                }
                return getVideoManager().getCurrentPosition()
            }
        } catch (e: Exception) {
            Logger.getLogger(TAG).info("Error getting current position: " + e.message)
            e.printStackTrace()
        }
        return 0
    }

    /**
     * @description Seek to a specific position in the video.
     * @param position position The position to seek to in milliseconds.
     */
    fun seekTo(position: Long) {
        try {
            if (position < 0) {
                return
            }
            if (isInPlaybackState()) {
                if (isSeeking) {
                    //防止播放中多次点击连续seek导致回跳
                    preferSeekPos = position
                } else {
                    if (getVideoManager().getPlayer() != null && getVideoManager().getPlayer()!!
                            .getMediaPlayer() != null && getVideoManager().getPlayer()!!
                            .getMediaPlayer() !is IjkExoMediaPlayer
                    ) {
                        //exo播放器seek完成后不会回调onSeekComplete接口，会导致seek到最后，结束播放会有点延后，这里只针对ijk和amp处理。
                        isSeeking = true
                    }
                    getVideoManager().seekTo(position)
                    mSeekWhenPrepared = 0
                }
            } else {
                mSeekWhenPrepared = position
            }
        } catch (e: Exception) {
            Logger.getLogger(TAG).info("Error seekTo: " + e.message)
            e.printStackTrace()
        }
    }

    /**
     * @description Check if the player is in a playback state (not idle, error, or preparing).
     * @return True if in a playback state, false otherwise.
     */
    fun isInPlaybackState(): Boolean {
        return (mCurrentState !== PlayerStatus.STATE_ERROR && mCurrentState !== PlayerStatus.STATE_IDLE && mCurrentState !== PlayerStatus.STATE_PREPARING)
    }

    /**
     * @description Get the current playback state of the player.
     * @return The current PlayerStatus.
     */
    fun getCurrentPlayState(): PlayerStatus {
        return mCurrentState
    }

    override fun onSurfaceCreatedEvent(surface: Surface?) {
        seekTo(getCurrentPosition())
    }

    override fun onSurfaceChangedEvent(surface: Surface?, width: Int, height: Int) {
        val isValidState = mCurrentState === PlayerStatus.STATE_PLAYING
        val hasValidSize =
            (mRenderView != null && !mRenderView!!.shouldWaitForResize()) || (getCurrentVideoWidth() == width && getCurrentVideoHeight() == height)
        if (isValidState && hasValidSize) {
            val seekToPosition: Long = mSeekWhenPrepared
            if (seekToPosition > 0) {
                mSeekWhenPrepared = 0
                seekTo(seekToPosition)
            }
            resumePlay(false)
        }
    }

    override fun onSurfaceDestroyedEvent(surface: Surface?) {
        getVideoManager().releaseSurface(surface)
    }

    override fun onVideoSizeChanged() {
        val currentVideoWidth = getVideoManager().getCurrentVideoWidth()
        val currentVideoHeight = getVideoManager().getCurrentVideoHeight()
        if (currentVideoWidth != 0 && currentVideoHeight != 0 && mRenderView != null) {
            mRenderView!!.requestLayout()
        }
    }

    /**
     * 获取管理器桥接的实现
     */
    fun getVideoManager(): VideoPlayerBridge {
        return PlayerManager
    }

    fun <T> setPlayerListener(listener: T) {
        if (listener is IMediaPlayer.OnPreparedListener) {
            mOnPreparedListener = listener
        } else if (listener is IMediaPlayer.OnInfoListener) {
            mOnInfoListener = listener
        } else if (listener is IMediaPlayer.OnCompletionListener) {
            mOnCompletionListener = listener
        } else if (listener is IMediaPlayer.OnErrorListener) {
            mOnErrorListener = listener
        } else if (listener is IMediaPlayer.OnBufferingUpdateListener) {
            mOnBufferingUpdateListener = listener
        } else if (listener is IMediaPlayer.OnSeekCompleteListener) {
            mOnSeekCompleteListener = listener
        }
    }

    fun setOnPlayerEventListener(l: OnPlayerEventListener?) {
        this.mOnPlayerEventListener = l
    }

    fun onDestroy() {
        release()
    }
}