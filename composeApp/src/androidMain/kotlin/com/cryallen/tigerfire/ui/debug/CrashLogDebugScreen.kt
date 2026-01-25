package com.cryallen.tigerfire.ui.debug

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cryallen.tigerfire.domain.repository.CrashLoggerInstance
import com.cryallen.tigerfire.domain.repository.CrashLoggerHelper
import kotlinx.coroutines.launch

/**
 * 调试屏幕 - 用于测试崩溃日志系统
 *
 * 此屏幕仅在 Debug 构建中可见，用于测试崩溃日志功能
 */
@Composable
fun CrashLogDebugScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var logInfo by remember { mutableStateOf("暂无日志信息") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "崩溃日志调试",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 测试按钮组
            Text(
                text = "触发崩溃测试",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    // 设置场景和操作
                    CrashLoggerHelper.setCurrentScene("DebugScreen")
                    CrashLoggerHelper.setLastAction("点击空指针崩溃测试")

                    // 触发空指针异常
                    @Suppress("UNUSED_VARIABLE")
                    val nullString: String? = null
                    try {
                        nullString!!.length
                    } catch (e: Exception) {
                        CrashLoggerHelper.logException(e, "DebugScreen", "NPE 测试")
                        Toast.makeText(context, "已记录 NPE 崩溃", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("测试：空指针异常（不崩溃）")
            }

            Button(
                onClick = {
                    CrashLoggerHelper.setCurrentScene("DebugScreen")
                    CrashLoggerHelper.setLastAction("点击索引越界测试")

                    try {
                        val list = listOf(1, 2, 3)
                        list[10]
                    } catch (e: Exception) {
                        CrashLoggerHelper.logException(e, "DebugScreen", "索引越界测试")
                        Toast.makeText(context, "已记录索引越界错误", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("测试：索引越界（不崩溃）")
            }

            Button(
                onClick = {
                    CrashLoggerHelper.setCurrentScene("DebugScreen")
                    CrashLoggerHelper.setLastAction("点击除零错误测试")

                    try {
                        val x = 10 / 0
                    } catch (e: Exception) {
                        CrashLoggerHelper.logException(e, "DebugScreen", "除零错误测试")
                        Toast.makeText(context, "已记录除零错误", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("测试：除零错误（不崩溃）")
            }

            // 真正的崩溃测试（会杀死应用）
            Button(
                onClick = {
                    CrashLoggerHelper.setCurrentScene("DebugScreen")
                    CrashLoggerHelper.setLastAction("点击强制崩溃测试")
                    throw RuntimeException("测试崩溃")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("⚠️ 测试：强制崩溃（会杀死应用）")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 非致命错误测试
            Text(
                text = "非致命错误测试",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    CrashLoggerHelper.logVideoLoadFailed(
                        videoPath = "/invalid/path/video.mp4",
                        reason = "File not found",
                        scene = "DebugScreen"
                    )
                    Toast.makeText(context, "已记录视频加载失败", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("测试：视频加载失败")
            }

            Button(
                onClick = {
                    CrashLoggerHelper.logLottieParseFailed(
                        animationPath = "/invalid/path/animation.json",
                        reason = "Invalid JSON format",
                        scene = "DebugScreen"
                    )
                    Toast.makeText(context, "已记录 Lottie 解析失败", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("测试：Lottie 解析失败")
            }

            Button(
                onClick = {
                    CrashLoggerHelper.logMemoryWarning(
                        availableMemory = 50,
                        totalMemory = 2048,
                        scene = "DebugScreen"
                    )
                    Toast.makeText(context, "已记录内存警告", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("测试：内存警告")
            }

            Button(
                onClick = {
                    CrashLoggerHelper.logResourceNotFound(
                        resourcePath = "/missing/resource.png",
                        scene = "DebugScreen"
                    )
                    Toast.makeText(context, "已记录资源未找到", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("测试：资源未找到")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 日志管理
            Text(
                text = "日志管理",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val logFiles = CrashLoggerInstance.getInstance().getLogFiles()
                            logInfo = buildString {
                                appendLine("日志文件列表 (共 ${logFiles.size} 个):")
                                appendLine()
                                logFiles.forEach { file ->
                                    appendLine("📄 ${file.fileName}")
                                    appendLine("   路径: ${file.filePath}")
                                    appendLine("   大小: ${file.getReadableSize()}")
                                    appendLine("   时间: ${file.timestamp}")
                                    appendLine()
                                }
                            }
                        } catch (e: Exception) {
                            logInfo = "获取日志失败: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("查看日志文件列表")
            }

            Button(
                onClick = {
                    CrashLoggerInstance.getInstance().cleanupOldLogs()
                    Toast.makeText(context, "已清理旧日志", Toast.LENGTH_SHORT).show()
                    // 刷新日志列表
                    coroutineScope.launch {
                        val logFiles = CrashLoggerInstance.getInstance().getLogFiles()
                        logInfo = "清理完成，剩余 ${logFiles.size} 个日志文件"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("清理旧日志（保留最新 20 个）")
            }

            Button(
                onClick = {
                    CrashLoggerInstance.getInstance().clearAllLogs()
                    Toast.makeText(context, "已清空所有日志", Toast.LENGTH_SHORT).show()
                    logInfo = "所有日志已清空"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("⚠️ 清空所有日志")
            }

            // 日志信息显示
            if (logInfo.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "日志信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = logInfo,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 返回按钮
            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("返回")
            }
        }
    }
}
