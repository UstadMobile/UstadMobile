package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class PersonParentJoinWithMinorPerson : PersonParentJoin(){

    @Embedded
    var minorPerson: Person? = null

}