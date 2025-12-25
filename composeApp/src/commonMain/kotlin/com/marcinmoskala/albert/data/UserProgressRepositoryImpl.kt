package com.marcinmoskala.albert.data

import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.database.UserProgressLocalClient
import com.marcinmoskala.database.UserProgressRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class UserProgressRepositoryImpl(
    private val localClient: UserProgressLocalClient,
    backgroundScope: CoroutineScope,
) : UserProgressRepository {
    private val mutex = Mutex()
    private val _progress = MutableStateFlow<Map<String, UserProgressRecord>>(emptyMap())
    override val progress: StateFlow<Map<String, UserProgressRecord>> = _progress.asStateFlow()

    init {
        backgroundScope.launch { 
            _progress.value = localClient.getAll().associateBy { record ->
                makeKey(record.userId, record.courseId, record.lessonId, record.stepId)
            }
        }
    }
    
    override suspend fun upsert(record: UserProgressRecord) = mutex.withLock {
        localClient.upsert(record)
        val key = makeKey(record.userId, record.courseId, record.lessonId, record.stepId)
        _progress.value += (key to record)
    }

    override suspend fun get(
        userId: String,
        courseId: String,
        lessonId: String,
        stepId: String
    ): UserProgressRecord? = mutex.withLock {
        val key = makeKey(userId, courseId, lessonId, stepId)
        _progress.value[key] ?: localClient.get(userId, courseId, lessonId, stepId)
            ?.also { record ->
                _progress.value = _progress.value + (key to record)
            }
    }

    override suspend fun getAll(): List<UserProgressRecord> = mutex.withLock {
        _progress.value.values.toList()
    }

    override suspend fun getAllForUser(userId: String): List<UserProgressRecord> = mutex.withLock {
        _progress.value.values.filter { it.userId == userId }
    }

    override suspend fun delete(
        userId: String,
        courseId: String,
        lessonId: String,
        stepId: String
    ) = mutex.withLock {
        localClient.delete(userId, courseId, lessonId, stepId)
        val key = makeKey(userId, courseId, lessonId, stepId)
        _progress.value -= key
    }

    override suspend fun loadAllForUser(userId: String) = mutex.withLock {
        val records = localClient.getAllForUser(userId)
        val newMap = records.associateBy { record ->
            makeKey(record.userId, record.courseId, record.lessonId, record.stepId)
        }
        _progress.value += newMap
    }

    private fun makeKey(
        userId: String,
        courseId: String,
        lessonId: String,
        stepId: String
    ): String = "$userId:$courseId:$lessonId:$stepId"
}