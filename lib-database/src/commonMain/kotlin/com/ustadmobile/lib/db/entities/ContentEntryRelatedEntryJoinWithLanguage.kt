package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ContentEntryRelatedEntryJoinWithLanguage() : ContentEntryRelatedEntryJoin() {

    @Embedded
    var language: Language? = null

    constructor(relatedEntryJoin: ContentEntryRelatedEntryJoin) : this() {
        super.cerejUid = relatedEntryJoin.cerejUid
        super.comment = relatedEntryJoin.comment
        super.relType = relatedEntryJoin.relType
        super.cerejRelLanguageUid = relatedEntryJoin.cerejRelLanguageUid
        super.cerejContentEntryUid = relatedEntryJoin.cerejContentEntryUid
        super.cerejRelatedEntryUid = relatedEntryJoin.cerejRelatedEntryUid
    }

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
