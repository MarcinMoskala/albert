package com.marcinmoskala.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Clock

class UserProgressDatabaseTest {
    @Test
    fun insertAndFind() = runTest {
        val driver = createTestDriver(AlbertDatabase.Schema)
        val database = createUserProgressDatabase(driver)
        val dataSource = SqlDelightUserProgressLocalClient(database)
        val now = Clock.System.now()
        val record = UserProgressRecord(
            userId = "user-1",
            stepId = "step-1",
            status = UserProgressStatus.REPEATING,
            createdAt = now,
            updatedAt = now,
            reviewAt = now,
            lastIntervalDays = 3
        )
        dataSource.upsert(record)
        val found = dataSource.get(
            userId = record.userId,
            stepId = record.stepId
        )
        assertNotNull(found)
        assertEquals(record, found)
    }
}

expect suspend fun createTestDriver(
    schema: SqlSchema<QueryResult.AsyncValue<Unit>>
): SqlDriver