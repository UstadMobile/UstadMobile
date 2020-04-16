package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class GroupWithMemberCount : PersonGroup() {

    var memberCount: Int = 0
}
