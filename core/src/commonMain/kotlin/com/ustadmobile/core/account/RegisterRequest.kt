package com.ustadmobile.core.account

import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
import kotlinx.serialization.Serializable

/**
 * Object encapsulating all information needed from a client to register
 */
@Serializable
data class RegisterRequest(
    val person: Person,
    val newPassword: String,
    val parent: PersonParentJoin?,
    val endpointUrl: String,
    val langCode: String = "en"
)
