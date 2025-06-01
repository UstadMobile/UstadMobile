package com.ustadmobile.core.domain.blob.savepicture

import com.ustadmobile.core.account.LearningSpace
import org.quartz.Scheduler
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase.Companion.DATA_LEARNINGSPACE
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase.Companion.DATA_TABLE_ID
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase.Companion.DATA_ENTITY_UID
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase.Companion.DATA_LOCAL_URI
import io.github.aakira.napier.Napier
import org.quartz.JobBuilder
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey


class EnqueueSavePictureUseCaseJvm (
    private val scheduler: Scheduler,
    private val learningSpace: LearningSpace,
) : EnqueueSavePictureUseCase{

    override suspend fun invoke(entityUid: Long, tableId: Int, pictureUri: String?) {
        Napier.d { "EnqueueSavePictureUseCase: Save picture $pictureUri for entity=$entityUid tableId=$tableId" }
        val quartzJob = JobBuilder.newJob(SavePictureJob::class.java)
            .usingJobData(DATA_LEARNINGSPACE, learningSpace.url)
            .usingJobData(DATA_TABLE_ID, tableId)
            .usingJobData(DATA_LOCAL_URI, pictureUri)
            .usingJobData(DATA_ENTITY_UID, entityUid)
            .build()

        val triggerKey = TriggerKey("save-picture-${learningSpace.url}-$tableId-$entityUid-$pictureUri")
        scheduler.unscheduleJob(triggerKey)
        val jobTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startNow()
            .build()

        scheduler.scheduleJob(quartzJob, jobTrigger)
    }
}