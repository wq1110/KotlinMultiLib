package com.jw.media.lib.utils.utils

import android.app.ActivityManager
import android.content.Context
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 *Created by Joyce.wang on 2024/9/27 10:23
 *@Description TODO
 */
class ProcessUtils {
    companion object {
        private const val PROCESS_FLAG_KEY: String = "process_flag_key"
        private const val PROCESS_RESULT_KEY: String = "process_result_key"
        private val PID_PATTERN: Pattern = Pattern.compile("pid=(\\d{1,10})")

        /**
         * 这个方法肯能会经常出现stackoverflow错误
         *
         * @param context
         * @param packageName
         * @return
         */
        fun isPackageRunning(context: Context, packageName: String): Boolean {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    ?: return false
            for (processInfo in activityManager.runningAppProcesses) {
                if (processInfo.processName != null && processInfo.processName == packageName) {
                    return true
                }
            }
            return false
        }

        fun execCmdsForResult(vararg cmds: String): String? {
            val result: ProcessUtils.ProcessResult =
                ProcessUtils.execCmds(false, 5, *cmds)
            return result.outMessaage
        }

        fun execCmdsForResult(timeout: Long, vararg cmds: String?): String? {
            val result: ProcessUtils.ProcessResult =
                ProcessUtils.execCmds(false, timeout, *cmds)
            return result.outMessaage
        }

        fun exeCommandsWithFormat(vararg cmds: String?): String? {
            val result: ProcessUtils.ProcessResult =
                ProcessUtils.execCmds(false, 5, "\n", *cmds)
            return result.outMessaage
        }

        /***
         * @param withSu  是否root身份
         * @param timeout 执行命令超时时间，单位秒
         * @param cmds    执行的命令列表
         * @return
         */
        fun execCmds(
            withSu: Boolean,
            timeout: Long,
            vararg cmds: String?
        ): ProcessUtils.ProcessResult {
            return execCmds(withSu, timeout, null, *cmds)
        }

        fun execCmds(
            withSu: Boolean,
            timeout: Long,
            separate: String?,
            vararg cmds: String
        ): ProcessUtils.ProcessResult {
            val result: ProcessUtils.ProcessResult = ProcessUtils.ProcessResult()
            try {
                val latch = CountDownLatch(1)
                val sb = StringBuilder()
                val processBuilder = ProcessBuilder(if (withSu) "su" else "sh")
                processBuilder.redirectErrorStream(true)
                val process = processBuilder.start()
                val cmdStream = DataOutputStream(process.outputStream)
                val stdout = process.inputStream
                val thread: Thread = object : Thread() {
                    override fun run() {
                        val br = BufferedReader(InputStreamReader(stdout))
                        var line: String?
                        try {
                            while ((br.readLine().also { line = it }) != null) {
                                sb.append(line)
                                if (separate != null) sb.append(separate)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } finally {
                            latch.countDown()
                            try {
                                stdout.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                thread.isDaemon = true
                thread.start()
                for (cmd in cmds) {
                    var realCmd = cmd
                    if (!cmd.endsWith("\n")) realCmd = cmd + "\n"
                    cmdStream.writeBytes(realCmd)
                }
                cmdStream.writeBytes("exit\n")
                cmdStream.flush()
                cmdStream.close()
                val execResult = process.waitFor()
                latch.await(timeout, TimeUnit.SECONDS)
                result.isSuccess = (execResult == 0)
                result.outMessaage = sb.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                result.isSuccess = false
                result.outMessaage = ""
            } catch (e: InterruptedException) {
                e.printStackTrace()
                result.isSuccess = false
                result.outMessaage = ""
            }
            return result
        }

        fun getPid(process: Process?): String {
            if (process == null) return ""
            val processStr = process.toString()
            val matcher = PID_PATTERN.matcher(processStr)
            return if (matcher.find()) {
                matcher.group(1)
            } else {
                ""
            }
        }
    }

    class ProcessResult {
        var isSuccess = false
        var outMessaage: String? = null

        fun isSuccess(): Boolean {
            return isSuccess
        }

        fun outMessaage(): String? {
            return outMessaage
        }
    }

}