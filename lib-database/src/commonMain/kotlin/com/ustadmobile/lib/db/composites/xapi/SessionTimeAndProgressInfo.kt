package com.ustadmobile.lib.db.composites.xapi

import kotlinx.serialization.Serializable

@Serializable
data class SessionTimeAndProgressInfo(
    var contextRegistrationHi: Long = 0,
    var contextRegistrationLo: Long = 0,
    var timeStarted: Long = 0,
    var maxProgress: Int? = null,
    var maxScore: Float? = null,
    var isCompleted: Boolean = false,
    var isSuccessful: Boolean? = null,
)