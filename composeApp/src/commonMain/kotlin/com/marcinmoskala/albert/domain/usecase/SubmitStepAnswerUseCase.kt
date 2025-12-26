package com.marcinmoskala.albert.domain.usecase

import com.marcinmoskala.albert.domain.model.LessonStep
import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.database.UserProgressRecord
import com.marcinmoskala.database.UserProgressStatus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class SubmitStepAnswerUseCase(
    private val userProgressRepository: UserProgressRepository
) {
    suspend operator fun invoke(
        step: LessonStep,
        isCorrect: Boolean,
    ) {
        val userId = "guest1" // TODO: Get actual user ID from auth system
        val now = Clock.System.now()

        // Get existing record or create new one
        val existingRecord = userProgressRepository.get(userId, step.stepId)

        val (reviewAt, lastIntervalDays) = when {
            !step.repeatable -> null to null
            !isCorrect -> now.toLocalDateTime(TimeZone.UTC).date to null
            else -> { // isCorrect && step.repeatable
                val lastIntervalDays = existingRecord?.lastIntervalDays
                val newInterval = if (lastIntervalDays == null) 1 else lastIntervalDays * 2
                (now + newInterval.days).toLocalDateTime(TimeZone.UTC).date to newInterval
            }
        }

        val record = UserProgressRecord(
            userId = userId,
            stepId = step.stepId,
            status = when {
                step.repeatable -> UserProgressStatus.REPEATING
                isCorrect -> UserProgressStatus.COMPLETED
                else -> UserProgressStatus.PENDING
            },
            createdAt = existingRecord?.createdAt ?: now,
            updatedAt = now,
            reviewAt = reviewAt,
            lastIntervalDays = lastIntervalDays,
        )

        userProgressRepository.upsert(record)
    }
}
