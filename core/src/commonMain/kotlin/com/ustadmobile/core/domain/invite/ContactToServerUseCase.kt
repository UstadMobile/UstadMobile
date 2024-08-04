package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.setBodyJson
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

import kotlinx.serialization.encodeToString

/**
 * this usecase for a http api call to send all contact to server to send invite links to particular contacts
 *
 * @param contacts list of contacts like email , phone number or username
 * @param clazzUid the clazzuid for which person will join course
 * @param personUid the personUid from which invitation is sent
 * @param role the roleid from which invitation is sent
 */
class ContactToServerUseCase(
    private val httpClient: HttpClient,
    private val endpoint: Endpoint,
    private val json: Json
) {
    suspend operator fun invoke(
        contacts: List<String>,
        clazzUid: Long,
        role: Long,
        personUid: Long
    ):String {
        try {

          val respose=  httpClient.post("${endpoint.url}api/inviteuser/sendcontacts") {
                contentType(ContentType.Application.Json)
                setBodyJson(
                    json = json,
                    serializer = ContactUploadRequest.serializer(),
                    value = ContactUploadRequest(
                        contacts = contacts,
                        clazzUid = clazzUid,
                        role = role,
                        personUid = personUid
                    )
                )
            }.bodyAsText()
            Napier.d { "ContactToServerUseCase:-   $respose" }
            return respose

        } catch (e: Throwable) {
            Napier.d { "ContactToServerUseCase:-  exception $e" }
            return e.message.toString()
        }

    }
}