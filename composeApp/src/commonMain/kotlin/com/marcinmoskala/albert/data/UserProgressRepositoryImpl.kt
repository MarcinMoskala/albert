package com.marcinmoskala.albert.data

import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.albert.domain.repository.UserRepository
import com.marcinmoskala.database.ProgressSynchronizer
import com.marcinmoskala.database.UserProgressLocalClient
import com.marcinmoskala.database.UserProgressRecord
import com.marcinmoskala.model.UserCourseProgressApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class UserProgressRepositoryImpl(
    private val localClient: UserProgressLocalClient,
    private val progressSynchronizer: ProgressSynchronizer,
    private val userRepository: UserRepository,
    backgroundScope: CoroutineScope,
) : UserProgressRepository {
    private val mutex = Mutex()
    private val _allProgress = MutableStateFlow<Map<String, UserProgressRecord>>(emptyMap())
    private val _progressForCurrentUser =
        MutableStateFlow<Map<String, UserProgressRecord>>(emptyMap())
    override val progress: StateFlow<Map<String, UserProgressRecord>> =
        _progressForCurrentUser.asStateFlow()

    val loadedJob = backgroundScope.launch {
        _allProgress.value = localClient.getAll().associateBy { record ->
            makeKey(record.userId, record.stepId)
        }
        recomputeProgressForCurrentUser()
    }

    init {
        backgroundScope.launch {
            userRepository.currentUser.collectLatest { recomputeProgressForCurrentUser() }
        }
    }

    override suspend fun upsert(record: UserProgressRecord) = mutex.withLock {
        val key = makeKey(record.userId, record.stepId)
        _allProgress.value += (key to record)
        recomputeProgressForCurrentUser()
        localClient.upsert(record)
    }

    override suspend fun get(
        userId: String,
        stepId: String
    ): UserProgressRecord? = mutex.withLock {
        val key = makeKey(userId, stepId)
        _allProgress.value[key] ?: localClient.get(userId, stepId)
            ?.also { record ->
                _allProgress.value = _allProgress.value + (key to record)
                recomputeProgressForCurrentUser()
            }
    }

    override suspend fun getAll(): List<UserProgressRecord> = mutex.withLock {
        _allProgress.value.values.toList()
    }

    override suspend fun getAllForUser(userId: String): List<UserProgressRecord> = mutex.withLock {
        _allProgress.value.values.filter { it.userId == userId }
    }

    override suspend fun delete(
        userId: String,
        stepId: String
    ) = mutex.withLock {
        localClient.delete(userId, stepId)
        val key = makeKey(userId, stepId)
        _allProgress.value -= key
        recomputeProgressForCurrentUser()
    }

    override suspend fun loadAllForUser(userId: String) = mutex.withLock {
        val records = localClient.getAllForUser(userId)
        val newMap = records.associateBy { record ->
            makeKey(record.userId, record.stepId)
        }
        _allProgress.value += newMap
        recomputeProgressForCurrentUser()
    }

    override suspend fun getProgress(userId: String, stepId: String): UserProgressRecord? {
        loadedJob.join()
        return _allProgress.value[makeKey(userId, stepId)]
    }

    override suspend fun synchronize(remote: UserCourseProgressApi) {
        progressSynchronizer.synchronizeWithRemote(remote)
        val merged = localClient.getAll()
        mutex.withLock {
            _allProgress.value = merged.associateBy { makeKey(it.userId, it.stepId) }
            recomputeProgressForCurrentUser()
        }
    }

    override suspend fun migrateProgress(fromUserId: String, toUserId: String) {
        loadedJob.join()
        mutex.withLock {
            val recordsToMigrate = _allProgress.value.values.filter { record ->
                record.userId == fromUserId
            }
            if (recordsToMigrate.isEmpty()) return@withLock

            val migratedRecords = recordsToMigrate.map { record -> record.copy(userId = toUserId) }

            val updatedMap = _allProgress.value
                .filterValues { record -> record.userId != fromUserId }
                .toMutableMap()

            for (migratedRecord in migratedRecords) {
                localClient.delete(fromUserId, migratedRecord.stepId)

                val destinationKey = makeKey(toUserId, migratedRecord.stepId)
                val existingDestinationRecord = updatedMap[destinationKey]

                val shouldUpsertMigratedRecord = when {
                    existingDestinationRecord == null -> true
                    migratedRecord.updatedAt > existingDestinationRecord.updatedAt -> true
                    migratedRecord.updatedAt < existingDestinationRecord.updatedAt -> false
                    else -> {
                        // Same timestamp - prefer the more advanced status.
                        migratedRecord.status.ordinal > existingDestinationRecord.status.ordinal
                    }
                }

                if (!shouldUpsertMigratedRecord) {
                    continue
                }

                localClient.upsert(migratedRecord)
                updatedMap[destinationKey] = migratedRecord
            }

            _allProgress.value = updatedMap.toMap()
            recomputeProgressForCurrentUser()
        }
    }

    private fun recomputeProgressForCurrentUser() {
        val activeUserId = userRepository.currentUser.value?.userId
            ?: UserProgressRepository.ANONYMOUS_USER_ID
        _progressForCurrentUser.value = _allProgress.value
            .filterValues { record -> record.userId == activeUserId }
            .mapKeys { (_, record) -> record.stepId }
    }

    private fun makeKey(
        userId: String,
        stepId: String
    ): String = "$userId:$stepId"
}