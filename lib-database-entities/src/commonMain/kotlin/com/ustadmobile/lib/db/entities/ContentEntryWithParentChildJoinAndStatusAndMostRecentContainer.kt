package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer : ContentEntry() {

    @Embedded
    var contentEntryStatus: ContentEntryStatus? = null

    @Embedded
    var mostRecentContainer: Container? = null

    @Embedded
    var contentEntryParentChildJoin: ContentEntryParentChildJoin? = null

    @Embedded
    var contentEntryProgress: ContentEntryProgress? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) return false
        if (!super.equals(other)) return false

        if (contentEntryStatus != other.contentEntryStatus) return false
        if (mostRecentContainer != other.mostRecentContainer) return false
        if (contentEntryParentChildJoin != other.contentEntryParentChildJoin) return false
        if (contentEntryProgress != other.contentEntryProgress) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (contentEntryStatus?.hashCode() ?: 0)
        result = 31 * result + (mostRecentContainer?.hashCode() ?: 0)
        result = 31 * result + (contentEntryParentChildJoin?.hashCode() ?: 0)
        result = 31 * result + (contentEntryProgress?.hashCode() ?: 0)
        return result
    }


}