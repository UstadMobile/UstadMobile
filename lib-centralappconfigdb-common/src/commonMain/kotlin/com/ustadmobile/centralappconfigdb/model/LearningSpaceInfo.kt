package com.ustadmobile.centralappconfigdb.model

import kotlinx.serialization.Serializable

@Serializable
data class LearningSpaceInfo(
    val url: String,
    val name: String,
    val description: String,
    val lastModified: Long,
)
