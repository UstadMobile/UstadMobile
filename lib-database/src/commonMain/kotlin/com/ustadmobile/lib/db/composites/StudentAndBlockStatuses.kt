package com.ustadmobile.lib.db.composites

import kotlinx.serialization.Serializable

@Serializable
data class StudentAndBlockStatuses(
    val student: PersonAndClazzMemberListDetails,
    val blockStatuses: List<BlockStatus>
)
