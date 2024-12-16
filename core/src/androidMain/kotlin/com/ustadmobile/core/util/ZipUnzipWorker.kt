package com.ustadmobile.core.util


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import com.ustadmobile.core.domain.backup.AndroidUnzipFileUseCase
import com.ustadmobile.core.domain.backup.AndroidZipFileUseCase
import com.ustadmobile.core.domain.backup.FileToZip

import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.flow.collect

class ZipUnzipWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val PROGRESS_MAX = 100
        const val CHANNEL_ID = "zip_unzip_channel"
        const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {
        val isZip = inputData.getBoolean("isZip", true)
        val inputPath = inputData.getString("inputPath") ?: return Result.failure()
        val outputPath = inputData.getString("outputPath") ?: return Result.failure()

        createNotificationChannel()
        setForeground(createForegroundInfo(0f))

        return try {
            if (isZip) {
                zip(inputPath, outputPath)
            } else {
                unzip(inputPath, outputPath)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun zip(inputPath: String, outputPath: String) {
        val zipUseCase = AndroidZipFileUseCase(applicationContext)
        val filesToZip = listOf(FileToZip(inputPath, "file.txt")) // Adjust as needed
        zipUseCase(filesToZip, outputPath).collect { progress ->
            updateNotification(progress)
        }
    }

    private suspend fun unzip(inputPath: String, outputPath: String) {
        val unzipUseCase = AndroidUnzipFileUseCase(applicationContext)
        unzipUseCase(inputPath).collect { progress ->
            updateNotification(progress)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Zip/Unzip Progress",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundInfo(progress: Float): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Zip/Unzip Progress")
            .setProgress(PROGRESS_MAX, (progress * PROGRESS_MAX).toInt(), false)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private suspend fun updateNotification(progress: ZipProgress) {
        setForeground(createForegroundInfo(progress.progress))
    }
}