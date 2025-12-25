package com.marcinmoskala.database

interface UserProgressLocalClient {
    suspend fun upsert(record: UserProgressRecord)
    suspend fun get(userId: String, courseId: String, lessonId: String, stepId: String): UserProgressRecord?
    suspend fun getAllForUser(userId: String): List<UserProgressRecord>
    suspend fun getAll(): List<UserProgressRecord>
    suspend fun delete(userId: String, courseId: String, lessonId: String, stepId: String)
}