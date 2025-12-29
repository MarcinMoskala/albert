package com.marcinmoskala.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

actual class DriverFactory {
    actual fun createDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
        databaseName: String
    ): SqlDriver {
        error("JS driver is no longer supported because SQLDelight is not used on JS")
    }
}