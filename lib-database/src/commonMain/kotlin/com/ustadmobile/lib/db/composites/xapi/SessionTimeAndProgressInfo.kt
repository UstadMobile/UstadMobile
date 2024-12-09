package com.ustadmobile.lib.db.composites.xapi

import kotlinx.serialization.Serializable

@Serializable
data class SessionTimeAndProgressInfo(
    var timeStarted: Long = 0,
    var maxProgress: Int? = null,
    var maxScore: Float? = null,
)