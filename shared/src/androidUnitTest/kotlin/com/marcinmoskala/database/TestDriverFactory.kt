package com.marcinmoskala.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.util.Properties

actual suspend fun createTestDriver(
    schema: SqlSchema<QueryResult.AsyncValue<Unit>>
): SqlDriver = JdbcSqliteDriver(
    url = "jdbc:sqlite::memory:",
    properties = Properties(),
    schema = schema.synchronous()
)