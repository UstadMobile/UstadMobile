package com.ustadmobile.jsExt

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.lib.db.entities.Person

/**
 * Starts a local session as the given person. There is no need for the person to be registered
 */
suspend fun UstadAccountManager.startLocalSessionAsync(
    person: Person,
    endpointUrl: String,
    password: String = ""
) {
    val userSession = addSession(person, endpointUrl, password)
    activeEndpoint = Endpoint(endpointUrl)
    activeSession = userSession
}