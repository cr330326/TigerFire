package com.cryallen.tigerfire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.cryallen.tigerfire.navigation.AppNavigation

/**
 * Android 主 Activity
 *
 * 应用的入口点，设置 Compose UI 内容
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            TigerFireTheme {
                AppNavigation()
            }
        }
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
        colorScheme = darkColorScheme(),
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
        AppNavigation(rememberNavController())
    }
}