package com.ustadmobile.lib.rest.domain.xapi.session

import com.ustadmobile.core.domain.account.VerifyClientUserSessionUseCase
import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.xapi.model.XapiActor
import com.ustadmobile.core.domain.xapi.session.ResumeOrStartXapiSessionUseCase
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.json.Json
import com.ustadmobile.door.ext.requireRemoteNodeIdAndAuth
import com.ustadmobile.lib.rest.ext.requireParamOrThrow
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import kotlinx.serialization.encodeToString

/**
 * Used by StartHttpSessionOverHttpUseCaseJs - where the web version needs to start an Xapi Over
 * http session.
 */
fun Route.ResumeOrStartXapiSessionRoute(
    resumeOrStartXapiSessionUseCase: (ApplicationCall) -> ResumeOrStartXapiSessionUseCase,
    verifyClientUserSessionUseCase: (ApplicationCall) -> VerifyClientUserSessionUseCase,
    json: Json,
) {
    post("resumeOrStartSession") {
        val (nodeId, nodeAuth)  = requireRemoteNodeIdAndAuth()
        val queryParams = call.request.queryParameters
        val accountPersonUid = queryParams.requireParamOrThrow("accountPersonUid").toLong()

        verifyClientUserSessionUseCase(call).invoke(
            fromNodeId = nodeId,
            nodeAuth = nodeAuth,
            accountPersonUid = accountPersonUid,
        )

        try {
            val xapiSessionResult = resumeOrStartXapiSessionUseCase(call).invoke(
                accountPersonUid = queryParams.requireParamOrThrow("accountPersonUid").toLong(),
                actor = json.decodeFromString(
                    XapiActor.serializer(), queryParams.requireParamOrThrow("actor")
                ),
                activityId = queryParams.requireParamOrThrow("activityId"),
                clazzUid = queryParams.requireParamOrThrow("clazzUid").toLong(),
                cbUid = queryParams.requireParamOrThrow("cbUid").toLong(),
                contentEntryUid = queryParams.requireParamOrThrow("contentEntryUid").toLong(),
            )

            call.respondText(json.encodeToString(xapiSessionResult), ContentType.Application.Json)
        }catch(e: HttpApiException) {
            call.respondText(
                text = e.message ?: "",
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.BadRequest,
            )
        }
    }
}
