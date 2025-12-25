package com.marcinmoskala.database

import app.cash.sqldelight.async.coroutines.await
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import kotlin.wasm.unsafe.JsFun

external interface Worker

@JsFun("() => new Worker(new URL('@cashapp/sqldelight-sqljs-worker/sqljs.worker.js', import.meta.url))")
private external fun createSqljsWorker(): Worker

actual class DriverFactory {
    actual suspend fun createDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
        databaseName: String
    ): SqlDriver = WebWorkerDriver(
        worker = createSqljsWorker()
    ).also { schema.create(it).await() }
}