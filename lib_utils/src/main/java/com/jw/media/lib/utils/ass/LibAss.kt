package com.jw.media.lib.utils.ass

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

/**
 *Created by Joyce.wang on 2024/9/27 16:37
 *@Description TODO
 */
object LibAss {
    private const val TAG = "LibAss"

    init {
        try {
            System.loadLibrary("assdemo")
        } catch (e: Exception) {
            e.toString()
        }
    }

    fun setFontDirectory(
        context: Context,
        fontDirectoryPath: String,
        fontNameMapping: Map<String?, String?>?
    ) {
        val cacheDir = context.cacheDir
        var validFontNameMappingCount = 0

        val tempConfigurationDirectory = File(cacheDir, TAG)
        if (!tempConfigurationDirectory.exists()) {
            val tempFontConfDirectoryCreated = tempConfigurationDirectory.mkdirs()
            Log.d(
                TAG,
                String.format(
                    "Created temporary font conf directory: %s.",
                    tempFontConfDirectoryCreated
                )
            )
        }

        val fontConfiguration = File(tempConfigurationDirectory, "fonts.conf")
        if (fontConfiguration.exists()) {
            val fontConfigurationDeleted = fontConfiguration.delete()
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
            val outputStream = FileOutputStream(fontConfiguration)
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

            setFontconfigConfigurationPath(tempConfigurationDirectory.absolutePath)

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

    fun setFontconfigConfigurationPath(path: String?): Int {
        return setNativeEnvironmentVariable("FONTCONFIG_PATH", path)
    }

    external fun setNativeEnvironmentVariable(variableName: String?, variableValue: String?): Int
    external fun convert(
        buffer: ByteArray?,
        length: Int,
        resolutionX: Int,
        resolutionY: Int
    ): AssTrack?

    external fun getImage(track: AssTrack?, ms: Long): Array<AssImage?>?
    external fun addFont(name: String?, buffer: ByteArray?, length: Int)

    fun tear(obj: Any) {
        Log.v(TAG, obj.toString())
    }
}