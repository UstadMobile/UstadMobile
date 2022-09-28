package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class PersonGroupWithMemberCount : PersonGroup() {

    var memberCount: Int = 0
}
