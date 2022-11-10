package com.ustadmobile.lib.db.entities

import com.ustadmobile.door.annotation.ShallowCopy
import kotlinx.serialization.Serializable

@ShallowCopy
@Serializable
class PersonWithAccount : Person() {

    var currentPassword: String? = null

    var newPassword: String? = null

    var confirmedPassword:String? = null
}