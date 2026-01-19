package com.cryallen.tigerfire.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.QueryResult

/**
 * 平台特定的 SQLDelight 驱动
 *
 * 使用 expect/actual 模式，在各平台实现不同的驱动创建逻辑
 */
expect class PlatformSqlDriver {
    /**
     * 创建 SQLDelight 驱动实例
     *
     * @param schema 数据库 schema
     * @param name 数据库名称
     * @return SqlDriver 实例
     */
    fun createDriver(schema: app.cash.sqldelight.db.SqlSchema<QueryResult.Value<Unit>>, name: String): SqlDriver
}
