package com.ustadmobile.core.domain.blob

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.quartz.InterruptableJob
import org.quartz.JobExecutionContext

abstract class InterruptableCoroutineJob: InterruptableJob {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun execute(context: JobExecutionContext) {
        runBlocking {
            withContext(scope.coroutineContext) {
                executeAsync(context)
            }
        }
    }

    abstract suspend fun executeAsync(context: JobExecutionContext)

    override fun interrupt() {
        scope.cancel("Job Interrupted")
    }
}