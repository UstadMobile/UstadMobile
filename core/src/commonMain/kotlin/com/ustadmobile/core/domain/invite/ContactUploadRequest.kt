package com.ustadmobile.core.domain.invite

import kotlinx.serialization.Serializable


@Serializable
data class ContactUploadRequest(
    val contacts: List<String>,
    val clazzUid: Long,
    val role: Long,
    val personUid: Long
)