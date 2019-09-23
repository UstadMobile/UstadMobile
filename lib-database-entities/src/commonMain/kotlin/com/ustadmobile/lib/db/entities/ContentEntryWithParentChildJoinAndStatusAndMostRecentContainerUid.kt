package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ContentEntryWithParentChildJoinAndStatusAndMostRecentContainerUid : ContentEntryWithContentEntryStatus() {

    var mostRecentContainer: Long = 0

    @Embedded
    var contentEntryParentChildJoin: ContentEntryParentChildJoin? = null

}