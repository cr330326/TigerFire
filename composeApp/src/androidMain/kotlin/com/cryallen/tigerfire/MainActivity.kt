package com.cryallen.tigerfire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.cryallen.tigerfire.factory.ViewModelFactory
import com.cryallen.tigerfire.navigation.AppNavigation
import com.cryallen.tigerfire.presentation.common.AppSessionManager

/**
 * Android 主 Activity
 *
 * 应用的入口点，设置 Compose UI 内容
 * 负责应用生命周期管理和全局会话计时
 */
class MainActivity : ComponentActivity() {

    /**
     * 全局应用会话管理器
     */
    private lateinit var appSessionManager: AppSessionManager

    /**
     * ViewModel 工厂
     */
    private lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 初始化 ViewModel 工厂
        viewModelFactory = ViewModelFactory(this)

        // 初始化全局会话管理器
        appSessionManager = AppSessionManager.getInstance(
            scope = viewModelFactory.createCoroutineScope(),
            progressRepository = viewModelFactory.createProgressRepository()
        )

        setContent {
            // 使用 Box 容器和状态触发重组，修复小米设备白屏问题
            // 参考: https://issuetracker.google.com/issues/227926002
            var isReady by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                // 延迟触发重组，确保 NavHost 正确初始化
                isReady = true
            }

            TigerFireTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isReady) {
                        AppNavigation(
                            navController = rememberNavController(),
                            viewModelFactory = viewModelFactory
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // 启动会话计时器（应用首次启动或从后台返回）
        appSessionManager.initialize()
    }

    override fun onResume() {
        super.onResume()

        // 应用从后台恢复，恢复会话计时
        appSessionManager.resumeSession()
    }

    override fun onPause() {
        super.onPause()

        // 应用进入后台，暂停会话计时
        appSessionManager.pauseSession()
    }

    override fun onDestroy() {
        super.onDestroy()

        // 应用退出，停止会话计时并记录使用时长
        appSessionManager.stopSession()

        // 清理资源
        viewModelFactory.release()
    }
}

/**
 * TigerFire 应用主题
 *
 * 使用深色主题配色方案
 */
@Composable
fun TigerFireTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = androidx.compose.ui.graphics.Color.Black
        ),
        content = content
    )
}

/**
 * Android 预览
 */
@Preview
@Composable
fun AppAndroidPreview() {
    TigerFireTheme {
        // 预览不需要实际的导航
        Text("TigerFire App Preview")
    }
}
