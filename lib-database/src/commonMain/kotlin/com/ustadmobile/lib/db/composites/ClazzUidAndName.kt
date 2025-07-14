package com.ustadmobile.lib.db.composites

import kotlinx.serialization.Serializable

@Serializable
data class ClazzUidAndName(
    var clazzUid: Long = 0,
    var clazzName: String = ""
)