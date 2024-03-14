package com.ustadmobile.lib.db.composites

import kotlinx.serialization.Serializable

@Serializable
data class AssignmentPermissionAndActiveUserSubmitterUid(
    var canMark: Boolean = false,
    var canView: Boolean = false,
    var canModerate: Boolean = false,
    var activeUserSubmitterUid: Long = 0L,
)
