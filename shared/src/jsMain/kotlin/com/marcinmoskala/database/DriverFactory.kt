package com.marcinmoskala.database

import app.cash.sqldelight.async.coroutines.await
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker
import kotlin.js.JsName

@JsName("DriverFactory")
actual class DriverFactory {
    actual suspend fun createDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
        databaseName: String
    ): SqlDriver = WebWorkerDriver(
        worker = Worker(js("new URL('@cashapp/sqldelight-sqljs-worker/sqljs.worker.js', import.meta.url)"))
    ).also { schema.create(it).await() }
}