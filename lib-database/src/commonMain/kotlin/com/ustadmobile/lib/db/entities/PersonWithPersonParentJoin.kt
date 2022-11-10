package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import com.ustadmobile.door.annotation.ShallowCopy
import kotlinx.serialization.Serializable

@Serializable
@ShallowCopy
class PersonWithPersonParentJoin : Person(){

    @Embedded
    var parentJoin: PersonParentJoin? = null

}