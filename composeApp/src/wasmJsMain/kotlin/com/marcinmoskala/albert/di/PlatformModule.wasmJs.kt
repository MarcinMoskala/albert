package com.marcinmoskala.albert.di

import com.marcinmoskala.database.UserProgressLocalClient
import com.marcinmoskala.database.UserProgressRecord
import org.koin.dsl.module

// Temporary in-memory implementation for WasmJs platform
// TODO: Implement full SQLite support for WASM
// See: https://github.com/dellisd/sqldelight-sqlite-wasm for implementation examples
class InMemoryUserProgressLocalClient : UserProgressLocalClient {
    private val records = mutableListOf<UserProgressRecord>()

    override suspend fun upsert(record: UserProgressRecord) {
        records.removeAll {
            it.userId == record.userId &&
                    it.stepId == record.stepId
        }
        records.add(record)
    }

    override suspend fun get(
        userId: String,
        stepId: String
    ): UserProgressRecord? = records.firstOrNull {
        it.userId == userId &&
                it.stepId == stepId
    }

    override suspend fun getAll(): List<UserProgressRecord> = records.toList()

    override suspend fun getAllForUser(userId: String): List<UserProgressRecord> =
        records.filter { it.userId == userId }

    override suspend fun delete(
        userId: String,
        stepId: String
    ) {
        records.removeAll {
            it.userId == userId &&
                    it.stepId == stepId
        }
    }
}

val platformModule = module {
    single<UserProgressLocalClient> { InMemoryUserProgressLocalClient() }
}
