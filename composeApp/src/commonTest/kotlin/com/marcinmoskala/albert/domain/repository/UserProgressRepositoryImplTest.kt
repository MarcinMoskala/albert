package com.marcinmoskala.albert.domain.repository

import com.marcinmoskala.albert.data.UserProgressRepositoryImpl
import com.marcinmoskala.database.UserProgressLocalClient
import com.marcinmoskala.database.UserProgressRecord
import com.marcinmoskala.database.UserProgressStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserProgressRepositoryImplTest {
    @Test
    fun shouldStartWithEmptyProgress() = runTest {
        // Given
        val localClient = FakeUserProgressLocalClient()
        val repository = UserProgressRepositoryImpl(localClient, this.backgroundScope)

        // Then
        testScheduler.advanceUntilIdle()
        assertEquals(emptyMap(), repository.progress.value)
    }

    @Test
    fun shouldUpsertRecordAndUpdateStateFlow() = runTest {
        // Given
        val localClient = FakeUserProgressLocalClient()
        val repository = UserProgressRepositoryImpl(localClient, this.backgroundScope)
        testScheduler.advanceUntilIdle()
        val record = createTestRecord()

        // When
        repository.upsert(record)

        // Then
        val progressMap = repository.progress.value
        assertEquals(1, progressMap.size)
        assertEquals(record, progressMap["user-1:course-1:lesson-1:step-1"])
        assertEquals(listOf(record), localClient.records)
    }

    @Test
    fun shouldUpdateExistingRecordInState() = runTest {
        // Given
        val localClient = FakeUserProgressLocalClient()
        val repository = UserProgressRepositoryImpl(localClient, this.backgroundScope)
        testScheduler.advanceUntilIdle()
        val record1 = createTestRecord()
        val record2 = record1.copy(status = UserProgressStatus.COMPLETED)

        // When
        repository.upsert(record1)
        repository.upsert(record2)

        // Then
        val progressMap = repository.progress.value
        assertEquals(1, progressMap.size)
        assertEquals(record2, progressMap["user-1:course-1:lesson-1:step-1"])
        assertEquals(listOf(record2), localClient.records)
        assertEquals(2, localClient.upsertCallCount)
    }

    @Test
    fun shouldGetRecordFromStateFlow() = runTest {
        // Given
        val localClient = FakeUserProgressLocalClient()
        val repository = UserProgressRepositoryImpl(localClient, this.backgroundScope)
        testScheduler.advanceUntilIdle()
        val record = createTestRecord()
        repository.upsert(record)

        // When
        val result = repository.get("user-1", "course-1", "lesson-1", "step-1")

        // Then
        assertEquals(record, result)
    }

    @Test
    fun shouldGetRecordFromDatabaseAndAddToState() = runTest {
        // Given
        val record = createTestRecord()
        val localClient = FakeUserProgressLocalClient()
        localClient.records.add(record)
        val repository = UserProgressRepositoryImpl(localClient, this.backgroundScope)
        testScheduler.advanceUntilIdle()

        // When
        val result = repository.get("user-1", "course-1", "lesson-1", "step-1")

        // Then
        assertEquals(record, result)
        assertEquals(1, repository.progress.value.size)
        assertEquals(record, repository.progress.value["user-1:course-1:lesson-1:step-1"])
    }

    @Test
    fun shouldReturnNullWhenRecordNotFound() = runTest {
        // Given
        val localClient = FakeUserProgressLocalClient()
        val repository = UserProgressRepositoryImpl(localClient, this.backgroundScope)
        testScheduler.advanceUntilIdle()

        // When
        val result = repository.get("user-1", "course-1", "lesson-1", "step-1")

        // Then
        assertNull(result)
    }

    @Test
    fun shouldGetAllForUser() = runTest {
        // Given
        val localClient = FakeUserProgressLocalClient()
        val repository = UserProgressRepositoryImpl(localClient, this.backgroundScope)
        testScheduler.advanceUntilIdle()
        val record1 = createTestRecord(userId = "user-1", stepId = "step-1")
        val record2 = createTestRecord(userId = "user-1", stepId = "step-2")
        val record3 = createTestRecord(userId = "user-2", stepId = "step-1")
        repository.upsert(record1)
        repository.upsert(record2)
        repository.upsert(record3)

        // When
        val result = repository.getAllForUser("user-1")

        // Then
        assertEquals(2, result.size)
        assertEquals(setOf(record1, record2), result.toSet())
    }

    @Test
    fun shouldDeleteRecordAndUpdateState() = runTest {
        // Given
        val localClient = FakeUserProgressLocalClient()
        val repository = UserProgressRepositoryImpl(localClient, this.backgroundScope)
        testScheduler.advanceUntilIdle()
        val record = createTestRecord()
        repository.upsert(record)

        // When
        repository.delete("user-1", "course-1", "lesson-1", "step-1")

        // Then
        assertEquals(emptyMap(), repository.progress.value)
        assertEquals(emptyList(), localClient.records)
    }

    @Test
    fun shouldLoadAllForUserFromDatabase() = runTest {
        // Given
        val record1 = createTestRecord(userId = "user-1", stepId = "step-1")
        val record2 = createTestRecord(userId = "user-1", stepId = "step-2")
        val record3 = createTestRecord(userId = "user-2", stepId = "step-1")
        val localClient = FakeUserProgressLocalClient()
        localClient.records.addAll(listOf(record1, record2, record3))
        val repository = UserProgressRepositoryImpl(localClient, this.backgroundScope)
        testScheduler.runCurrent() // Run the init block coroutine
        testScheduler.advanceUntilIdle()

        // When
        repository.loadAllForUser("user-1")

        // Then
        val progressMap = repository.progress.value
        // 3 from init (all records loaded), loadAllForUser doesn't add new ones since they're already there
        assertEquals(3, progressMap.size)
        assertEquals(record1, progressMap["user-1:course-1:lesson-1:step-1"])
        assertEquals(record2, progressMap["user-1:course-1:lesson-1:step-2"])
    }

    @Test
    fun shouldMaintainConsistencyBetweenStateAndDatabase() = runTest {
        // Given
        val localClient = FakeUserProgressLocalClient()
        val repository = UserProgressRepositoryImpl(localClient, this.backgroundScope)
        testScheduler.advanceUntilIdle()
        val record1 = createTestRecord(stepId = "step-1")
        val record2 = createTestRecord(stepId = "step-2")

        // When
        repository.upsert(record1)
        repository.upsert(record2)
        val retrievedRecord1 = repository.get("user-1", "course-1", "lesson-1", "step-1")
        val retrievedRecord2 = repository.get("user-1", "course-1", "lesson-1", "step-2")
        repository.delete("user-1", "course-1", "lesson-1", "step-1")

        // Then
        assertEquals(record1, retrievedRecord1)
        assertEquals(record2, retrievedRecord2)
        val finalState = repository.progress.value
        assertEquals(1, finalState.size)
        assertEquals(record2, finalState["user-1:course-1:lesson-1:step-2"])
        assertEquals(listOf(record2), localClient.records)
    }

    @Test
    fun shouldHandleMultipleUsersCorrectly() = runTest {
        // Given
        val localClient = FakeUserProgressLocalClient()
        val repository = UserProgressRepositoryImpl(localClient, this.backgroundScope)
        testScheduler.advanceUntilIdle()
        val user1Record = createTestRecord(userId = "user-1")
        val user2Record = createTestRecord(userId = "user-2")

        // When
        repository.upsert(user1Record)
        repository.upsert(user2Record)
        val user1Progress = repository.getAllForUser("user-1")
        val user2Progress = repository.getAllForUser("user-2")

        // Then
        assertEquals(listOf(user1Record), user1Progress)
        assertEquals(listOf(user2Record), user2Progress)
        assertEquals(2, repository.progress.value.size)
    }

    @Test
    fun shouldEmitStateFlowUpdates() = runTest {
        // Given
        val localClient = FakeUserProgressLocalClient()
        val repository = UserProgressRepositoryImpl(localClient, this.backgroundScope)
        testScheduler.advanceUntilIdle()
        val record = createTestRecord()

        // When
        val initialState = repository.progress.first()
        repository.upsert(record)
        val updatedState = repository.progress.first()

        // Then
        assertEquals(emptyMap(), initialState)
        assertEquals(1, updatedState.size)
        assertEquals(record, updatedState["user-1:course-1:lesson-1:step-1"])
    }

    @Test
    fun shouldGetAll() = runTest {
        // Given
        val localClient = FakeUserProgressLocalClient()
        val repository = UserProgressRepositoryImpl(localClient, this.backgroundScope)
        testScheduler.advanceUntilIdle()
        val record1 = createTestRecord(userId = "user-1", stepId = "step-1")
        val record2 = createTestRecord(userId = "user-2", stepId = "step-2")
        val record3 = createTestRecord(userId = "user-3", stepId = "step-3")
        repository.upsert(record1)
        repository.upsert(record2)
        repository.upsert(record3)

        // When
        val result = repository.getAll()

        // Then
        assertEquals(3, result.size)
        assertEquals(setOf(record1, record2, record3), result.toSet())
    }

    @Test
    fun shouldLoadAllFromDatabaseOnInit() = runTest {
        // Given
        val record1 = createTestRecord(userId = "user-1", stepId = "step-1")
        val record2 = createTestRecord(userId = "user-2", stepId = "step-2")
        val localClient = FakeUserProgressLocalClient()
        localClient.records.addAll(listOf(record1, record2))

        // When
        val repository = UserProgressRepositoryImpl(localClient, this.backgroundScope)
        testScheduler.runCurrent() // Run the init block coroutine
        testScheduler.advanceUntilIdle()

        // Then
        val progressMap = repository.progress.value
        assertEquals(2, progressMap.size)
        assertEquals(record1, progressMap["user-1:course-1:lesson-1:step-1"])
        assertEquals(record2, progressMap["user-2:course-1:lesson-1:step-2"])
    }

    private fun createTestRecord(
        userId: String = "user-1",
        courseId: String = "course-1",
        lessonId: String = "lesson-1",
        stepId: String = "step-1"
    ): UserProgressRecord {
        val now = kotlin.time.Clock.System.now()
        return UserProgressRecord(
            userId = userId,
            courseId = courseId,
            lessonId = lessonId,
            stepId = stepId,
            status = UserProgressStatus.REPEATING,
            createdAt = now,
            updatedAt = now,
            reviewAt = now,
            lastIntervalDays = 3
        )
    }
}

class FakeUserProgressLocalClient : UserProgressLocalClient {
    val records = mutableListOf<UserProgressRecord>()
    var upsertCallCount = 0

    override suspend fun upsert(record: UserProgressRecord) {
        upsertCallCount++
        records.removeAll {
            it.userId == record.userId &&
                    it.courseId == record.courseId &&
                    it.lessonId == record.lessonId &&
                    it.stepId == record.stepId
        }
        records.add(record)
    }

    override suspend fun get(
        userId: String,
        courseId: String,
        lessonId: String,
        stepId: String
    ): UserProgressRecord? = records.firstOrNull {
        it.userId == userId &&
                it.courseId == courseId &&
                it.lessonId == lessonId &&
                it.stepId == stepId
    }

    override suspend fun getAll(): List<UserProgressRecord> = records.toList()

    override suspend fun getAllForUser(userId: String): List<UserProgressRecord> =
        records.filter { it.userId == userId }

    override suspend fun delete(
        userId: String,
        courseId: String,
        lessonId: String,
        stepId: String
    ) {
        records.removeAll {
            it.userId == userId &&
                    it.courseId == courseId &&
                    it.lessonId == lessonId &&
                    it.stepId == stepId
        }
    }
}
