package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class PersonNameWithClazzName {

    var clazzMemberUid: Long = 0
    var personUid: Long = 0
    var firstNames: String? = null
    var lastName: String? = null
    var num: Int = 0
    var clazzName: String? = null

}
