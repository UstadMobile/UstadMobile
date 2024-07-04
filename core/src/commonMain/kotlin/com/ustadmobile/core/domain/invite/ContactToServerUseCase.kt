package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.doorNodeAndVersionHeaders
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText

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
        }.bodyAsText().toLong()
    }
}