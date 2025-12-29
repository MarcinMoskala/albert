package com.marcinmoskala.database

import com.marcinmoskala.model.UserCourseProgressApi
import com.marcinmoskala.model.UserProgressApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class ProgressSynchronizerTest {

    @Test
    fun `synchronize keeps all records and prefers newer updates`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val localClient = FakeUserProgressLocalClient()

        val localRecords = listOf(
            record(
                userId = "user-1",
                stepId = "step-1",
                updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
            ),
            record(
                userId = "user-1",
                stepId = "step-2",
                updatedAt = Instant.parse("2024-01-02T00:00:00Z"),
            ),
            record(
                userId = "user-2",
                stepId = "step-1",
                updatedAt = Instant.parse("2024-01-03T00:00:00Z"),
            )
        )
        localClient.records.addAll(localRecords)

        val remoteProgress = UserCourseProgressApi(
            progress = listOf(
                remoteApi(
                    userId = "user-1",
                    stepId = "step-1",
                    updatedAt = Instant.parse("2024-01-05T00:00:00Z"),
                ), // newer -> replace
                remoteApi(
                    userId = "user-1",
                    stepId = "step-2",
                    updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
                ), // older -> ignore
                remoteApi(
                    userId = "user-1",
                    stepId = "step-3",
                    updatedAt = Instant.parse("2024-01-04T00:00:00Z"),
                ), // new -> add
                remoteApi(
                    userId = "user-3",
                    stepId = "step-1",
                    updatedAt = Instant.parse("2024-01-06T00:00:00Z"),
                ), // new user -> add
            )
        )

        val synchronizer = ProgressSynchronizer(
            userProgressLocalClient = localClient,
            ioDispatcher = dispatcher,
        )

        synchronizer.synchronizeWithRemote(remoteProgress)

        val recordsByKey = localClient.records.associateBy { it.userId to it.stepId }

        val expectedRecords = listOf(
            record(
                userId = "user-1",
                stepId = "step-1",
                updatedAt = Instant.parse("2024-01-05T00:00:00Z"),
            ),
            record(
                userId = "user-1",
                stepId = "step-2",
                updatedAt = Instant.parse("2024-01-02T00:00:00Z"),
            ),
            record(
                userId = "user-2",
                stepId = "step-1",
                updatedAt = Instant.parse("2024-01-03T00:00:00Z"),
            ),
            record(
                userId = "user-1",
                stepId = "step-3",
                updatedAt = Instant.parse("2024-01-04T00:00:00Z"),
            ),
            record(
                userId = "user-3",
                stepId = "step-1",
                updatedAt = Instant.parse("2024-01-06T00:00:00Z"),
            ),
        ).associateBy { it.userId to it.stepId }

        assertEquals(expectedRecords.keys, recordsByKey.keys)
        expectedRecords.forEach { (key, expectedRecord) ->
            assertEquals(expectedRecord, recordsByKey[key])
        }
    }

    private fun record(
        userId: String,
        stepId: String,
        updatedAt: Instant,
        status: UserProgressStatus = UserProgressStatus.REPEATING,
    ): UserProgressRecord = UserProgressRecord(
        userId = userId,
        stepId = stepId,
        status = status,
        createdAt = updatedAt,
        updatedAt = updatedAt,
        reviewAt = null,
        lastIntervalDays = null,
    )

    private fun remoteApi(
        userId: String,
        stepId: String,
        updatedAt: Instant,
        status: UserProgressStatus = UserProgressStatus.REPEATING,
    ): UserProgressApi = UserProgressApi(
        userId = userId,
        stepId = stepId,
        status = status,
        createdAt = updatedAt.toString(),
        updatedAt = updatedAt.toString(),
        reviewAt = null,
        lastIntervalDays = null,
    )
}

private class FakeUserProgressLocalClient : UserProgressLocalClient {
    val records = mutableListOf<UserProgressRecord>()

    override suspend fun upsert(record: UserProgressRecord) {
        records.removeAll { it.userId == record.userId && it.stepId == record.stepId }
        records.add(record)
    }

    override suspend fun get(userId: String, stepId: String): UserProgressRecord? =
        records.firstOrNull { it.userId == userId && it.stepId == stepId }

    override suspend fun getAllForUser(userId: String): List<UserProgressRecord> =
        records.filter { it.userId == userId }

    override suspend fun getAll(): List<UserProgressRecord> = records.toList()

    override suspend fun delete(userId: String, stepId: String) {
        records.removeAll { it.userId == userId && it.stepId == stepId }
    }
}
