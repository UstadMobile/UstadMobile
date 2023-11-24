package com.ustadmobile.lib.db.composites

import kotlinx.serialization.Serializable

@Serializable
data class AssignmentSubmitterUidAndName(
    var name: String? = null,
    var submitterUid: Long = 0,
)
