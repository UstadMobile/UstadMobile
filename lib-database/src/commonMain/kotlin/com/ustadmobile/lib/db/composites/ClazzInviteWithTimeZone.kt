package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ClazzInvite
import kotlinx.serialization.Serializable

@Serializable
data class ClazzInviteWithTimeZone(
    @Embedded
    var clazzInvite: ClazzInvite?= null,
    var timeZone: String?=null
)