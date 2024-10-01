package com.ustadmobile.core.account

data class PassKeyPromptData(
    val username: String,
    val personUid: Long,
    val doorNodeId: String,
    val usStartTime: Long,
    val serverUrl:String
)