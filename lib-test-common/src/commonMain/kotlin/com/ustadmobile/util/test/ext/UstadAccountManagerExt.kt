package com.ustadmobile.util.test.ext

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.lib.db.entities.Person

suspend fun UstadAccountManager.startLocalTestSessionAsync(
    person: Person,
    endpointUrl: String,
    password: String = ""
) {
    val userSession = addSession(person, endpointUrl, password)
    currentSession = userSession
}