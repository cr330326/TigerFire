package com.cryallen.tigerfire

import android.app.Application
import android.util.Log
import com.cryallen.tigerfire.domain.repository.CrashLogger

/**
 * TigerFire Application 类
 *
 * 负责初始化应用级别的组件，包括：
 * - 崩溃日志系统
 */
class TigerFireApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "TigerFire Application starting...")

        // 初始化崩溃日志系统
        initializeCrashLogger()

        Log.i(TAG, "TigerFire Application initialized")
    }

    /**
     * 初始化崩溃日志系统
     */
    private fun initializeCrashLogger() {
        try {
            val crashLogger = CrashLogger.initialize(this)
            Log.i(TAG, "CrashLogger initialized successfully")
            Log.i(TAG, "Logs directory: /data/data/$packageName/files/crash_logs/")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize CrashLogger", e)
        }
    }

    companion object {
        private const val TAG = "TigerFireApp"
    }
}
