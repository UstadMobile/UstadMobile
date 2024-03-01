package com.ustadmobile.lib.db.composites

import kotlinx.serialization.Serializable

@Serializable
data class PermissionPair(
    var firstPermission: Boolean = false,
    var secondPermission: Boolean = false,
)
