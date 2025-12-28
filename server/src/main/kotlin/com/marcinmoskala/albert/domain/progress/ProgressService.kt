package com.marcinmoskala.albert.domain.progress

import com.marcinmoskala.database.ProgressSynchronizer
import com.marcinmoskala.database.UserProgressLocalClient
import com.marcinmoskala.database.toCourseProgressApi
import com.marcinmoskala.model.UserCourseProgressApi

class ProgressService(
    private val userProgressLocalClient: UserProgressLocalClient,
    private val progressSynchronizer: ProgressSynchronizer,
) {
    suspend fun synchronize(progressApi: UserCourseProgressApi): UserCourseProgressApi {
        progressSynchronizer.synchronizeWithRemote(progressApi)
        val allRecords = userProgressLocalClient.getAll()
        return allRecords.toCourseProgressApi()
    }
}
