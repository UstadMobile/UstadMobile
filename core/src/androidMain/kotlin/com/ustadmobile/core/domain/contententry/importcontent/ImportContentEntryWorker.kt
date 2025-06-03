package com.ustadmobile.core.domain.contententry.importcontent

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ustadmobile.core.account.LearningSpace
import io.github.aakira.napier.Napier
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class ImportContentEntryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params){

    private val di: DI by closestDI { applicationContext }

    override suspend fun doWork(): Result {
        val learningSpaceUrl = inputData.getString(EnqueueContentEntryImportUseCase.DATA_LEARNINGSPACE)
            ?: throw IllegalArgumentException("Endpoint url not specified")
        val jobUid = inputData.getLong(EnqueueContentEntryImportUseCase.DATA_JOB_UID, 0)

        val importUseCase: ImportContentEntryUseCase = di.on(LearningSpace(learningSpaceUrl)).direct.instance()
        return try {
            importUseCase(jobUid)
            Result.success()
        }catch(e: Exception) {
            Napier.e("ImportContentEntryWorker: Exception", e)
            Result.failure()
        }
    }
}