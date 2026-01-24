package com.cryallen.tigerfire.factory

import android.content.Context
import com.cryallen.tigerfire.data.local.PlatformSqlDriver
import com.cryallen.tigerfire.data.repository.ProgressRepositoryImpl
import com.cryallen.tigerfire.data.resource.ResourcePathProvider
import com.cryallen.tigerfire.database.TigerFireDatabase
import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.presentation.collection.CollectionViewModel
import com.cryallen.tigerfire.presentation.common.AppSessionManager
import com.cryallen.tigerfire.presentation.firestation.FireStationViewModel
import com.cryallen.tigerfire.presentation.forest.ForestViewModel
import com.cryallen.tigerfire.presentation.map.MapViewModel
import com.cryallen.tigerfire.presentation.parent.ParentViewModel
import com.cryallen.tigerfire.presentation.school.SchoolViewModel
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
     * 创建 SchoolViewModel
     */
    fun createSchoolViewModel(): SchoolViewModel {
        val resourcePathProvider = ResourcePathProvider()
        return SchoolViewModel(
            viewModelScope = coroutineScope,
            progressRepository = progressRepository,
            resourcePathProvider = resourcePathProvider
        )
    }

    /**
     * 创建 ForestViewModel
     */
    fun createForestViewModel(): ForestViewModel {
        val resourcePathProvider = ResourcePathProvider()
        return ForestViewModel(
            viewModelScope = coroutineScope,
            progressRepository = progressRepository,
            resourcePathProvider = resourcePathProvider
        )
    }

    /**
     * 创建 CollectionViewModel
     *
     * 为每个实例创建独立的 CoroutineScope，防止内存泄漏
     */
    fun createCollectionViewModel(): CollectionViewModel {
        return CollectionViewModel(
            viewModelScope = CoroutineScope(),  // 独立的 scope
            progressRepository = progressRepository
        )
    }

    /**
     * 创建 ParentViewModel
     */
    fun createParentViewModel(): ParentViewModel {
        return ParentViewModel(
            viewModelScope = coroutineScope,
            progressRepository = progressRepository
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

    /**
     * 获取全局应用会话管理器
     */
    fun getAppSessionManager(): AppSessionManager {
        return AppSessionManager.getInstance(
            scope = coroutineScope,
            progressRepository = progressRepository
        )
    }

    /**
     * 释放资源
     *
     * 应在应用退出时调用
     */
    fun release() {
        // 关闭数据库连接
        sqlDriver.close()
    }
}
