package com.marcinmoskala.albert.domain.usecase

import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.albert.domain.repository.UserRepository
import com.marcinmoskala.client.SynchronizeClient
import com.marcinmoskala.database.UserProgressLocalClient
import com.marcinmoskala.database.toCourseProgressApi
import com.marcinmoskala.model.UserCourseProgressApi

class SynchronizeProgressUseCase(
    private val synchronizeClient: SynchronizeClient,
    private val userProgressRepository: UserProgressRepository,
) {
    suspend operator fun invoke(userId: String): UserCourseProgressApi {
        userProgressRepository.migrateProgress(UserProgressRepository.ANONYMOUS_USER_ID, userId)
        val localProgress = userProgressRepository.getAll().toCourseProgressApi()
        val remoteMerged = synchronizeClient.synchronize(localProgress)
        userProgressRepository.synchronize(remoteMerged)
        return remoteMerged
    }
}
