package com.jw.common.common.kotlinmultilib.player.base

import android.content.Context
import android.media.AudioManager
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.view.Surface
import com.jw.media.lib.utils.provider.ContextProvider
import tv.danmaku.ijk.media.player.AndroidMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.misc.IMediaDataSource
import java.io.File
import java.util.logging.Logger

/**
 *Created by Joyce.wang on 2024/9/10 13:39
 *@Description TODO
 */
class SystemPlayerManager : IPlayerManager{
    companion object {
        private val TAG: String = SystemPlayerManager::class.java.simpleName
    }

    private var context: Context? = null
    private var mediaPlayer: AndroidMediaPlayer? = null
    private var surface: Surface? = null
    private var isReleased = false
    private var isPlaying = false


    override fun getMediaPlayer(): IMediaPlayer? {
        return mediaPlayer
    }

    override fun initVideoPlayer(context: Context, config: InitializationPlayerConfig) {
        this.context = context
        mediaPlayer = AndroidMediaPlayer()
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        isReleased = false
        try {
            //set url
            var uri = Uri.parse(config.getUrl())
            var scheme = uri.scheme
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (TextUtils.isEmpty(scheme) || scheme.equals("file", ignoreCase = true))
            ) {
                val dataSource: IMediaDataSource = FileMediaDataSource(File(uri.toString()))
                mediaPlayer!!.setDataSource(dataSource)
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mediaPlayer!!.setDataSource(ContextProvider.getContext(), uri, config.getHeaders())
            } else {
                mediaPlayer!!.setDataSource(uri.toString())
            }
            mediaPlayer!!.setLooping(config.isLooping())
            if (config.getSpeed().toInt() !== 1 && config.getSpeed() > 0) {
                setSpeed(config.getSpeed())
            }
        } catch (e: Exception) {
            Logger.getLogger(TAG).info("Error initializing video player.")
            e.printStackTrace()
        }
    }

    override fun start() {
        if (mediaPlayer != null) {
            mediaPlayer!!.start()
            isPlaying = true
        }
    }

    override fun stop() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            isPlaying = false
        }
    }

    override fun pause() {
        if (mediaPlayer != null) {
            mediaPlayer!!.pause()
            isPlaying = false
        }
    }

    override fun getVideoWidth(): Int {
        return mediaPlayer?.videoWidth ?: 0
    }

    override fun getVideoHeight(): Int {
        return mediaPlayer?.videoHeight ?: 0
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    override fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position)
    }

    override fun getCurrentPosition(): Long {
        return mediaPlayer?.currentPosition ?: 0
    }

    override fun getDuration(): Long {
        return mediaPlayer?.duration ?: 0
    }

    override fun getVideoSarNum(): Int {
        return mediaPlayer?.videoSarNum ?: 0
    }

    override fun getVideoSarDen(): Int {
        return mediaPlayer?.videoSarDen ?: 0
    }

    override fun setDisplay(surface: Surface?) {
        if (surface == null && mediaPlayer != null && !isReleased) {
            mediaPlayer!!.setSurface(null)
        } else if (surface != null) {
            this.surface = surface
            if (mediaPlayer != null && surface.isValid && !isReleased) {
                mediaPlayer!!.setSurface(surface)
            }
            if (!isPlaying) {
                pause()
            }
        }
    }

    override fun setNeedMute(needMute: Boolean) {
        try {
            if (mediaPlayer != null && !isReleased) {
                if (needMute) {
                    mediaPlayer!!.setVolume(0f, 0f)
                } else {
                    mediaPlayer!!.setVolume(1f, 1f)
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        mediaPlayer?.setVolume(leftVolume, rightVolume)
    }

    override fun releaseSurface() {
        if (surface != null) {
            //surface.release();
            surface = null
        }
    }

    override fun release() {
        if (mediaPlayer != null) {
            isReleased = true
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    private fun setSpeed(speed: Float) {
        if (isReleased || mediaPlayer == null || mediaPlayer!!.internalMediaPlayer == null || !mediaPlayer!!.isPlayable
        ) {
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val playbackParams = PlaybackParams()
                playbackParams.setSpeed(speed)
                mediaPlayer!!.internalMediaPlayer.setPlaybackParams(playbackParams)
            } else {
                Logger.getLogger(TAG).info("Setting playback speed is not supported on this Android version.")
            }
        } catch (e: java.lang.Exception) {
            Logger.getLogger(TAG).info("Error setting playback speed.")
            e.printStackTrace()
        }
    }
}