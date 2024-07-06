package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.doorNodeAndVersionHeaders
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post


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
    private val repo: UmAppDatabase?,
) {
    suspend operator fun invoke(
        contacts: List<String>,
        clazzUid: Long,
        role: Long,
        personUid: Long
    ) {

        httpClient.post("${endpoint.url}api/inviteuser/sendcontacts") {
            parameter("role", role.toString())
            parameter("personUid", personUid.toString())
            parameter("clazzUid", clazzUid.toString())
            parameter("contacts", contacts.toString())
            doorNodeAndVersionHeaders(repo as DoorDatabaseRepository)
        }
    }
}