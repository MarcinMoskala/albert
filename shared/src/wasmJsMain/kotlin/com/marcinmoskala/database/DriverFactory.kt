package com.marcinmoskala.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

actual class DriverFactory {
    actual suspend fun createDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
        databaseName: String
    ): SqlDriver {
        // TODO: Implement SQLite support for WASM
        // The web-worker-driver-wasm-js library is available but requires additional setup
        // See: https://github.com/dellisd/sqldelight-sqlite-wasm for implementation examples
        // For now, use the in-memory implementation in the composeApp platform module
        error(
            "WASM SQLite driver not yet fully implemented in shared module. " +
                    "The platform module provides an in-memory fallback implementation. " +
                    "For persistent storage, see: https://github.com/dellisd/sqldelight-sqlite-wasm"
        )
    }
}
