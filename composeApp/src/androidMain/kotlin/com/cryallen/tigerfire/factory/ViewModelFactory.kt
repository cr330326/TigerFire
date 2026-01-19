package com.cryallen.tigerfire.factory

import android.content.Context
import com.cryallen.tigerfire.data.local.PlatformSqlDriver
import com.cryallen.tigerfire.data.repository.ProgressRepositoryImpl
import com.cryallen.tigerfire.data.resource.ResourcePathProvider
import com.cryallen.tigerfire.database.TigerFireDatabase
import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.presentation.firestation.FireStationViewModel
import com.cryallen.tigerfire.presentation.map.MapViewModel
import com.cryallen.tigerfire.presentation.welcome.CoroutineScope
import com.cryallen.tigerfire.presentation.welcome.WelcomeViewModel

/**
 * ViewModel 工厂
 *
 * 提供 ViewModel 实例，负责依赖注入
 */
class ViewModelFactory(
    private val context: Context
) {
    /**
     * 平台 SQL 驱动
     */
    private val platformSqlDriver = PlatformSqlDriver(context)

    /**
     * SQL 驱动
     */
    private val sqlDriver = platformSqlDriver.createDriver(
        schema = TigerFireDatabase.Schema,
        name = "TigerFire.db"
    )

    /**
     * 数据库实例
     */
    private val database: TigerFireDatabase = TigerFireDatabase(sqlDriver)

    /**
     * 进度仓储
     */
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(
        database = database
    )

    /**
     * 协程作用域
     */
    private val coroutineScope = CoroutineScope()

    /**
     * 创建 WelcomeViewModel
     */
    fun createWelcomeViewModel(): WelcomeViewModel {
        return WelcomeViewModel(
            viewModelScope = coroutineScope
        )
    }

    /**
     * 创建 MapViewModel
     */
    fun createMapViewModel(): MapViewModel {
        return MapViewModel(
            viewModelScope = coroutineScope,
            progressRepository = progressRepository
        )
    }

    /**
     * 创建 FireStationViewModel
     */
    fun createFireStationViewModel(): FireStationViewModel {
        val resourcePathProvider = ResourcePathProvider()
        return FireStationViewModel(
            viewModelScope = coroutineScope,
            progressRepository = progressRepository,
            resourcePathProvider = resourcePathProvider
        )
    }

    /**
     * 创建 ProgressRepository（供其他 ViewModel 使用）
     */
    fun createProgressRepository(): ProgressRepository {
        return progressRepository
    }

    /**
     * 创建协程作用域（供其他 ViewModel 使用）
     */
    fun createCoroutineScope(): CoroutineScope {
        return coroutineScope
    }
}
