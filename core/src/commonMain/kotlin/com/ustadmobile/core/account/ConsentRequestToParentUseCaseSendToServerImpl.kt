package com.ustadmobile.core.account

import com.ustadmobile.core.account.SendConsentRequestToParentUseCase.SendConsentRequestToParentRequest
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
class ConsentRequestToParentUseCaseSendToServerImpl(
    private val httpClient: HttpClient,
    private val learningSpace: LearningSpace,
    private val json: Json
): SendConsentRequestToParentUseCase {

    override suspend operator fun invoke(
        request: SendConsentRequestToParentRequest
    ) {
        try {
            httpClient.post("${learningSpace.url}api/account/sendcensentrequest") {
                contentType(ContentType.Application.Json)
                setBodyJson(
                    json = json,
                    serializer = SendConsentRequestToParentRequest.serializer(),
                    value = request
                )
            }
            Napier.d { "SendConsentRequestToParentUseCase: sent OK" }
        } catch (e: Throwable) {
            Napier.d { "SendConsentRequestToParentUseCase:-  exception $e" }
            throw e
        }
    }
}