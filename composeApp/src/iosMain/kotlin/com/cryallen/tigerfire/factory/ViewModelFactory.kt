package com.cryallen.tigerfire.factory

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
import platform.Foundation.NSHomeDirectory

/**
 * iOS ViewModel 工厂
 *
 * 为 iOS 平台创建 ViewModel 实例
 */
class ViewModelFactory {
    /**
     * 数据库文件名
     */
    private val databaseFileName = "TigerFire.db"

    /**
     * 平台 SQL 驱动
     */
    private val platformSqlDriver = PlatformSqlDriver()

    /**
     * SQL 驱动
     */
    private val sqlDriver = platformSqlDriver.createDriver(
        schema = TigerFireDatabase.Schema,
        name = databaseFileName
    )

    /**
     * 数据库实例（全局共享）
     */
    private val database: TigerFireDatabase = TigerFireDatabase(sqlDriver)

    /**
     * 进度仓储（全局共享）
     */
    private val progressRepository: ProgressRepository = ProgressRepositoryImpl(
        database = database
    )

    /**
     * 全局会话管理器专用作用域
     */
    private val globalSessionScope = CoroutineScope()

    /**
     * 全局会话管理器
     */
    private val _appSessionManager: AppSessionManager by lazy {
        AppSessionManager.getInstance(
            scope = globalSessionScope,
            progressRepository = progressRepository
        )
    }

    // ==================== ViewModel 创建方法 ====================

    /**
     * 创建 WelcomeViewModel
     */
    fun createWelcomeViewModel(): WelcomeViewModel {
        return WelcomeViewModel(
            viewModelScope = CoroutineScope()
        )
    }

    /**
     * 创建 MapViewModel
     */
    fun createMapViewModel(): MapViewModel {
        return MapViewModel(
            viewModelScope = CoroutineScope(),
            progressRepository = progressRepository
        )
    }

    /**
     * 创建 FireStationViewModel
     */
    fun createFireStationViewModel(): FireStationViewModel {
        val resourcePathProvider = ResourcePathProvider()
        return FireStationViewModel(
            viewModelScope = CoroutineScope(),
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
            viewModelScope = CoroutineScope(),
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
            viewModelScope = CoroutineScope(),
            progressRepository = progressRepository,
            resourcePathProvider = resourcePathProvider
        )
    }

    /**
     * 创建 CollectionViewModel
     */
    fun createCollectionViewModel(): CollectionViewModel {
        return CollectionViewModel(
            viewModelScope = CoroutineScope(),
            progressRepository = progressRepository
        )
    }

    /**
     * 创建 ParentViewModel
     */
    fun createParentViewModel(): ParentViewModel {
        return ParentViewModel(
            viewModelScope = CoroutineScope(),
            progressRepository = progressRepository
        )
    }

    // ==================== 共享资源访问方法 ====================

    /**
     * 创建 ProgressRepository
     */
    fun createProgressRepository(): ProgressRepository {
        return progressRepository
    }

    /**
     * 获取全局应用会话管理器
     */
    fun getAppSessionManager(): AppSessionManager {
        return _appSessionManager
    }

    /**
     * 释放资源
     */
    fun release() {
        globalSessionScope.cancel()
        sqlDriver.close()
        AppSessionManager.clearInstance()
    }

    /**
     * 获取 UseCase 工厂
     */
    fun getUseCaseFactory(): UseCaseFactory {
        return UseCaseFactory(progressRepository)
    }

    /**
     * 获取 UseCases 容器
     */
    fun getUseCases(): UseCases {
        return UseCases.fromFactory(getUseCaseFactory())
    }
}

/**
 * 全局 ViewModelFactory 实例
 */
val viewModelFactory = ViewModelFactory()
