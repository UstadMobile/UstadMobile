package com.ustadmobile.core.domain.person.bulkadd

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.person.bulkadd.EnqueueBulkAddPersonUseCase.Companion.TMP_FILE_PREFIX
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.di
import com.ustadmobile.core.viewmodel.person.bulkaddrunimport.BulkAddPersonRunImportUiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.io.File

/**
 * Run bulk import on behalf of a web client. See EnqueueBulkAddPerson for an explanation of the
 * overall flow.
 */
class BulkAddPersonJob: Job {

    override fun execute(context: JobExecutionContext) {
        //Use di to get status holder
        val endpointUrl = context.jobDetail.jobDataMap.getString(EnqueueBulkAddPersonUseCase.DATA_LEARNINGSPACE)
        val timestamp = context.jobDetail.jobDataMap.getLong(EnqueueBulkAddPersonUseCase.DATA_TIMESTAMP)
        val di = context.scheduler.di
        val learningSpace = LearningSpace(endpointUrl)

        val statusMap: BulkAddPersonStatusMap = di.direct.on(learningSpace).instance()
        val bulkAddPersonUseCase: BulkAddPersonsUseCase = di.direct.on(learningSpace).instance()
        val tmpDir: File = di.direct.instance(tag = DiTag.TAG_TMP_DIR)

        val initUiState = BulkAddPersonRunImportUiState()

        statusMap[timestamp] = initUiState

        val csvTmpFile = File(tmpDir, "$TMP_FILE_PREFIX$timestamp.csv")
        runBlocking {
            try {
                val csvText = csvTmpFile.readText()
                val result = bulkAddPersonUseCase(
                    csv = csvText,
                    onProgress = { numImported, total ->
                        statusMap[timestamp] = initUiState.copy(
                            numImported = numImported,
                            totalRecords = total,
                        )
                    }
                )
                statusMap[timestamp] = initUiState.copy(
                    inProgress = false,
                    totalRecords = result.numImported,
                    numImported = result.numImported,
                )
            }catch(e: Throwable) {
                Napier.w("BulkAddPerson: Exception running", e)
                statusMap[timestamp] =initUiState.copy(
                    inProgress = false,
                    errors = (e as? BulkAddPersonException)?.errors ?: emptyList(),
                    errorMessage = e.message
                )
            }finally {
                csvTmpFile.delete()
            }
        }
    }
}