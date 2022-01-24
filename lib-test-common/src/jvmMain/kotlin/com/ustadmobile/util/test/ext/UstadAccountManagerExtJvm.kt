package com.ustadmobile.util.test.ext

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.runBlocking

fun UstadAccountManager.startLocalTestSessionBlocking(
    person: Person,
    endpointUrl: String,
    password: String = "secret"
) {
    runBlocking {
        startLocalTestSessionAsync(person, endpointUrl, password)
    }
}