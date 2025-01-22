package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzInvite
import kotlinx.serialization.Serializable

@Serializable
data class ClazzInviteAndClazz(
    @Embedded
    var clazzInvite: ClazzInvite? = null,
    @Embedded
    var clazz: Clazz? = null,
)