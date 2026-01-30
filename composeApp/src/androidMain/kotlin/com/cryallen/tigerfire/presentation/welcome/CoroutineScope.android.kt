package com.cryallen.tigerfire.presentation.welcome

import kotlinx.coroutines.CoroutineScope as KotlinCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Android 平台的 CoroutineScope 实现
 *
 * 将 ViewModel 的协程作用域绑定到 Android 的 Main dispatcher
 *
 * ✅ 修复：添加 Job 管理，确保协程可以被正确取消
 */
actual class CoroutineScope {

    private val job = SupervisorJob()
    private val scope = KotlinCoroutineScope(job + Dispatchers.Main)

    actual fun launch(block: suspend () -> Unit) {
        scope.launch {
            block()
        }
    }

    /**
     * 取消协程作用域中的所有协程
     *
     * 应在 ViewModel 不再需要时调用，防止内存泄漏
     *
     * ✅ 修复：立即取消所有子协程，无需等待
     */
    actual fun cancel() {
        job.cancel()  // ✅ 直接取消 Job，立即生效
        scope.cancel()
    }
}
