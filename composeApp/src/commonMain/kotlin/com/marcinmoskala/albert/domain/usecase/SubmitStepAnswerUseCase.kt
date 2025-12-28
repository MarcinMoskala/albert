package com.marcinmoskala.albert.domain.usecase

import com.marcinmoskala.albert.domain.model.LessonStep
import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.database.UserProgressRecord
import com.marcinmoskala.database.UserProgressStatus
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.Duration.Companion.days

class SubmitStepAnswerUseCase(
    private val userProgressRepository: UserProgressRepository
) {
    suspend operator fun invoke(
        userId: String,
        step: LessonStep,
        isCorrect: Boolean,
    ) {
        val now: Instant = Clock.System.now()

        // Get existing record or create new one
        val existingRecord = userProgressRepository.get(userId, step.stepId)

        val reviewAt: Instant?
        val lastIntervalDays: Int?
        when {
            !step.repeatable -> {
                reviewAt = null
                lastIntervalDays = null
            }

            !isCorrect -> {
                reviewAt = now
                lastIntervalDays = null
            }
            else -> { // isCorrect && step.repeatable
                val existingIntervalDays = existingRecord?.lastIntervalDays
                val newIntervalDays =
                    if (existingIntervalDays == null) 1 else existingIntervalDays * 2
                reviewAt = now + newIntervalDays.days
                lastIntervalDays = newIntervalDays
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

        println("Store record: $record")
        userProgressRepository.upsert(record)
    }
}
