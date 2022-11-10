package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import com.ustadmobile.door.annotation.ShallowCopyable
import kotlinx.serialization.Serializable

@Serializable
@ShallowCopyable
class PersonWithPersonParentJoin : Person(){

    @Embedded
    var parentJoin: PersonParentJoin? = null

}