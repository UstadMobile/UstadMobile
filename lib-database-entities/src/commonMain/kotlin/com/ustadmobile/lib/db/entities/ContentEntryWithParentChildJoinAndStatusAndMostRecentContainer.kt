package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer : ContentEntryWithContentEntryStatus() {

    @Embedded
    var mostRecentContainer: Container? = null

    @Embedded
    var contentEntryParentChildJoin: ContentEntryParentChildJoin? = null

}