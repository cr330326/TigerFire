package com.cryallen.tigerfire.presentation.welcome

import kotlinx.coroutines.CoroutineScope as KotlinCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Android 平台的 CoroutineScope 实现
 *
 * 将 ViewModel 的协程作用域绑定到 Android 的 Main dispatcher
 */
actual class CoroutineScope {

    private val scope = KotlinCoroutineScope(SupervisorJob() + Dispatchers.Main)

    actual fun launch(block: suspend () -> Unit) {
        scope.launch {
            block()
        }
    }

    /**
     * 取消协程作用域中的所有协程
     *
     * 应在 ViewModel 不再需要时调用，防止内存泄漏
     */
    actual fun cancel() {
        scope.cancel()
    }
}
