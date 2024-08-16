package com.ustadmobile.core.domain.xapi.noninteractivecontentusagestatementrecorder

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.ext.agent
import com.ustadmobile.core.domain.xapi.model.VERB_COMPLETED
import com.ustadmobile.core.domain.xapi.model.VERB_PROGRESSED
import com.ustadmobile.core.domain.xapi.model.XAPI_RESULT_EXTENSION_PROGRESS
import com.ustadmobile.core.domain.xapi.model.XapiActivityStatementObject
import com.ustadmobile.core.domain.xapi.model.XapiResult
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.domain.xapi.model.XapiVerb
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnClearUseCase
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnUnloadUseCase
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import io.github.aakira.napier.Napier
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import kotlinx.atomicfu.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Duration.Companion.milliseconds

/**
 * Records xAPI statements for non-interactive content e.g. Video, PDF and EPUB. This tracks total
 * usage time (not counting time where the window is inactive) and the maximum progress point reached.
 */
class NonInteractiveContentXapiStatementRecorder(
    private val saveStatementOnClearUseCase: SaveStatementOnClearUseCase,
    private val saveStatementOnUnloadUseCase: SaveStatementOnUnloadUseCase?,
    private val xapiStatementResource: XapiStatementResource,
    private val xapiSession: XapiSessionEntity,
    private val scope: CoroutineScope,
    private val xapiActivityProvider: () -> XapiActivityStatementObject,
    private val endpoint: Endpoint,
) {

    private val totalUsageTime = atomic(0L)

    private val maxProgressPoint = atomic(0)

    private val activeStartTime = atomic(0L)

    /*
     * Invoked by the underlying content view model:
     * For videos: this should be based on the video being played/paused
     * For PDF and Epub: this should be based on visibility e.g. record the time that is spent in
     * the Lifecycle resumed state
     */
    fun onActiveChanged(active: Boolean) {
        Napier.v { "ContentUsageStatementRecorder: active=$active" }

        if(active) {
            activeStartTime.update {
                if(it == 0L) systemTimeInMillis() else it
            }
        }else {
            val activeStarted = activeStartTime.getAndUpdate { 0L }
            if(activeStarted != 0L)
                totalUsageTime.update { it + (systemTimeInMillis() - activeStarted) }
        }
    }

    fun onProgressed(progress: Int) {
        maxProgressPoint.update { maxOf(it, progress) }
    }

    fun onComplete() {
        val usageDurationVal = totalUsageTime.getAndUpdate { 0L }
        val activeStartTimeVal = activeStartTime.getAndUpdate { 0L }
        val timeSinceActive = if(activeStartTimeVal != 0L) {
            systemTimeInMillis() - activeStartTimeVal
        } else {
            0L
        }

        maxProgressPoint.update { 0 }

        scope.launch {
            xapiStatementResource.post(
                statements = listOf(
                    createXapiStatement(
                        totalDuration = usageDurationVal + timeSinceActive,
                        progress = 100,
                        isComplete = true,
                    )
                ),
                xapiSession = xapiSession,
            )
        }
    }

    private fun createXapiStatement(
        totalDuration: Long,
        progress: Int,
        isComplete: Boolean?,
    ): XapiStatement {
        return XapiStatement(
            actor = xapiSession.agent(endpoint),
            verb = XapiVerb(
                id = if(isComplete == true) {
                    VERB_COMPLETED
                }else {
                    VERB_PROGRESSED
                }
            ),
            `object` = xapiActivityProvider(),
            result = XapiResult(
                completion = isComplete,
                duration = totalDuration.milliseconds.toIsoString(),
                extensions = mapOf(
                    XAPI_RESULT_EXTENSION_PROGRESS to JsonPrimitive(progress)
                )
            )
        )
    }

    private fun createFinalStatement(): XapiStatement {
        onActiveChanged(false)

        val usageDurationVal = totalUsageTime.getAndUpdate { 0L }
        val maxProgressVal = maxProgressPoint.getAndUpdate { 0 }
        return createXapiStatement(
            totalDuration = usageDurationVal,
            progress = maxProgressVal,
            isComplete = false,
        )
    }

    fun onCleared() {
        saveStatementOnClearUseCase(listOf(createFinalStatement()), xapiSession)
    }

    fun onUnload() {
        saveStatementOnUnloadUseCase?.invoke(listOf(createFinalStatement()), xapiSession)
    }

}