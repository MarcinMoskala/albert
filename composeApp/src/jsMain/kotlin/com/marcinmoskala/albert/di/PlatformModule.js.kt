package com.marcinmoskala.albert.di

import com.marcinmoskala.database.AlbertDatabase
import com.marcinmoskala.database.DriverFactory
import com.marcinmoskala.database.SqlDelightUserProgressLocalClient
import com.marcinmoskala.database.UserProgressLocalClient
import com.marcinmoskala.database.createUserProgressDatabase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.dsl.module

// Lazy database holder for JS platform
private var albertDatabase: AlbertDatabase? = null

@OptIn(DelicateCoroutinesApi::class)
private suspend fun getOrCreateDatabase(driverFactory: DriverFactory): AlbertDatabase {
    if (albertDatabase == null) {
        val driver = driverFactory.createDriver()
        albertDatabase = createUserProgressDatabase(driver)
    }
    return albertDatabase!!
}

val platformModule = module {
    single { DriverFactory() }

    single<UserProgressLocalClient> {
        val driverFactory = get<DriverFactory>()

        // Eagerly initialize database in background
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            getOrCreateDatabase(driverFactory)
        }

        // Return a client that will lazily await database initialization
        object : UserProgressLocalClient {
            override suspend fun upsert(record: com.marcinmoskala.database.UserProgressRecord) {
                val db = getOrCreateDatabase(driverFactory)
                SqlDelightUserProgressLocalClient(db).upsert(record)
            }

            override suspend fun get(
                userId: String,
                stepId: String
            ): com.marcinmoskala.database.UserProgressRecord? {
                val db = getOrCreateDatabase(driverFactory)
                return SqlDelightUserProgressLocalClient(db).get(userId, stepId)
            }

            override suspend fun getAllForUser(userId: String): List<com.marcinmoskala.database.UserProgressRecord> {
                val db = getOrCreateDatabase(driverFactory)
                return SqlDelightUserProgressLocalClient(db).getAllForUser(userId)
            }

            override suspend fun getAll(): List<com.marcinmoskala.database.UserProgressRecord> {
                val db = getOrCreateDatabase(driverFactory)
                return SqlDelightUserProgressLocalClient(db).getAll()
            }

            override suspend fun delete(userId: String, stepId: String) {
                val db = getOrCreateDatabase(driverFactory)
                SqlDelightUserProgressLocalClient(db).delete(userId, stepId)
            }
        }
    }
}
