package com.marcinmoskala.database

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlDriver
import kotlin.time.Instant

enum class UserProgressStatus {
    PENDING,
    REPEATING,
    COMPLETED;
}

data class UserProgressRecord(
    val userId: String,
    val stepId: String,
    val status: UserProgressStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val reviewAt: Instant?,
    val lastIntervalDays: Int?
)

fun createUserProgressDatabase(driver: SqlDriver): AlbertDatabase = AlbertDatabase(driver)

class SqlDelightUserProgressLocalClient(private val database: AlbertDatabase) : UserProgressLocalClient {
    private val queries get() = database.userProgressQueries

    override suspend fun upsert(record: UserProgressRecord) {
        queries.insertUserProgress(
            userId = record.userId,
            stepId = record.stepId,
            status = record.status.toString(),
            createdAt = record.createdAt.toString(),
            updatedAt = record.updatedAt.toString(),
            reviewAt = record.reviewAt?.toString(),
            lastIntervalDays = record.lastIntervalDays?.toLong()
        )
    }

    override suspend fun get(
        userId: String,
        stepId: String
    ): UserProgressRecord? = queries.selectUserProgress(
        userId = userId,
        stepId = stepId
    ).awaitAsOneOrNull()?.toRecord()

    override suspend fun getAll(): List<UserProgressRecord> = queries.selectAllUserProgress()
        .awaitAsList().map { it.toRecord() }

    override suspend fun getAllForUser(userId: String): List<UserProgressRecord> = queries.selectUserProgressByUser(
        userId = userId
    ).awaitAsList().map { it.toRecord() }

    override suspend fun delete(
        userId: String,
        stepId: String
    ) {
        queries.deleteUserProgress(
            userId = userId,
            stepId = stepId
        )
    }

    private fun User_progress.toRecord(): UserProgressRecord = UserProgressRecord(
        userId = userId,
        stepId = stepId,
        status = UserProgressStatus.valueOf(status),
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
        reviewAt = reviewAt?.let(Instant::parse),
        lastIntervalDays = lastIntervalDays?.toInt()
    )
}