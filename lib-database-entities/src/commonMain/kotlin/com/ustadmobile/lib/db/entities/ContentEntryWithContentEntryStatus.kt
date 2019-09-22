package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
open class ContentEntryWithContentEntryStatus() : ContentEntry() {

    @Embedded
    var contentEntryStatus: ContentEntryStatus? = null
}
