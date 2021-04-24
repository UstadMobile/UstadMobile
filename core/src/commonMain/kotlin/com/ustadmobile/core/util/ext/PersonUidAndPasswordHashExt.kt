package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.lib.db.entities.UmAccount

fun PersonDao.PersonUidAndPasswordHash.toUmAccount(endpointUrl: String, username: String) = UmAccount(personUid = personUid,
    username = username, auth ="", endpointUrl = endpointUrl, firstName = firstNames,
    lastName = lastName, admin = admin)
