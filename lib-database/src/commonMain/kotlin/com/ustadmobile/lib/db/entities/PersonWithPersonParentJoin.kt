package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class PersonWithPersonParentJoin : Person(){

    @Embedded
    var parentJoin: PersonParentJoin? = null

}