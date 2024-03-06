package com.ustadmobile.lib.db.composites

import kotlinx.serialization.Serializable

@Serializable
data class PersonNames(
    var firstNames: String? = null,
    var lastName: String? = null,
) {

    override fun toString(): String {
        return "${firstNames ?: ""} ${lastName ?: ""}"
    }
}
