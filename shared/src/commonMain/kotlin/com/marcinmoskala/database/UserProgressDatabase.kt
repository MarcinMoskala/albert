package com.marcinmoskala.database

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlDriver
import kotlinx.datetime.Instant

enum class UserProgressStatus {
    REPEATING,
    COMPLETED;
    companion object {
        fun fromStorage(value: String): UserProgressStatus = when (value) {
            "repeating" -> REPEATING
            "completed" -> COMPLETED
            else -> throw IllegalArgumentException("Unknown status: $value")
        }
        fun toStorage(value: UserProgressStatus): String = when (value) {
            REPEATING -> "repeating"
            COMPLETED -> "completed"
        }
    }
}

data class UserProgressRecord(
    val userId: String,
    val courseId: String,
    val lessonId: String,
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
            courseId = record.courseId,
            lessonId = record.lessonId,
            stepId = record.stepId,
            status = UserProgressStatus.toStorage(record.status),
            createdAt = record.createdAt.toString(),
            updatedAt = record.updatedAt.toString(),
            reviewAt = record.reviewAt?.toString(),
            lastIntervalDays = record.lastIntervalDays?.toLong()
        )
    }

    override suspend fun get(
        userId: String,
        courseId: String,
        lessonId: String,
        stepId: String
    ): UserProgressRecord? = queries.selectUserProgress(
        userId = userId,
        courseId = courseId,
        lessonId = lessonId,
        stepId = stepId
    ).awaitAsOneOrNull()?.toRecord()

    override suspend fun getAll(): List<UserProgressRecord> = queries.selectAllUserProgress()
        .awaitAsList().map { it.toRecord() }

    override suspend fun getAllForUser(userId: String): List<UserProgressRecord> = queries.selectUserProgressByUser(
        userId = userId
    ).awaitAsList().map { it.toRecord() }

    override suspend fun delete(
        userId: String,
        courseId: String,
        lessonId: String,
        stepId: String
    ) {
        queries.deleteUserProgress(
            userId = userId,
            courseId = courseId,
            lessonId = lessonId,
            stepId = stepId
        )
    }

    private fun User_progress.toRecord(): UserProgressRecord = UserProgressRecord(
        userId = userId,
        courseId = courseId,
        lessonId = lessonId,
        stepId = stepId,
        status = UserProgressStatus.fromStorage(status),
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
        reviewAt = reviewAt?.let(Instant::parse),
        lastIntervalDays = lastIntervalDays?.toInt()
    )
}