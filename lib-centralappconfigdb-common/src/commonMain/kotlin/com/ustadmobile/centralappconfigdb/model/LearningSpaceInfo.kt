package com.ustadmobile.centralappconfigdb.model

import kotlinx.serialization.Serializable

@Serializable
data class LearningSpaceInfo(
    val url: String,
    val name: String,
    val description: String,
//    val subdomain: String,
//    val organisationLogo: String,
//    val adminContact: String,
    val lastModified: Long,
)
