package com.jw.media.lib.utils.ass

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewTreeObserver
import androidx.annotation.Keep
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 *Created by Joyce.wang on 2024/9/27 16:33
 *@Description TODO
 */
class Ass private constructor(builder: AssBuilder) : OnLayoutChangeListener {
    private val mBuilder = builder
    private var mTrack: AssTrack? = null
    private val mView: IAssView? = mBuilder.view
    private val mUiHandler = Handler(Looper.getMainLooper())
    private val mWorkHandler: Handler
    private val isFontReady = AtomicBoolean(false)
    private val isSubtitleReady = AtomicBoolean(false)

    private val rect = IntArray(2)

    init {
        val thread = HandlerThread(TAG)
        thread.start()
        mWorkHandler = Handler(thread.looper)
    }

    fun prepare() {
        mWorkHandler.post {
            if (!isFontReady.get()) fontsWork()
            if (!isSubtitleReady.get()) subtitlesWork()
        }
    }

    fun destory() {
        if (mTrack != null) mTrack.destory()
        if (mView != null && mView is View) (mView as View).removeOnLayoutChangeListener(
            this
        )
        AssBitmapPool.clear()
    }

    private fun fontsWork() {
        val fontNameMapping: HashMap<String, String> = LinkedHashMap()
        val fontsDataDir: File = File(ContextProvider.getContext().getCacheDir(), TAG)
        FileUtils.deleteDirectory(fontsDataDir.path)
        fontsDataDir.mkdir()

        if (mBuilder.fonts.isEmpty()) {
            try {
                mBuilder.fonts["default"] =
                    IOUtils.toByteArray(
                        ContextProvider.getContext().getResources().getAssets()
                            .open("lib_ass_defaut_font.ttf")
                    )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        for ((key, value) in mBuilder.fonts) {
            fontNameMapping[key] = key
            Utils.storeToCache(fontsDataDir, value, "$key.ttf") //Todo
        }
        setFontDirectory(fontsDataDir, fontNameMapping)
        isFontReady.set(true)
    }

    private fun subtitlesWork() {
        if (mView.getWidth() > 0 || mView.getHeight() > 0) {
            mTrack = convert(
                mBuilder.content,
                mBuilder.content.size,
                mBuilder.view.getWidth(),
                mBuilder.view.getHeight()
            )
            if (mTrack == null) {
                Log.w(TAG, "Ass create track fail !!!! ")
                return
            }
            isSubtitleReady.set(true)
            rect[0] = mView.getWidth()
            rect[1] = mView.getHeight()
            if (mView is View) (mView as View).addOnLayoutChangeListener(
                this
            )
        } else if (mView is View) {
            (mView as View?)!!.viewTreeObserver.addOnPreDrawListener(object :
                ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    (mView as View?)!!.viewTreeObserver.removeOnPreDrawListener(this)
                    mWorkHandler.post { this@Ass.subtitlesWork() }
                    return false
                }
            })
        } else {
            throw RuntimeException("can not get valid resolution")
        }
    }

    fun onTimeStamp(ms: Long) {
        if (!isFontReady.get() || !isSubtitleReady.get()) {
            prepare()
            Log.w(TAG, "Ass not ready !!!! ")
            return
        }

        mWorkHandler.post {
            val images: Array<AssImage> = getImage(mTrack, ms)
            mUiHandler.post {
                mView.render(images)
            }
        }
    }

    private fun setFontDirectory(fontDirectoryPath: File, fontNameMapping: Map<String, String>?) {
        var validFontNameMappingCount = 0

        val fontConfigurationPath = File(fontDirectoryPath, "fonts.conf")
        if (fontConfigurationPath.exists()) {
            val fontConfigurationDeleted = fontConfigurationPath.delete()
            Log.d(
                TAG,
                String.format(
                    "Deleted old temporary font configuration: %s.",
                    fontConfigurationDeleted
                )
            )
        }

        /* PROCESS MAPPINGS FIRST */
        val fontNameMappingBlock = StringBuilder("")
        if (fontNameMapping != null && (fontNameMapping.size > 0)) {
            fontNameMapping.entries
            for ((fontName, mappedFontName) in fontNameMapping) {
                if ((fontName != null) && (mappedFontName != null) && (fontName.trim { it <= ' ' }.length > 0) && (mappedFontName.trim { it <= ' ' }.length > 0)) {
                    fontNameMappingBlock.append("        <match target=\"pattern\">\n")
                    fontNameMappingBlock.append("                <test qual=\"any\" name=\"family\">\n")
                    fontNameMappingBlock.append(
                        String.format(
                            "                        <string>%s</string>\n",
                            fontName
                        )
                    )
                    fontNameMappingBlock.append("                </test>\n")
                    fontNameMappingBlock.append("                <edit name=\"family\" mode=\"assign\" binding=\"same\">\n")
                    fontNameMappingBlock.append(
                        String.format(
                            "                        <string>%s</string>\n",
                            mappedFontName
                        )
                    )
                    fontNameMappingBlock.append("                </edit>\n")
                    fontNameMappingBlock.append("        </match>\n")

                    validFontNameMappingCount++
                }
            }
        }

        val fontConfig = """<?xml version="1.0"?>
<!DOCTYPE fontconfig SYSTEM "fonts.dtd">
<fontconfig>
    <dir>.</dir>
    <dir>$fontDirectoryPath</dir>
$fontNameMappingBlock</fontconfig>"""

        val reference = AtomicReference<FileOutputStream?>()
        try {
            val outputStream = FileOutputStream(fontConfigurationPath)
            reference.set(outputStream)

            outputStream.write(fontConfig.toByteArray())
            outputStream.flush()

            Log.d(
                TAG,
                String.format(
                    "Saved new temporary font configuration with %d font name mappings.",
                    validFontNameMappingCount
                )
            )

            setFontconfigConfigurationPath(fontDirectoryPath.absolutePath)

            Log.d(
                TAG,
                String.format("Font directory %s registered successfully.", fontDirectoryPath)
            )
        } catch (e: IOException) {
            Log.e(TAG, String.format("Failed to set font directory: %s.", fontDirectoryPath), e)
        } finally {
            if (reference.get() != null) {
                try {
                    reference.get()!!.close()
                } catch (e: IOException) {
                    // DO NOT PRINT THIS ERROR
                }
            }
        }
    }

    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        if (rect[0] != mView.getWidth() || rect[1] != mView.getHeight()) {
            isSubtitleReady.set(false)
            if (mTrack != null) mTrack.destory()
            mTrack = null
            prepare()
        }
    }

    class AssBuilder {
        var view: IAssView? = null
        var content: ByteArray
        val fonts: HashMap<String, ByteArray> = LinkedHashMap()

        fun setTarget(view: IAssView?): AssBuilder {
            this.view = view
            return this
        }

        fun setAssContent(content: ByteArray): AssBuilder {
            this.content = content
            return this
        }

        fun addTTF(font: ByteArray, fontName: String): AssBuilder {
            fonts[fontName] = font
            return this
        }

        fun build(): Ass {
            return Ass(this)
        }
    }

    companion object {
        init {
            try {
                System.loadLibrary("assAndroid")
            } catch (e: Exception) {
                e.toString()
            }
        }

        private const val TAG = "libAss"
        private const val FONTCONFIG_ENV_VARIABLE = "FONTCONFIG_PATH"
        @Keep
        private fun tear(obj: Any) {
            obj.toString()
        }


        fun setFontconfigConfigurationPath(path: String): Int {
            return setNativeEnvironmentVariable(FONTCONFIG_ENV_VARIABLE, path)
        }

        private external fun setNativeEnvironmentVariable(
            variableName: String,
            variableValue: String
        ): Int

        private external fun convert(
            buffer: ByteArray,
            length: Int,
            resolutionX: Int,
            resolutionY: Int
        ): AssTrack?

        private external fun getImage(track: AssTrack?, ms: Long): Array<AssImage>

        private external fun addFont(name: String, buffer: ByteArray, length: Int)

        private external fun setNativeDebug(ref: Boolean)

        fun setDebug(ref: Boolean) {
            setNativeDebug(ref)
            AssBitmapPool.getInstance().lbp.isDebug = ref
        }
    }
}