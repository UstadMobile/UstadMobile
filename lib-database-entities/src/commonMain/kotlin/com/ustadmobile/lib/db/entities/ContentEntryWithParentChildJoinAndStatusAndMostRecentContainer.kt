package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer : ContentEntry() {

    @Embedded
    var mostRecentContainer: Container? = null

    @Embedded
    var contentEntryParentChildJoin: ContentEntryParentChildJoin? = null

    @Embedded
    var scoreProgress: ContentEntryStatementScoreProgress? = null

    // TODO cleanup
   var assignmentContentWeight: Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) return false
        if (!super.equals(other)) return false

        if (mostRecentContainer != other.mostRecentContainer) return false
        if (contentEntryParentChildJoin != other.contentEntryParentChildJoin) return false
        if (scoreProgress != other.scoreProgress) return false
        if(assignmentContentWeight != other.assignmentContentWeight) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (mostRecentContainer?.hashCode() ?: 0)
        result = 31 * result + (contentEntryParentChildJoin?.hashCode() ?: 0)
        result = 31 * result + (scoreProgress?.hashCode() ?: 0)
        result = 31 * result + (assignmentContentWeight.hashCode())
        return result
    }


}