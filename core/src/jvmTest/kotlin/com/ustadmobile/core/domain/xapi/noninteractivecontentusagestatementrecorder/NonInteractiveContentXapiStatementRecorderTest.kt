package com.ustadmobile.core.domain.xapi.noninteractivecontentusagestatementrecorder

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.ext.resultDurationMillis
import com.ustadmobile.core.domain.xapi.ext.resultProgressExtension
import com.ustadmobile.core.domain.xapi.model.XapiActivity
import com.ustadmobile.core.domain.xapi.model.XapiActivityStatementObject
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnClearUseCase
import com.ustadmobile.core.test.isWithinThreshold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.Test
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import kotlin.test.BeforeTest

class NonInteractiveContentXapiStatementRecorderTest {

    private lateinit var recorder: NonInteractiveContentXapiStatementRecorder

    private lateinit var saveStatementOnClearUseCase: SaveStatementOnClearUseCase

    private lateinit var xapiStatementResource: XapiStatementResource

    private lateinit var xapiSession: XapiSession

    private lateinit var scope: CoroutineScope

    @BeforeTest
    fun setup() {
        saveStatementOnClearUseCase = mock {  }
        xapiStatementResource = mock { }
        val rootActivityId = "http://localhost:8087/activity/the-meaning-of-life"
        xapiSession = XapiSession(
            endpoint = Endpoint("http://localhost:8087/"),
            accountPersonUid = 42L,
            accountUsername = "test",
            clazzUid = 1042L,
            rootActivityId = rootActivityId,
        )
        scope = CoroutineScope(Dispatchers.Default + Job())

        recorder = NonInteractiveContentXapiStatementRecorder(
            saveStatementOnClearUseCase = saveStatementOnClearUseCase,
            saveStatementOnUnloadUseCase = null,
            xapiStatementResource = xapiStatementResource,
            xapiSession = xapiSession,
            scope = scope,
            xapiActivityProvider = {
                XapiActivityStatementObject(
                    id = rootActivityId,
                    definition = XapiActivity(
                        name = mapOf("en" to "The Meaning of Life")
                    )
                )
            }
        )
    }

    @Test
    fun givenRecorderCreated_whenOnActiveSetAndOnCompletedInvoked_thenShouldRecordCompletedStatement() {
        recorder.onActiveChanged(true)
        val delay = 2_000L

        Thread.sleep(delay)
        recorder.onComplete()

        verifyBlocking(xapiStatementResource, timeout(1000)) {
            post(
                statements = argWhere { stmts ->
                    isWithinThreshold(delay, stmts.first().resultDurationMillis!!, 100) &&
                            stmts.first().resultProgressExtension == 100
                },
                xapiSession = eq(xapiSession),
            )
        }
    }

    @Test
    fun givenRecorderCreated_whenOnActiveSetAndClearedBeforeCompleted_thenShouldRecordProgress() {
        recorder.onActiveChanged(true)
        val delay = 2_000L
        val progress = 50

        Thread.sleep(delay)
        recorder.onProgressed(progress)
        recorder.onCleared()

        verify(saveStatementOnClearUseCase).invoke(
            statements = argWhere {
                isWithinThreshold(delay, it.first().resultDurationMillis!!, 100) &&
                        it.first().resultProgressExtension!! == progress
            },
            xapiSession = eq(xapiSession),
        )
    }

}