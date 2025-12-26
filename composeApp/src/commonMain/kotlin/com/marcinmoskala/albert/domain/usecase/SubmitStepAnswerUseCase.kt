package com.marcinmoskala.albert.domain.usecase

import com.marcinmoskala.albert.domain.model.LessonStep
import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.database.UserProgressRecord
import com.marcinmoskala.database.UserProgressStatus
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class SubmitStepAnswerUseCase(
    private val userProgressRepository: UserProgressRepository
) {
    suspend fun execute(
        step: LessonStep,
        courseId: String,
        lessonId: String,
        isCorrect: Boolean
    ) {
        val userId = "guest1" // TODO: Get actual user ID from auth system
        val now = Clock.System.now()

        // Get existing record or create new one
        val existingRecord = userProgressRepository.get(userId, courseId, lessonId, step.stepId)

        val record = UserProgressRecord(
            userId = userId,
            courseId = courseId,
            lessonId = lessonId,
            stepId = step.stepId,
            status = if (isCorrect) UserProgressStatus.COMPLETED else UserProgressStatus.REPEATING,
            createdAt = existingRecord?.createdAt ?: now,
            updatedAt = now,
            reviewAt = if (isCorrect && step.repeatable) {
                // Schedule review based on last interval or start with 1 day
                val lastInterval = existingRecord?.lastIntervalDays ?: 0
                val nextInterval = if (lastInterval == 0) 1 else lastInterval * 2
                now.plus(nextInterval.days)
            } else null,
            lastIntervalDays = if (isCorrect && step.repeatable) {
                val lastInterval = existingRecord?.lastIntervalDays ?: 0
                if (lastInterval == 0) 1 else lastInterval * 2
            } else null
        )

        userProgressRepository.upsert(record)
    }
}
