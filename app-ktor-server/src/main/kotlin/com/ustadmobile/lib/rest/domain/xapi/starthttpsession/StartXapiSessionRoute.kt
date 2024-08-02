package com.ustadmobile.lib.rest.domain.xapi.starthttpsession

import com.ustadmobile.core.domain.account.VerifyClientUserSessionUseCase
import com.ustadmobile.core.domain.xapi.starthttpsession.StartXapiSessionOverHttpUseCase
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.json.Json
import com.ustadmobile.door.ext.requireRemoteNodeIdAndAuth
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import io.ktor.http.ContentType
import io.ktor.server.response.respondText
import kotlinx.serialization.encodeToString

/**
 * Used by StartHttpSessionOverHttpUseCaseJs - where the web version needs to start an Xapi Over
 * http session.
 */
fun Route.StartHttpXapiSessionRoute(
    startXapiSessionOverHttpUseCase: (ApplicationCall) -> StartXapiSessionOverHttpUseCase,
    verifyClientUserSessionUseCase: (ApplicationCall) -> VerifyClientUserSessionUseCase,
    json: Json,
) {
    post("startSession") {
        val bodyStr = call.receiveText()
        val xapiSession = json.decodeFromString(XapiSessionEntity.serializer(), bodyStr)

        val (nodeId, nodeAuth)  = requireRemoteNodeIdAndAuth()
        verifyClientUserSessionUseCase(call).invoke(
            fromNodeId = nodeId,
            nodeAuth = nodeAuth,
            accountPersonUid = xapiSession.xseAccountPersonUid,
        )

        val xapiSessionResult = startXapiSessionOverHttpUseCase(call).invoke(xapiSession)
        call.respondText(json.encodeToString(xapiSessionResult), ContentType.Application.Json)
    }
}
