package com.aman.vanish.ai

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aman.vanish.ai.db.AiCacheEntryDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that deletes AI cache entries older than 7 days.
 * Scheduled to run once daily.
 */
@HiltWorker
class AICacheCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cacheDao: AiCacheEntryDao,
) : CoroutineWorker(context, params) {

    companion object {
        private const val WORK_NAME = "ai_cache_cleanup"
        private const val TTL_DAYS = 7L

        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<AICacheCleanupWorker>(1, TimeUnit.DAYS)
                .build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val cutoff = System.currentTimeMillis() - (TTL_DAYS * 24 * 60 * 60 * 1000)
            cacheDao.deleteOlderThan(cutoff)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
