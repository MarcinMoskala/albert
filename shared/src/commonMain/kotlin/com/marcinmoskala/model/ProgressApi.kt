package com.marcinmoskala.model

import com.marcinmoskala.database.UserProgressStatus
import kotlinx.serialization.Serializable

@Serializable
data class UserProgressApi(
    val userId: String,
    val stepId: String,
    val status: UserProgressStatus,
    val createdAt: String,
    val updatedAt: String,
    val reviewAt: String?,
    val lastIntervalDays: Int?
)

@Serializable
data class UserCourseProgressApi(
    val progress: List<UserProgressApi>
)
