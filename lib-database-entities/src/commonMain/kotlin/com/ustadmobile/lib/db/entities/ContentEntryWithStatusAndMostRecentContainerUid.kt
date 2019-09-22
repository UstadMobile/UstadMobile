package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ContentEntryWithStatusAndMostRecentContainerUid() : ContentEntryWithContentEntryStatus() {

    var mostRecentContainer: Long = 0

}
