package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
data class UmAccount(var personUid: Long,
                     var username: String? = null,
                     var auth: String? = null,
                     var endpointUrl: String = "",
                     var firstName: String? = null,
                     var lastName: String ? = null,
                     var admin: Boolean = false)
