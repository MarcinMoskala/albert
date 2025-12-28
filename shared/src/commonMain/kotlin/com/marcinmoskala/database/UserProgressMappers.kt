package com.marcinmoskala.database

import com.marcinmoskala.model.UserCourseProgressApi
import com.marcinmoskala.model.UserProgressApi
import kotlinx.datetime.Instant

fun UserProgressRecord.toApi(): UserProgressApi = UserProgressApi(
    userId = userId,
    stepId = stepId,
    status = status,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    reviewAt = reviewAt?.toString(),
    lastIntervalDays = lastIntervalDays,
)

fun List<UserProgressRecord>.toCourseProgressApi(): UserCourseProgressApi =
    UserCourseProgressApi(progress = map { it.toApi() })

fun UserProgressApi.toRecord(): UserProgressRecord = UserProgressRecord(
    userId = userId,
    stepId = stepId,
    status = status,
    createdAt = Instant.parse(createdAt),
    updatedAt = Instant.parse(updatedAt),
    reviewAt = reviewAt?.let(Instant::parse),
    lastIntervalDays = lastIntervalDays
)
