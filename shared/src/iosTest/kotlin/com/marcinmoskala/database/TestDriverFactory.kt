package com.marcinmoskala.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

actual suspend fun createTestDriver(
    schema: SqlSchema<QueryResult.AsyncValue<Unit>>
): SqlDriver = DriverFactory().createDriver(schema = schema, databaseName = "test.db")