package com.ustadmobile.core.domain.xapi.savestatementonclear

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import kotlinx.serialization.json.Json
import web.http.BodyInit
import web.navigator.navigator

/**
 * Handle saving a statement when the user is navigating away. This is done using sendBeacon as per
 * https://developer.mozilla.org/en-US/docs/Web/API/Navigator/sendBeacon .
 */
class SaveStatementOnUnloadUseCaseJs(
    private val learningSpace: LearningSpace,
    private val json: Json,
): SaveStatementOnUnloadUseCase {

    override fun invoke(statements: List<XapiStatement>, xapiSession: XapiSessionEntity) {
        navigator.sendBeacon(
            "${learningSpace.url}api/xapi-ext/statementOnUnload",
            BodyInit(
                json.encodeToString(
                    XapiStatementsAndSession.serializer(),
                    XapiStatementsAndSession(statements, xapiSession)
                )
            )
        )
    }
}