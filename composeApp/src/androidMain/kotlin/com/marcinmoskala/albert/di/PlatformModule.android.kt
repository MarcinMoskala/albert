package com.marcinmoskala.albert.di

import android.content.Context
import com.marcinmoskala.database.AlbertDatabase
import com.marcinmoskala.database.DriverFactory
import com.marcinmoskala.database.SqlDelightUserProgressLocalClient
import com.marcinmoskala.database.UserProgressLocalClient
import com.marcinmoskala.database.createUserProgressDatabase
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module

fun platformModule(context: Context) = module {
    single<UserProgressLocalClient> {
        val driverFactory = DriverFactory(context)
        val driver = runBlocking { driverFactory.createDriver() }
        val database: AlbertDatabase = createUserProgressDatabase(driver)
        SqlDelightUserProgressLocalClient(database)
    }
}
