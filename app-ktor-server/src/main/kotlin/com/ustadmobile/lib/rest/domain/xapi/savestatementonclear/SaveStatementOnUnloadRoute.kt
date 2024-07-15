package com.ustadmobile.lib.rest.domain.xapi.savestatementonclear

import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.savestatementonclear.XapiStatementsAndSession
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.json.Json

/**
 *
 */
fun Route.SaveStatementOnUnloadRoute(
    statementResource: (ApplicationCall) -> XapiStatementResource,
    json: Json,
) {
    post("statementOnUnload") {
        val bodyStr = call.receiveText()
        val statementResourceVal = statementResource(call)

        val statementsAndSession: XapiStatementsAndSession = json.decodeFromString(bodyStr)

        statementResourceVal.post(
            statementsAndSession.statements, statementsAndSession.session
        )
    }
}