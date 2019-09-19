package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class UmAccount(var personUid: Long, var username: String?, var auth: String?, var endpointUrl: String?)