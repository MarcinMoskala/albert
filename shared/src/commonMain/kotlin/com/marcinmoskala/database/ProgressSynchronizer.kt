package com.marcinmoskala.database

import com.marcinmoskala.model.UserCourseProgressApi
import com.marcinmoskala.model.UserProgressApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class ProgressSynchronizer(
    private val userProgressLocalClient: UserProgressLocalClient,
    private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun synchronizeWithRemote(remoteProgressApi: UserCourseProgressApi) {
        val remoteRecordsByKey = remoteProgressApi.progress.associateBy { api ->
            makeKey(api.userId, api.stepId)
        }

        withContext(ioDispatcher) {
            val localRecords = userProgressLocalClient.getAll()
            val localRecordsByKey = localRecords.associateBy { record ->
                makeKey(record.userId, record.stepId)
            }

            val toUpsert = mutableListOf<UserProgressRecord>()
            for ((key, remoteApi) in remoteRecordsByKey) {
                val localRecord = localRecordsByKey[key]
                val remoteUpdatedAt = Instant.parse(remoteApi.updatedAt)
                if (localRecord == null || remoteUpdatedAt > localRecord.updatedAt) {
                    toUpsert += remoteApi.toRecord()
                }
            }

            if (toUpsert.isEmpty()) return@withContext

            userProgressLocalClient.upsertMany(toUpsert)
        }
    }

    private fun makeKey(userId: String, stepId: String): String = "$userId:$stepId"
}

