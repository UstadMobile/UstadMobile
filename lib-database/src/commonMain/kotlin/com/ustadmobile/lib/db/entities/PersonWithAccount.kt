package com.ustadmobile.lib.db.entities

import com.ustadmobile.door.annotation.ShallowCopyable
import kotlinx.serialization.Serializable

@ShallowCopyable
@Serializable
class PersonWithAccount : Person() {

    var currentPassword: String? = null

    var newPassword: String? = null

    var confirmedPassword:String? = null
}