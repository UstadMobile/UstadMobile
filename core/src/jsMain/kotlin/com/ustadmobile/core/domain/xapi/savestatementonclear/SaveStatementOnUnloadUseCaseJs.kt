package com.ustadmobile.core.domain.xapi.savestatementonclear

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import kotlinx.serialization.json.Json
import web.http.BodyInit
import web.navigator.navigator

/**
 * Handle saving a statement when the user is navigating away. This is done using sendBeacon as per
 * https://developer.mozilla.org/en-US/docs/Web/API/Navigator/sendBeacon .
 */
class SaveStatementOnUnloadUseCaseJs(
    private val endpoint: Endpoint,
    private val json: Json,
): SaveStatementOnUnloadUseCase {

    override fun invoke(statements: List<XapiStatement>, xapiSession: XapiSession) {
        navigator.sendBeacon(
            "${endpoint.url}api/xapi-ext/statementOnUnload",
            BodyInit(
                json.encodeToString(
                    XapiStatementsAndSession.serializer(),
                    XapiStatementsAndSession(statements, xapiSession)
                )
            )
        )
    }
}