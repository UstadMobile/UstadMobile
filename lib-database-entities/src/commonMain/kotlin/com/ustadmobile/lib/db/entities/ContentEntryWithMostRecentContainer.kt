package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ContentEntryWithMostRecentContainer: ContentEntry() {

    @Embedded
    var container: Container? = null

}