package com.cryallen.tigerfire.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/**
 * iOS 平台特定的 SQLDelight 驱动实现
 */
actual class PlatformSqlDriver {

    /**
     * 创建 iOS Native 驱动实例
     *
     * @param schema 数据库 schema
     * @param name 数据库名称
     * @return NativeSqliteDriver 实例
     */
    actual fun createDriver(
        schema: app.cash.sqldelight.db.SqlSchema<QueryResult.Value<Unit>>,
        name: String
    ): SqlDriver {
        return NativeSqliteDriver(
            schema = schema,
            name = name
        )
    }
}
