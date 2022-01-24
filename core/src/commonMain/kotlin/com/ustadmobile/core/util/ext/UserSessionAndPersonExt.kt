package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.lib.db.entities.UserSessionAndPerson

fun UserSessionAndPerson.withEndpoint(endpoint: Endpoint) =
    UserSessionWithPersonAndEndpoint(
        userSession ?: throw IllegalArgumentException("session withendpoint : usersession must not be null"),
        person ?: throw IllegalArgumentException("session withendpoint: person msut not be null"),
        endpoint)