package com.marcinmoskala.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties

actual class DriverFactory {
    actual fun createDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
        databaseName: String
    ): SqlDriver {
        val databasePathFromEnvironment = System.getenv("ALBERT_DB_PATH")
        val databasePathFromSystemProperty = System.getProperty("albert.db.path")
        val resolvedDatabasePath = listOf(
            databasePathFromEnvironment,
            databasePathFromSystemProperty
        ).firstOrNull { it != null && it.isNotBlank() }
            ?: Paths.get("data", databaseName).toString()

        ensureParentDirectoryExists(resolvedDatabasePath)

        return JdbcSqliteDriver(
            url = "jdbc:sqlite:$resolvedDatabasePath",
            properties = Properties(),
            schema = schema.synchronous()
        )
    }

    private fun ensureParentDirectoryExists(pathString: String) {
        val path: Path = Paths.get(pathString).toAbsolutePath()
        val parent: Path? = path.parent
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent)
        }
    }
}