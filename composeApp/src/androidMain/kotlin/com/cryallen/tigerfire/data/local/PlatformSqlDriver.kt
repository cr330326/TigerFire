package com.cryallen.tigerfire.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Android 平台特定的 SQLDelight 驱动实现
 *
 * @param context Android 应用上下文
 */
actual class PlatformSqlDriver(private val context: Context) {

    /**
     * 创建 Android SQLite 驱动实例
     *
     * @param schema 数据库 schema
     * @param name 数据库名称
     * @return SqlDriver 实例
     */
    actual fun createDriver(
        schema: app.cash.sqldelight.db.SqlSchema<QueryResult.Value<Unit>>,
        name: String
    ): SqlDriver {
        return AndroidSqliteDriver(
            schema = com.cryallen.tigerfire.database.TigerFireDatabase.Schema,
            context = context,
            name = name
        )
    }
}
