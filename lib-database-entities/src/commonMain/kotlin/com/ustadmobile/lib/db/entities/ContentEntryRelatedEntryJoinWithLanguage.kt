package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ContentEntryRelatedEntryJoinWithLanguage() {

    var cerejContentEntryUid: Long = 0

    var cerejRelatedEntryUid: Long = 0

    var languageName: String? = null


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ContentEntryRelatedEntryJoinWithLanguage

        if (cerejContentEntryUid != other.cerejContentEntryUid) return false
        if (cerejRelatedEntryUid != other.cerejRelatedEntryUid) return false
        if (languageName != other.languageName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cerejContentEntryUid.hashCode()
        result = 31 * result + cerejRelatedEntryUid.hashCode()
        result = 31 * result + (languageName?.hashCode() ?: 0)
        return result
    }


}
