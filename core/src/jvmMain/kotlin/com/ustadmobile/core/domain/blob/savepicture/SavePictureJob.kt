package com.ustadmobile.core.domain.blob.savepicture

import com.ustadmobile.core.account.Endpoint
import org.quartz.Job
import org.quartz.JobExecutionContext
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase.Companion.DATA_ENDPOINT
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase.Companion.DATA_TABLE_ID
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase.Companion.DATA_ENTITY_UID
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase.Companion.DATA_LOCAL_URI
import com.ustadmobile.core.util.ext.di
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


class SavePictureJob: Job {

    override fun execute(context: JobExecutionContext) {
        val di = context.scheduler.di
        val jobDataMap = context.jobDetail.jobDataMap

        val endpoint = Endpoint(jobDataMap.getString(DATA_ENDPOINT))
        val tableId = jobDataMap.getInt(DATA_TABLE_ID)
        val entityUid = jobDataMap.getLong(DATA_ENTITY_UID)
        val pictureUri = jobDataMap.getString(DATA_LOCAL_URI)

        val savePictureUseCase: SavePictureUseCase = di.on(endpoint).direct.instance()
        runBlocking {
            try {
                savePictureUseCase(
                    entityUid = entityUid,
                    tableId = tableId,
                    pictureUri = pictureUri,
                )
            }catch(e: Throwable) {
                Napier.e("SavePictureJob: exception running savepicture", e)
                throw e
            }
        }
    }
}