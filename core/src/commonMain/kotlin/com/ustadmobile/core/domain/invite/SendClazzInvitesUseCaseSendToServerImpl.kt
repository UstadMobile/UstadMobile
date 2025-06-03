package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.door.ext.setBodyJson
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

/**
 * Client side implementation - simply sends to the learning space server.
 */
class SendClazzInvitesUseCaseSendToServerImpl(
    private val httpClient: HttpClient,
    private val learningSpace: LearningSpace,
    private val json: Json
): SendClazzInvitesUseCase {

    override suspend operator fun invoke(
        request: SendClazzInvitesUseCase.SendClazzInvitesRequest
    ) {
        try {
            httpClient.post("${learningSpace.url}api/invite/sendclazzinvites") {
                contentType(ContentType.Application.Json)
                setBodyJson(
                    json = json,
                    serializer = SendClazzInvitesUseCase.SendClazzInvitesRequest.serializer(),
                    value = request
                )
            }
            Napier.d { "ContactToServerUseCase: sent OK" }
        } catch (e: Throwable) {
            Napier.d { "ContactToServerUseCase:-  exception $e" }
            throw e
        }
    }
}