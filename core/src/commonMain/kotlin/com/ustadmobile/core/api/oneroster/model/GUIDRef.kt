package com.ustadmobile.core.api.oneroster.model

import kotlinx.serialization.Serializable


@Serializable
data class GUIDRef(
    val href: String,
    val sourcedId: String,
    val type: GuidRefType,
) {


}