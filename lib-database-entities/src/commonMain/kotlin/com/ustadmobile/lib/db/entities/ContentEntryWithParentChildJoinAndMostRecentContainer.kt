package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ContentEntryWithParentChildJoinAndMostRecentContainer: ContentEntry() {
    @Embedded
    var mostRecentContainer: Container? = null

    @Embedded
    var contentEntryParentChildJoin: ContentEntryParentChildJoin? = null

}