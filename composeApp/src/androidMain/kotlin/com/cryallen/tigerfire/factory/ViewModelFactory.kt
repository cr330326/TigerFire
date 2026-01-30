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
 * ✅ 修复：为每个 ViewModel 创建独立的协程作用域，
 * 确保 ViewModel 销毁时协程可以被正确取消，防止内存泄露。
 *
 * 设计原则：
 * - 数据库和 Repository 全局共享（单例）
 * - 每个 ViewModel 拥有独立的协程作用域
 * - AppSessionManager 使用全局作用域（跨页面）
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
     * ✅ 修复：全局会话管理器专用作用域
     * 用于管理跨页面的会话计时，需要在整个应用生命周期中保持运行
     */
    private val globalSessionScope = CoroutineScope()

    /**
     * ✅ 修复：全局会话管理器（使用全局作用域）
     * 使用下划线前缀避免与 getAppSessionManager() 函数冲突
     */
    private val _appSessionManager: AppSessionManager by lazy {
        AppSessionManager.getInstance(
            scope = globalSessionScope,
            progressRepository = progressRepository
        )
    }

    // ==================== ViewModel 创建方法 ====================
    // ✅ 修复：每个 ViewModel 创建时分配新的独立作用域

    /**
     * 创建 WelcomeViewModel
     * ✅ 修复：使用独立的协程作用域
     */
    fun createWelcomeViewModel(): WelcomeViewModel {
        return WelcomeViewModel(
            viewModelScope = CoroutineScope()  // ✅ 独立作用域
        )
    }

    /**
     * 创建 MapViewModel
     * ✅ 修复：使用独立的协程作用域
     */
    fun createMapViewModel(): MapViewModel {
        return MapViewModel(
            viewModelScope = CoroutineScope(),  // ✅ 独立作用域
            progressRepository = progressRepository
        )
    }

    /**
     * 创建 FireStationViewModel
     * ✅ 修复：使用独立的协程作用域
     */
    fun createFireStationViewModel(): FireStationViewModel {
        val resourcePathProvider = ResourcePathProvider()
        return FireStationViewModel(
            viewModelScope = CoroutineScope(),  // ✅ 独立作用域
            progressRepository = progressRepository,
            resourcePathProvider = resourcePathProvider
        )
    }

    /**
     * 创建 SchoolViewModel
     * ✅ 修复：使用独立的协程作用域
     */
    fun createSchoolViewModel(): SchoolViewModel {
        val resourcePathProvider = ResourcePathProvider()
        return SchoolViewModel(
            viewModelScope = CoroutineScope(),  // ✅ 独立作用域
            progressRepository = progressRepository,
            resourcePathProvider = resourcePathProvider
        )
    }

    /**
     * 创建 ForestViewModel
     * ✅ 修复：使用独立的协程作用域
     */
    fun createForestViewModel(): ForestViewModel {
        val resourcePathProvider = ResourcePathProvider()
        return ForestViewModel(
            viewModelScope = CoroutineScope(),  // ✅ 独立作用域
            progressRepository = progressRepository,
            resourcePathProvider = resourcePathProvider
        )
    }

    /**
     * 创建 CollectionViewModel
     * ✅ 修复：使用独立的协程作用域
     */
    fun createCollectionViewModel(): CollectionViewModel {
        return CollectionViewModel(
            viewModelScope = CoroutineScope(),  // ✅ 独立作用域
            progressRepository = progressRepository
        )
    }

    /**
     * 创建 ParentViewModel
     * ✅ 修复：使用独立的协程作用域
     */
    fun createParentViewModel(): ParentViewModel {
        return ParentViewModel(
            viewModelScope = CoroutineScope(),  // ✅ 独立作用域
            progressRepository = progressRepository
        )
    }

    // ==================== 共享资源访问方法 ====================

    /**
     * 创建 ProgressRepository（供其他 ViewModel 使用）
     * 注意：返回的是全局共享的实例
     */
    fun createProgressRepository(): ProgressRepository {
        return progressRepository
    }

    /**
     * ✅ 已废弃：不再提供共享的协程作用域
     * 每个 ViewModel 应该使用自己的独立作用域
     *
     * @deprecated 使用 ViewModel 的独立作用域替代
     */
    @Deprecated(
        "Each ViewModel should have its own CoroutineScope",
        ReplaceWith("CoroutineScope()")
    )
    fun createCoroutineScope(): CoroutineScope {
        return CoroutineScope()
    }

    /**
     * 获取全局应用会话管理器
     * ✅ 返回单例实例，使用全局作用域
     */
    fun getAppSessionManager(): AppSessionManager {
        return _appSessionManager
    }

    /**
     * 释放资源
     *
     * 应在应用退出时调用
     * ✅ 修复：取消全局会话作用域
     */
    fun release() {
        globalSessionScope.cancel()  // ✅ 取消全局会话协程
        sqlDriver.close()            // 关闭数据库连接
        AppSessionManager.clearInstance()  // 清理会话管理器单例
    }

    /**
     * 获取 UseCase 工厂
     *
     * 提供对 UseCase 层的访问，用于封装业务逻辑
     *
     * @return UseCaseFactory UseCase 工厂实例
     */
    fun getUseCaseFactory(): UseCaseFactory {
        return UseCaseFactory(progressRepository)
    }

    /**
     * 获取 UseCases 容器
     *
     * 提供对所有 UseCase 的便捷访问
     *
     * @return UseCases UseCase 容器实例
     */
    fun getUseCases(): UseCases {
        return UseCases.fromFactory(getUseCaseFactory())
    }
}
