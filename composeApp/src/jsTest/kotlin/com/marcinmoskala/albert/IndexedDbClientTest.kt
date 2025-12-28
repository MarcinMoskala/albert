package com.marcinmoskala.albert

import com.marcinmoskala.albert.di.IndexedDbUserProgressClient
import com.marcinmoskala.albert.di.createUserProgressClient
import com.marcinmoskala.database.UserProgressRecord
import com.marcinmoskala.database.UserProgressStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.js.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class IndexedDbClientTest {
    @Test
    fun records_are_persisted_round_trip() = runTest {
        val client = createUserProgressClient()
        if (client !is IndexedDbUserProgressClient) return@runTest // skip when IndexedDB unsupported

        val now: Instant = Instant.fromEpochMilliseconds(Date.now().toLong())
        val record = UserProgressRecord(
            userId = "user1",
            stepId = "step1",
            status = UserProgressStatus.COMPLETED,
            createdAt = now,
            updatedAt = now,
            reviewAt = null,
            lastIntervalDays = null
        )

        client.delete(record.userId, record.stepId)
        client.upsert(record)
        val loaded = client.get(record.userId, record.stepId)
        assertNotNull(loaded)
        assertEquals(record.userId, loaded.userId)
        assertEquals(record.stepId, loaded.stepId)
        assertEquals(record.status, loaded.status)

        val all = client.getAll()
        assertEquals(true, all.any { it.userId == record.userId && it.stepId == record.stepId })
    }
}
