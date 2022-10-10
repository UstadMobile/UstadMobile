package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class PersonWithAccount : Person() {

    var currentPassword: String? = null

    var newPassword: String? = null

    var confirmedPassword:String? = null
}