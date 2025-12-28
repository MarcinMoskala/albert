package com.marcinmoskala.albert.di

import com.marcinmoskala.database.AlbertDatabase
import com.marcinmoskala.database.DeferredUserProgressLocalClient
import com.marcinmoskala.database.DriverFactory
import com.marcinmoskala.database.InMemoryUserProgressLocalClient
import com.marcinmoskala.database.SqlDelightUserProgressLocalClient
import com.marcinmoskala.database.UserProgressLocalClient
import com.marcinmoskala.database.createUserProgressDatabase
import com.marcinmoskala.database.UserProgressRecord
import com.marcinmoskala.database.toApi
import com.marcinmoskala.database.toRecord
import kotlin.js.json
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.dsl.module

private class LocalStorageBackedUserProgressClient(
    private val delegate: UserProgressLocalClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : UserProgressLocalClient {
    private val storageKey = "user_progress_backup"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            restoreFromLocalStorage()
        }
    }

    override suspend fun upsert(record: UserProgressRecord) {
        delegate.upsert(record)
        persist()
    }

    override suspend fun get(userId: String, stepId: String): UserProgressRecord? =
        delegate.get(userId, stepId)

    override suspend fun getAllForUser(userId: String): List<UserProgressRecord> =
        delegate.getAllForUser(userId)

    override suspend fun getAll(): List<UserProgressRecord> = delegate.getAll()

    override suspend fun delete(userId: String, stepId: String) {
        delegate.delete(userId, stepId)
        persist()
    }

    private suspend fun persist() {
        val current = delegate.getAll().map { it.toApi() }
        window.localStorage.setItem(storageKey, json.encodeToString(current))
    }

    private suspend fun restoreFromLocalStorage() {
        val stored = window.localStorage.getItem(storageKey) ?: return
        runCatching {
            val apiList =
                json.decodeFromString<List<com.marcinmoskala.model.UserProgressApi>>(stored)
            apiList.map { it.toRecord() }
        }.getOrNull()?.let { delegate.upsertMany(it) }
    }
}

val platformModule = module {
    single<UserProgressLocalClient> {
        val driverFactory = DriverFactory()
        val baseClient = DeferredUserProgressLocalClient(
            createClient = {
                val driver = driverFactory.createDriver()
                val database: AlbertDatabase = createUserProgressDatabase(driver)
                SqlDelightUserProgressLocalClient(database)
            },
            fallbackClientProvider = { InMemoryUserProgressLocalClient() }
        )
        LocalStorageBackedUserProgressClient(baseClient)
    }
}
