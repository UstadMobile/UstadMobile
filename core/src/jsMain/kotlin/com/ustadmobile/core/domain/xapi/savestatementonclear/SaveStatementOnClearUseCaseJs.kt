package com.ustadmobile.core.domain.xapi.savestatementonclear

import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SaveStatementOnClearUseCaseJs(
    private val xapiStatementResource: XapiStatementResource
): SaveStatementOnClearUseCase {

    @OptIn(DelicateCoroutinesApi::class)
    override fun invoke(statements: List<XapiStatement>, xapiSession: XapiSession) {
        GlobalScope.launch {
            xapiStatementResource.post(statements, xapiSession)
        }
    }

}