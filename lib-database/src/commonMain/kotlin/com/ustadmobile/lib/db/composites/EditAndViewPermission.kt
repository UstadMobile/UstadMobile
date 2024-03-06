package com.ustadmobile.lib.db.composites

import kotlinx.serialization.Serializable

@Serializable
data class EditAndViewPermission(
    var hasViewPermission: Boolean = false,
    var hasEditPermission: Boolean = false,
)