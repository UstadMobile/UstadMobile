package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
data class UmAccount(var personUid: Long, var username: String?, var auth: String?, var endpointUrl: String?)