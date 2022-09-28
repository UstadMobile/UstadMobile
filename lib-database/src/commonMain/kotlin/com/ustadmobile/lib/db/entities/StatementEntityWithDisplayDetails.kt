package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class StatementEntityWithDisplayDetails : StatementEntity() {

    @Embedded
    var person: Person? = null

    @Embedded
    var xlangMapEntry: XLangMapEntry? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as StatementEntityWithDisplayDetails

        if (person != other.person) return false
        if (xlangMapEntry != other.xlangMapEntry) return false

        return true
    }

    override fun hashCode(): Int {
        var result = person?.hashCode() ?: 0
        result = 31 * result + (xlangMapEntry?.hashCode() ?: 0)
        return result
    }


}