package com.marcinmoskala.albert.domain.usecase

import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.client.SynchronizeClient
import com.marcinmoskala.database.UserProgressLocalClient
import com.marcinmoskala.database.toCourseProgressApi
import com.marcinmoskala.model.UserCourseProgressApi

class SynchronizeProgressUseCase(
    private val synchronizeClient: SynchronizeClient,
    private val userProgressRepository: UserProgressRepository,
    private val localClient: UserProgressLocalClient,
) {
    suspend operator fun invoke(): UserCourseProgressApi {
        val localProgress = localClient.getAll().toCourseProgressApi()
        val remoteMerged = synchronizeClient.synchronize(localProgress)
        userProgressRepository.synchronize(remoteMerged)
        return remoteMerged
    }
}
