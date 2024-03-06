package com.ustadmobile.lib.db.composites

import kotlinx.serialization.Serializable

@Serializable
data class PermissionTriple(
    var firstPermission: Boolean = false,
    var secondPermission: Boolean = false,
    var thirdPermission: Boolean = false,
)