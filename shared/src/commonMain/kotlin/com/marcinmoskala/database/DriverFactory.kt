package com.marcinmoskala.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

expect class DriverFactory {
    fun createDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>> = AlbertDatabase.Schema,
        databaseName: String = "albert.db"
    ): SqlDriver
}