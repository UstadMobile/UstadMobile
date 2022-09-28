package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable


@Serializable
data class PersonNameAndUid(var personUid: Long = 0L, var name: String = ""){

    override fun toString(): String {
        return name
    }
}