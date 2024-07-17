package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
data class ClazzInviteWithTimeZone(
    @Embedded
    var clazzInvite: ClazzInvite?= null,
    var timeZone: String?=null
)