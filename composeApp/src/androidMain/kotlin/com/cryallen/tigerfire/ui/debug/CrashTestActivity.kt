package com.cryallen.tigerfire.ui.debug

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.cryallen.tigerfire.R
import com.cryallen.tigerfire.domain.repository.CrashLoggerHelper
import com.cryallen.tigerfire.domain.repository.CrashLoggerInstance

/**
 * 崩溃测试Activity
 *
 * 仅用于调试和测试崩溃日志功能
 */
class CrashTestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 创建简单的UI布局
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val titleText = TextView(this).apply {
            text = "崩溃日志测试"
            textSize = 24f
        }
        layout.addView(titleText)

        // 测试非致命错误
        val testErrorButton = Button(this).apply {
            text = "测试：非致命错误"
            setOnClickListener {
                CrashLoggerHelper.logVideoLoadFailed(
                    videoPath = "/test/path/video.mp4",
                    reason = "Test video load failed",
                    scene = "CrashTestActivity"
                )
                Toast.makeText(this@CrashTestActivity, "已记录非致命错误", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(testErrorButton)

        // 测试真正崩溃
        val crashButton = Button(this).apply {
            text = "⚠️ 测试：真正崩溃（会杀死应用）"
            setOnClickListener {
                CrashLoggerHelper.setCurrentScene("CrashTestActivity")
                CrashLoggerHelper.setLastAction("点击强制崩溃测试")
                throw RuntimeException("测试崩溃 - 测试崩溃日志功能")
            }
        }
        layout.addView(crashButton)

        // 查看日志列表
        val viewLogsButton = Button(this).apply {
            text = "查看日志文件列表"
            setOnClickListener {
                try {
                    val logFiles = CrashLoggerInstance.getInstance().getLogFiles()
                    val message = buildString {
                        appendLine("日志文件列表 (共 ${logFiles.size} 个):")
                        logFiles.forEach { file ->
                            appendLine("- ${file.fileName} (${file.getReadableSize()})")
                        }
                    }
                    Toast.makeText(this@CrashTestActivity, message, Toast.LENGTH_LONG).show()
                    Log.d("CrashTestActivity", message)
                } catch (e: Exception) {
                    Toast.makeText(this@CrashTestActivity, "获取日志失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        layout.addView(viewLogsButton)

        // 清空日志
        val clearLogsButton = Button(this).apply {
            text = "清空所有日志"
            setOnClickListener {
                CrashLoggerInstance.getInstance().clearAllLogs()
                Toast.makeText(this@CrashTestActivity, "已清空所有日志", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(clearLogsButton)

        // 关闭按钮
        val closeButton = Button(this).apply {
            text = "关闭"
            setOnClickListener {
                finish()
            }
        }
        layout.addView(closeButton)

        setContentView(layout)
    }

    companion object {
        private const val TAG = "CrashTestActivity"

        /**
         * 创建启动Intent
         */
        fun createIntent(): Intent {
            return Intent().setClassName(
                "com.cryallen.tigerfire",
                "com.cryallen.tigerfire.ui.debug.CrashTestActivity"
            )
        }
    }
}
