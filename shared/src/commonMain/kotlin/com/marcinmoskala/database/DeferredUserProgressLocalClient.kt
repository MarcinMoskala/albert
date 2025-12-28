package com.marcinmoskala.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DeferredUserProgressLocalClient(
    createClient: suspend () -> UserProgressLocalClient,
    fallbackClientProvider: (() -> UserProgressLocalClient)? = null,
    initializationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : UserProgressLocalClient {
    private val clientDeferred = initializationScope.async {
        runCatching { createClient() }
            .getOrElse {
                println("Failed to create database client, using fallback")
                fallbackClientProvider?.invoke() ?: throw it
            }
    }

    private suspend fun client(): UserProgressLocalClient = clientDeferred.await()

    override suspend fun upsert(record: UserProgressRecord) {
        client().upsert(record)
    }

    override suspend fun get(userId: String, stepId: String): UserProgressRecord? =
        client().get(userId, stepId)

    override suspend fun getAllForUser(userId: String): List<UserProgressRecord> =
        client().getAllForUser(userId)

    override suspend fun getAll(): List<UserProgressRecord> = client().getAll()

    override suspend fun delete(userId: String, stepId: String) {
        client().delete(userId, stepId)
    }
}

class InMemoryUserProgressLocalClient : UserProgressLocalClient {
    private val mutex = Mutex()
    private val records = mutableListOf<UserProgressRecord>()

    override suspend fun upsert(record: UserProgressRecord) {
        mutex.withLock {
            records.removeAll { it.userId == record.userId && it.stepId == record.stepId }
            records.add(record)
        }
    }

    override suspend fun get(userId: String, stepId: String): UserProgressRecord? = mutex.withLock {
        records.firstOrNull { it.userId == userId && it.stepId == stepId }
    }

    override suspend fun getAllForUser(userId: String): List<UserProgressRecord> = mutex.withLock {
        records.filter { it.userId == userId }
    }

    override suspend fun getAll(): List<UserProgressRecord> = mutex.withLock { records.toList() }

    override suspend fun delete(userId: String, stepId: String) {
        mutex.withLock {
            records.removeAll { it.userId == userId && it.stepId == stepId }
        }
    }
}
