package com.jw.common.common.kotlinmultilib.player.base

import android.view.View
import com.jw.common.common.kotlinmultilib.player.base.render.VideoSizeChangeListener
import java.lang.ref.WeakReference
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.min

/**
 *Created by Joyce.wang on 2024/9/5 8:55
 *@Description Helper class to calculate the actual display width and height of a video.
 */
class VideoMeasureHelper(view: View, listener: VideoSizeChangeListener) {

    companion object {
        private val TAG: String = VideoMeasureHelper::class.java.simpleName
    }

    private var mViewRef: WeakReference<View>? = WeakReference(view)
    private var mListener: VideoSizeChangeListener? = listener

    private var mVideoWidth: Int = 0
    private var mVideoHeight: Int = 0
    private var mVideoSarNum: Int = 0//横向采样数值
    private var mVideoSarDen: Int = 0//纵向采样数值

    private var mVideoRotationDegree: Int = 0//视频旋转角度

    private var mMeasuredWidth: Int = 0//计算后的实际视频高宽
    private var mMeasuredHeight: Int = 0;//计算后的实际视频高度

    private var mCurrentAspectRatioMode: Int = VideoType.AR_ASPECT_FIT_PARENT
    private var mFullScreenExpansionPer: Float = 1.0f//AR_ASPECT_FILL_PARENT模式宽高放大百分比

    val view: View? get() {
        return mViewRef?.get()
    }

    fun setVideoSize(videoWidth: Int, videoHeight: Int) {
        mVideoWidth = videoWidth
        mVideoHeight = videoHeight
    }

    fun setVideoSampleAspectRatio(videoSarNum: Int, videoSarDen: Int) {
        mVideoSarNum = videoSarNum
        mVideoSarDen = videoSarDen
    }

    fun setVideoRotation(videoRotationDegree: Int) {
        mVideoRotationDegree = videoRotationDegree
    }

    fun setFullScreenExpansionPer(fullScreenExpansionPer: Float) {
        mFullScreenExpansionPer = fullScreenExpansionPer
    }

    fun setAspectRatioMode(aspectRatioMode: Int) {
        mCurrentAspectRatioMode = aspectRatioMode
    }

    /**
     * Must be called by View.onMeasure(int, int)
     *
     * @param widthMeasureSpec  width measure spec
     * @param heightMeasureSpec height measure spec
     */
    fun doMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        if (mVideoRotationDegree == 90 || mVideoRotationDegree == 270) {
            val tempSpec: Int = widthMeasureSpec
            widthMeasureSpec = heightMeasureSpec
            heightMeasureSpec = tempSpec
        }
        var realWidth: Int = mVideoWidth

//        if (mVideoSarNum != 0 && mVideoSarDen != 0) {
//            val pixelWidthHeightRatio: Double = mVideoSarNum / (mVideoSarDen / 1.0)
//            realWidth = (realWidth / pixelWidthHeightRatio).toInt()
//        }

        var width: Int = View.getDefaultSize(realWidth, widthMeasureSpec)
        var height: Int = View.getDefaultSize(mVideoHeight, heightMeasureSpec)

        Logger.getLogger(TAG).log(Level.INFO, String.format("doMeasure currentAspectRatioMode[%s], [%s, %s] [%s, %s] [%s, %s]",
            mCurrentAspectRatioMode, realWidth, mVideoHeight, width, height, mVideoSarNum, mVideoSarDen))

        if (mCurrentAspectRatioMode == VideoType.AR_MATCH_PARENT) {
            width = widthMeasureSpec;
            height = heightMeasureSpec;
        } else if (realWidth > 0 && mVideoHeight > 0) {
            val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec)

            Logger.getLogger(TAG).log(Level.INFO, String.format("doMeasure widthSpecSize: %s, heightSpecSize: %s", widthSpecSize, heightSpecSize))

            if (widthSpecMode == View.MeasureSpec.AT_MOST && heightSpecMode == View.MeasureSpec.AT_MOST) {
                val specAspectRatio = widthSpecSize.toFloat() / heightSpecSize.toFloat()
                var displayAspectRatio: Float
                when (mCurrentAspectRatioMode) {
                    VideoType.AR_16_9_FIT_PARENT -> {
                        displayAspectRatio = 16.0f / 9.0f
                        if (mVideoRotationDegree == 90 || mVideoRotationDegree == 270) displayAspectRatio = 1.0f / displayAspectRatio
                    }

                    VideoType.AR_4_3_FIT_PARENT -> {
                        displayAspectRatio = 4.0f / 3.0f
                        if (mVideoRotationDegree == 90 || mVideoRotationDegree == 270) displayAspectRatio = 1.0f / displayAspectRatio
                    }

                    VideoType.AR_ASPECT_FIT_PARENT, VideoType.AR_ASPECT_FILL_PARENT, VideoType.AR_ASPECT_WRAP_CONTENT -> {
                        displayAspectRatio = realWidth.toFloat() / mVideoHeight.toFloat()
                        if (mVideoSarNum > 0 && mVideoSarDen > 0) displayAspectRatio = displayAspectRatio * mVideoSarNum / mVideoSarDen
                    }

                    else -> {
                        displayAspectRatio = realWidth.toFloat() / mVideoHeight.toFloat()
                        if (mVideoSarNum > 0 && mVideoSarDen > 0) displayAspectRatio = displayAspectRatio * mVideoSarNum / mVideoSarDen
                    }
                }
                val shouldBeWider = displayAspectRatio > specAspectRatio

                Logger.getLogger(TAG).log(Level.INFO, String.format("doMeasure shouldBeWider[%s] displayAspectRatio[%s] specAspectRatio[%s] fullScreenExpansionPer[%s]",
                    shouldBeWider, displayAspectRatio, specAspectRatio, mFullScreenExpansionPer))

                when (mCurrentAspectRatioMode) {
                    VideoType.AR_ASPECT_FIT_PARENT, VideoType.AR_16_9_FIT_PARENT, VideoType.AR_4_3_FIT_PARENT -> {
                        if (shouldBeWider) {
                            // too wide, fix width
                            width = widthSpecSize
                            height = (width / displayAspectRatio).toInt()
                        } else {
                            // too high, fix height
                            height = heightSpecSize
                            width = (height * displayAspectRatio).toInt()
                        }
                    }
                    VideoType.AR_ASPECT_FILL_PARENT -> {
                        //针对视频宽高比和显示view宽高比不一致，以及有些视频本来就存在黑边问题，导致播放中没法针对所有的影片做全屏播放。
                        // 这里先这样处理（会导致上下左右画面有裁剪，显示不全），后续有问题再想其他方法解决。
                        if (shouldBeWider) {
                            // not high enough, fix height
                            height = (heightSpecSize * mFullScreenExpansionPer).toInt()
                            width = (height * displayAspectRatio).toInt()
                        } else {
                            // not wide enough, fix width
                            width = (widthSpecSize * mFullScreenExpansionPer).toInt()
                            height = (width / displayAspectRatio).toInt()
                        }
                    }

                    VideoType.AR_ASPECT_WRAP_CONTENT -> {
                        if (shouldBeWider) {
                            // too wide, fix width
                            width = min(realWidth.toDouble(), widthSpecSize.toDouble()).toInt()
                            height = (width / displayAspectRatio).toInt()
                        } else {
                            // too high, fix height
                            height = min(mVideoHeight.toDouble(), heightSpecSize.toDouble()).toInt()
                            width = (height * displayAspectRatio).toInt()
                        }
                    }

                    else -> {
                        if (shouldBeWider) {
                            // too wide, fix width
                            width = min(realWidth.toDouble(), widthSpecSize.toDouble()).toInt()
                            height = (width / displayAspectRatio).toInt()
                        } else {
                            // too high, fix height
                            height = min(mVideoHeight.toDouble(), heightSpecSize.toDouble()).toInt()
                            width = (height * displayAspectRatio).toInt()
                        }
                    }
                }
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY && heightSpecMode == View.MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize
                height = heightSpecSize

                // for compatibility, we adjust size based on aspect ratio
                if (realWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * realWidth / mVideoHeight
                } else if (realWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / realWidth
                }
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize
                height = width * mVideoHeight / realWidth
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize
                }
            } else if (heightSpecMode == View.MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize
                width = height * realWidth / mVideoHeight
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = realWidth
                height = mVideoHeight
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize
                    width = height * realWidth / mVideoHeight
                }
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize
                    height = width * mVideoHeight / realWidth
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        Logger.getLogger(TAG).log(Level.INFO, String.format("doMeasure, final measure size[%s, %s]", width, height))

        mMeasuredWidth = width
        mMeasuredHeight = height
    }

    fun prepareMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int, rotate: Int) {
        if (mListener != null) {
            try {
                val videoWidth: Int = mListener?.getCurrentVideoWidth() ?: 0
                val videoHeight: Int = mListener?.getCurrentVideoHeight() ?: 0
                val videoSarNum: Int = mListener?.getVideoSarNum() ?: 0
                val videoSarDen: Int = mListener?.getVideoSarDen() ?: 0
                val aspectRatioMode: Int = mListener?.getAspectRatioMode() ?: 0

                Logger.getLogger(TAG).log(Level.INFO, String.format("videoWidth: %s, videoHeight: %s, videoSarNum: %s, videoSarDen: %s",
                    videoWidth, videoHeight, videoSarNum, videoSarDen))

                if (videoSarNum > 0 && videoSarDen > 0) {
                    setVideoSampleAspectRatio(videoSarNum, videoSarDen)
                }
                if (videoWidth > 0 && videoHeight > 0) {
                    setVideoSize(videoWidth, videoHeight)
                }
                if (aspectRatioMode >= 0) {
                    setAspectRatioMode(aspectRatioMode)
                }
                setVideoRotation(rotate)
                doMeasure(widthMeasureSpec, heightMeasureSpec)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getMeasuredWidth(): Int {
        return mMeasuredWidth
    }

    fun getMeasuredHeight(): Int {
        return mMeasuredHeight
    }
}