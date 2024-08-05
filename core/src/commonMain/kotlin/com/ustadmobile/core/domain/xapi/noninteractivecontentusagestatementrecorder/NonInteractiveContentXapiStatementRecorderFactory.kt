package com.ustadmobile.core.domain.xapi.noninteractivecontentusagestatementrecorder

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.model.XapiActivityStatementObject
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnClearUseCase
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnUnloadUseCase
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import kotlinx.coroutines.CoroutineScope

class NonInteractiveContentXapiStatementRecorderFactory(
    private val saveStatementOnClearUseCase: SaveStatementOnClearUseCase,
    private val saveStatementOnUnloadUseCase: SaveStatementOnUnloadUseCase?,
    private val xapiStatementResource: XapiStatementResource,
    private val endpoint: Endpoint,
) {

    fun newStatementRecorder(
        xapiSession: XapiSessionEntity,
        scope: CoroutineScope,
        xapiActivityProvider: () -> XapiActivityStatementObject
    ) : NonInteractiveContentXapiStatementRecorder{
        return NonInteractiveContentXapiStatementRecorder(
            saveStatementOnClearUseCase = saveStatementOnClearUseCase,
            saveStatementOnUnloadUseCase = saveStatementOnUnloadUseCase,
            xapiStatementResource= xapiStatementResource,
            xapiSession = xapiSession,
            scope = scope,
            xapiActivityProvider = xapiActivityProvider,
            endpoint = endpoint,
        )
    }

}