package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class LangUidAndName{

    var langUid: Long = 0

    var langName: String? = null

    override fun toString(): String {
        return langName.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LangUidAndName

        if (langUid != other.langUid) return false
        if (langName != other.langName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = langUid.hashCode()
        result = 31 * result + (langName?.hashCode() ?: 0)
        return result
    }


}