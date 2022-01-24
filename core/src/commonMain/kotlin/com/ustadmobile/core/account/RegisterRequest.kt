package com.ustadmobile.core.account

import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonWithAccount
import kotlinx.serialization.Serializable

/**
 * Object encapsulating all information needed from a client to register
 */
@Serializable
data class RegisterRequest(var person: PersonWithAccount,
                           var parent: PersonParentJoin?,
                           var endpointUrl: String,
                           var langCode: String = "en"
)
