package com.marcinmoskala.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DriverFactory {
    actual suspend fun createDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
        databaseName: String
    ): SqlDriver {
        // WasmJs SQLite support is experimental and has compatibility issues with the current
        // SQLDelight web worker driver implementation. The composeApp uses an in-memory
        // implementation instead via the platform module.
        error("WasmJs SQLite driver not implemented in shared module. Use platform module instead.")
    }
}
