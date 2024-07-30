package com.ustadmobile.core.domain.xapi.starthttpsession

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.doorNodeIdHeader
import com.ustadmobile.door.ext.setBodyJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import kotlinx.serialization.json.Json

/**
 * Start an Xapi over http session for the web client: sends the request to the server (which is
 * processed using StartXapiSessionRoute) so that Xapi content can then use basic auth
 */
class StartXapiSessionOverHttpUseCaseJs(
    private val endpoint: Endpoint,
    private val httpClient: HttpClient,
    private val repo: UmAppDatabase,
    private val json: Json,
): StartXapiSessionOverHttpUseCase {

    override suspend fun invoke(
        xapiSession: XapiSession
    ): StartXapiSessionOverHttpUseCase.StartXapiSessionOverHttpResult {
        val result: StartXapiSessionOverHttpUseCase.StartXapiSessionOverHttpResult = httpClient.post(
            "${endpoint.url}api/xapi-ext/startSession"
        ) {
            doorNodeIdHeader(repo as DoorDatabaseRepository)
            setBodyJson(json, XapiSession.serializer(), xapiSession)
        }.body()

        return result
    }

}