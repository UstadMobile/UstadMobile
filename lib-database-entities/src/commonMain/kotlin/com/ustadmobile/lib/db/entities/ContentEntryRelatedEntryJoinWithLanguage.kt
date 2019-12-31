package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ContentEntryRelatedEntryJoinWithLanguage : ContentEntryRelatedEntryJoin() {

    @Embedded
    var language: Language? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as ContentEntryRelatedEntryJoinWithLanguage

        if (language != other.language) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (language?.hashCode() ?: 0)
        return result
    }


}
