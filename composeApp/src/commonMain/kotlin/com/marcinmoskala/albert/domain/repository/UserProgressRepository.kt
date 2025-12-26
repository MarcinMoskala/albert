package com.marcinmoskala.albert.domain.repository

import com.marcinmoskala.database.UserProgressRecord
import kotlinx.coroutines.flow.StateFlow

interface UserProgressRepository {
    val progress: StateFlow<Map<String, UserProgressRecord>>
    suspend fun upsert(record: UserProgressRecord)
    suspend fun get(userId: String, stepId: String): UserProgressRecord?
    suspend fun getAll(): List<UserProgressRecord>
    suspend fun getAllForUser(userId: String): List<UserProgressRecord>
    suspend fun delete(userId: String, stepId: String)
    suspend fun loadAllForUser(userId: String)
}
