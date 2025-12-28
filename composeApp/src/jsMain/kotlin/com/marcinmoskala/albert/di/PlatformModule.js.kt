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
import kotlin.js.console
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val DATABASE_NAME = "albert_progress"
private const val DATABASE_VERSION: Short = 1
private const val STORE_NAME = "progress"

internal suspend fun dynamicAwait(request: dynamic): Any? = suspendCancellableCoroutine { cont ->
    request.onsuccess = { cont.resume(request.result) }
    request.onerror =
        { cont.resumeWithException(Throwable(request.error?.toString() ?: "IDB error")) }
}

internal suspend fun openDatabase(): Any? {
    val indexedDB =
        window.asDynamic().indexedDB ?: throw IllegalStateException("IndexedDB not supported")
    val openRequest = indexedDB.open(DATABASE_NAME, DATABASE_VERSION)
    openRequest.onupgradeneeded = { event: dynamic ->
        val db = event.target.result
        val names = db.objectStoreNames
        val hasStore = try {
            val containsFn = names.asDynamic().contains
            when {
                containsFn != undefined && containsFn != null -> containsFn.call(
                    names,
                    STORE_NAME
                ) as Boolean

                names.length != undefined -> {
                    val length = names.length as Int
                    (0 until length).any { idx -> names[idx] == STORE_NAME }
                }

                else -> false
            }
        } catch (t: Throwable) {
            console.warn("IndexedDB store check failed: ${'$'}t")
            false
        }
        if (!hasStore) {
            db.createObjectStore(STORE_NAME)
        }
    }
    return dynamicAwait(openRequest)
}

internal class IndexedDbUserProgressClient(
    private val db: Any?,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : UserProgressLocalClient {

    private fun store(readWrite: Boolean): dynamic {
        val mode = if (readWrite) "readwrite" else "readonly"
        return db.asDynamic().transaction(arrayOf(STORE_NAME), mode).objectStore(STORE_NAME)
    }

    private fun key(record: UserProgressRecord): String = "${record.userId}:${record.stepId}"
    private fun key(userId: String, stepId: String): String = "$userId:$stepId"

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
        val jsArray = rawList.asDynamic()
        val length = (jsArray.length as? Int) ?: 0
        val items = (0 until length).mapNotNull { idx -> jsArray[idx] as? String }
        return items.mapNotNull { stored ->
            runCatching {
                json.decodeFromString<com.marcinmoskala.model.UserProgressApi>(stored).toRecord()
            }.getOrNull()
        }
    }

    override suspend fun delete(userId: String, stepId: String) {
        val s = store(true)
        dynamicAwait(s.delete(key(userId, stepId)))
    }
}

internal class LocalStorageUserProgressClient(
    private val json: Json = Json { ignoreUnknownKeys = true }
) : UserProgressLocalClient {
    private val storageKey = "user_progress_backup"
    private val mutex = Mutex()
    private var records: MutableList<UserProgressRecord> = loadFromLocalStorage()

    override suspend fun upsert(record: UserProgressRecord) {
        mutex.withLock {
            records.removeAll { it.userId == record.userId && it.stepId == record.stepId }
            records.add(record)
            persistLocked()
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
            persistLocked()
        }
    }

    private fun loadFromLocalStorage(): MutableList<UserProgressRecord> {
        val stored = window.localStorage.getItem(storageKey) ?: return mutableListOf()
        val apiList = runCatching {
            json.decodeFromString<List<com.marcinmoskala.model.UserProgressApi>>(stored)
        }.getOrNull() ?: emptyList()
        return apiList.map { it.toRecord() }.toMutableList()
    }

    private fun persistLocked() {
        val current = records.map { it.toApi() }
        window.localStorage.setItem(storageKey, json.encodeToString(current))
    }
}

internal class SafePersistingClient(
    private val primary: UserProgressLocalClient,
    private val fallback: UserProgressLocalClient
) : UserProgressLocalClient {
    private suspend fun <T> withFallback(block: suspend UserProgressLocalClient.() -> T): T {
        return runCatching { primary.block() }.getOrElse { error ->
            console.warn("Primary storage failed, falling back to localStorage: $error")
            fallback.block()
        }
    }

    override suspend fun upsert(record: UserProgressRecord) = withFallback { upsert(record) }
    override suspend fun get(userId: String, stepId: String): UserProgressRecord? =
        withFallback { get(userId, stepId) }

    override suspend fun getAllForUser(userId: String): List<UserProgressRecord> =
        withFallback { getAllForUser(userId) }

    override suspend fun getAll(): List<UserProgressRecord> = withFallback { getAll() }
    override suspend fun delete(userId: String, stepId: String) =
        withFallback { delete(userId, stepId) }
}

internal suspend fun createUserProgressClient(): UserProgressLocalClient {
    val json = Json { ignoreUnknownKeys = true }
    return runCatching { openDatabase() }
        .mapCatching { db ->
            console.log("IndexedDB ready: $DATABASE_NAME")
            SafePersistingClient(
                IndexedDbUserProgressClient(db, json),
                LocalStorageUserProgressClient(json)
            )
        }
        .getOrElse { throwable ->
            console.warn("IndexedDB unavailable, using localStorage fallback: $throwable")
            LocalStorageUserProgressClient(json)
        }
}

val platformModule = module {
    single<UserProgressLocalClient> {
        DeferredUserProgressLocalClient(
            createClient = { createUserProgressClient() },
            fallbackClientProvider = { InMemoryUserProgressLocalClient() }
        )
    }
}
