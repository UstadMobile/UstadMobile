package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.door.ext.setBodyJson
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

/**
 * this usecase for a http api call to resend invite to particular contact
 *
 */
class ResendInviteUseCase(
    private val httpClient: HttpClient,
    private val learningSpace: LearningSpace,
    private val json: Json
) {

    suspend operator fun invoke(
        contact: String,
        personUid: Long
    ):String {
        try {

          val respose = httpClient.post("${learningSpace.url}api/resendinvite/sendcontact") {
                contentType(ContentType.Application.Json)
                setBodyJson(
                    json = json,
                    serializer = ResendInviteRequest.serializer(),
                    value = ResendInviteRequest(
                        contacts = contact,
                        personUid = personUid
                    )
                )
            }.bodyAsText()
            Napier.d { "ResendInviteUseCase:-   $respose" }
            return respose

        } catch (e: Throwable) {
            Napier.d { "ResendInviteUseCase:-  exception $e" }
            return e.message.toString()
        }

    }
}