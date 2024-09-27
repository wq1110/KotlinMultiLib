package com.jw.common.common.kotlinmultilib.player.base

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.view.Surface
import com.jw.media.lib.utils.provider.ContextProvider
import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.misc.IMediaDataSource
import java.io.File

/**
 *Created by Joyce.wang on 2024/9/6 15:27
 *@Description IjkExoMediaPlayer manager (EXO Player)
 */
class ExoPlayerManager : IPlayerManager {
    companion object {
        private val TAG: String = ExoPlayerManager::class.java.simpleName

        //exo对应的tracktype，跟ijk，amp有区别，待播放器统一起来
        private val EXO_TRACK_INFO_VIDEO: Int = 0
        private val EXO_TRACK_INFO_AUDIO: Int = 1
        private val EXO_TRACK_INFO_SUBTITLE: Int = 2
    }

    private var mediaPlayer: IjkExoMediaPlayer? = null
    private var surface: Surface? = null
    override fun getMediaPlayer(): IMediaPlayer? {
        return mediaPlayer
    }

    override fun initVideoPlayer(context: Context, config: InitializationPlayerConfig) {
        mediaPlayer = IjkExoMediaPlayer(context)
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            mediaPlayer!!.setLooping(config.isLooping())
            //set url
            val uri = Uri.parse(config.getUrl())
            setDataSource(uri, config.getHeaders())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Sets the data source for the media player based on the URI and headers.
     * This method handles different URI schemes and Android versions for compatibility.
     *
     * @param uri     The URI of the video to play.
     * @param headers Optional headers to include in the request.
     */
    @Throws(java.lang.Exception::class)
    private fun setDataSource(uri: Uri, headers: Map<String, String>?) {
        val scheme = uri.scheme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            (TextUtils.isEmpty(scheme) || scheme.equals("file", ignoreCase = true))
        ) {
            val dataSource: IMediaDataSource = FileMediaDataSource(File(uri.toString()))
            mediaPlayer?.setDataSource(dataSource)
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mediaPlayer?.setDataSource(ContextProvider.getContext(), uri, headers)
        } else {
            mediaPlayer?.dataSource = uri.toString()
        }
    }

    override fun start() {
        mediaPlayer?.start()
    }

    override fun stop() {
        mediaPlayer?.stop()
    }

    override fun pause() {
        mediaPlayer?.pause()
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
        return mediaPlayer?.duration ?: -1
    }

    override fun getVideoSarNum(): Int {
        return mediaPlayer?.videoSarNum ?: 0
    }

    override fun getVideoSarDen(): Int {
        return mediaPlayer?.videoSarDen ?: 0
    }

    override fun setDisplay(surface: Surface?) {
        if (surface == null && mediaPlayer != null) {
            mediaPlayer!!.setSurface(null)
        } else if (surface != null) {
            this.surface = surface
            if (mediaPlayer != null && surface.isValid) {
                mediaPlayer!!.setSurface(surface)
            }
        }
    }

    override fun setNeedMute(needMute: Boolean) {
        if (mediaPlayer != null) {
            if (needMute) {
                mediaPlayer!!.setVolume(0f, 0f)
            } else {
                mediaPlayer!!.setVolume(1f, 1f)
            }
        }
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        mediaPlayer?.setVolume(leftVolume, rightVolume)
    }

    override fun releaseSurface() {
        if (surface != null) {
            //surface!!.release()
            surface = null
        }
    }

    override fun release() {
        if (mediaPlayer != null) {
            mediaPlayer!!.setSurface(null)
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }
}