package com.cryallen.tigerfire.data.local

import com.cryallen.tigerfire.database.TigerFireDatabase

/**
 * 数据库工厂
 *
 * 提供统一的数据库创建接口，封装平台特定的驱动创建逻辑
 */
object DatabaseFactory {

    /**
     * 数据库名称
     */
    private const val DATABASE_NAME = "TigerFire.db"

    /**
     * 创建数据库实例
     *
     * @param platformDriver 平台特定的驱动包装器
     * @return TigerFireDatabase 实例
     */
    fun createDatabase(platformDriver: PlatformSqlDriver): TigerFireDatabase {
        val driver = platformDriver.createDriver(
            schema = TigerFireDatabase.Schema,
            name = DATABASE_NAME
        )
        return TigerFireDatabase(driver)
    }

    /**
     * 获取数据库路径（用于调试或导出）
     *
     * 注意：此方法仅在 Android 平台可用，iOS 平台返回空字符串
     *
     * @param platformDriver 平台特定的驱动包装器
     * @return 数据库文件路径，若无法获取则返回空字符串
     */
    fun getDatabasePath(platformDriver: PlatformSqlDriver): String {
        // TODO: 在平台特定实现中添加路径获取逻辑
        return ""
    }
}
