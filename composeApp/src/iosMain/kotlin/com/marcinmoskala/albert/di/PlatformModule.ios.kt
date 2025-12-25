package com.marcinmoskala.albert.di

import com.marcinmoskala.database.AlbertDatabase
import com.marcinmoskala.database.DriverFactory
import com.marcinmoskala.database.SqlDelightUserProgressLocalClient
import com.marcinmoskala.database.UserProgressLocalClient
import com.marcinmoskala.database.createUserProgressDatabase
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module

val platformModule = module {
    single<UserProgressLocalClient> {
        val driverFactory = DriverFactory()
        val driver = runBlocking { driverFactory.createDriver() }
        val database: AlbertDatabase = createUserProgressDatabase(driver)
        SqlDelightUserProgressLocalClient(database)
    }
}
