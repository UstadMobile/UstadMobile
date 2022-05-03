package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class CourseGroupMemberPerson: Person() {

    @Embedded
    var member: CourseGroupMember? = null

}