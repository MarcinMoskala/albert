package com.marcinmoskala.albert.di

import com.marcinmoskala.database.DeferredUserProgressLocalClient
import com.marcinmoskala.database.InMemoryUserProgressLocalClient
import com.marcinmoskala.database.UserProgressLocalClient
import com.marcinmoskala.database.UserProgressRecord
import com.marcinmoskala.database.toApi
import com.marcinmoskala.database.toRecord
import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val DATABASE_NAME = "albert_progress"
private const val DATABASE_VERSION: Short = 1
private const val STORE_NAME = "progress"

private suspend fun dynamicAwait(request: dynamic): Any? = suspendCancellableCoroutine { cont ->
    request.onsuccess = { cont.resume(request.result) }
    request.onerror =
        { cont.resumeWithException(Throwable(request.error?.toString() ?: "IDB error")) }
}

private suspend fun openDatabase(): Any? {
    val indexedDB =
        window.asDynamic().indexedDB ?: throw IllegalStateException("IndexedDB not supported")
    val openRequest = indexedDB.open(DATABASE_NAME, DATABASE_VERSION)
    openRequest.onupgradeneeded = { event: dynamic ->
        val db = event.target.result
        if (db.objectStoreNames.contains(STORE_NAME).not()) {
            db.createObjectStore(STORE_NAME)
        }
    }
    return dynamicAwait(openRequest)
}

private class IndexedDbUserProgressClient(
    private val db: Any?,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : UserProgressLocalClient {

    private fun store(readWrite: Boolean): dynamic {
        val mode = if (readWrite) "readwrite" else "readonly"
        return db.asDynamic().transaction(arrayOf(STORE_NAME), mode).objectStore(STORE_NAME)
    }

    private fun key(record: UserProgressRecord): String =
        "${'$'}{record.userId}:${'$'}{record.stepId}"
    private fun key(userId: String, stepId: String): String = "${'$'}userId:${'$'}stepId"

    override suspend fun upsert(record: UserProgressRecord) {
        val encoded = json.encodeToString(record.toApi())
        val s = store(true)
        dynamicAwait(s.put(encoded, key(record)))
    }

    override suspend fun get(userId: String, stepId: String): UserProgressRecord? {
        val s = store(false)
        val raw = dynamicAwait(s.get(key(userId, stepId)))
        val value = raw as? String ?: return null
        return runCatching {
            json.decodeFromString<com.marcinmoskala.model.UserProgressApi>(value).toRecord()
        }.getOrNull()
    }

    override suspend fun getAllForUser(userId: String): List<UserProgressRecord> =
        getAll().filter { it.userId == userId }

    override suspend fun getAll(): List<UserProgressRecord> {
        val s = store(false)
        val rawList = dynamicAwait(s.getAll())
        val arr = rawList as? Array<Any?> ?: emptyArray()
        return arr.mapNotNull { item ->
            (item as? String)?.let {
                runCatching {
                    json.decodeFromString<com.marcinmoskala.model.UserProgressApi>(it).toRecord()
                }.getOrNull()
            }
        }
    }

    override suspend fun delete(userId: String, stepId: String) {
        val s = store(true)
        dynamicAwait(s.delete(key(userId, stepId)))
    }
}

private suspend fun createUserProgressClient(): UserProgressLocalClient {
    return runCatching { openDatabase() }.mapCatching { db -> IndexedDbUserProgressClient(db) }
        .getOrElse { InMemoryUserProgressLocalClient() }
}

val platformModule = module {
    single<UserProgressLocalClient> {
        DeferredUserProgressLocalClient(
            createClient = { createUserProgressClient() },
            fallbackClientProvider = { InMemoryUserProgressLocalClient() }
        )
    }
}
