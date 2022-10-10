package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.PersonUidAndPasswordHash
import com.ustadmobile.lib.db.entities.UmAccount

fun PersonUidAndPasswordHash.toUmAccount(endpointUrl: String, username: String) = UmAccount(personUid = personUid,
    username = username, auth ="", endpointUrl = endpointUrl, firstName = firstNames,
    lastName = lastName, admin = admin)
