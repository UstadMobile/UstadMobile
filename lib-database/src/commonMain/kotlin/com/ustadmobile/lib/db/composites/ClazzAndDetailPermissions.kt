package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.Clazz
import kotlinx.serialization.Serializable

@Serializable
data class ClazzAndDetailPermissions(
    @Embedded
    var clazz: Clazz? = null,
    var hasAttendancePermission: Boolean = false,
    var hasViewMembersPermission: Boolean = false,
    var hasLearningRecordPermission: Boolean = false,
)
