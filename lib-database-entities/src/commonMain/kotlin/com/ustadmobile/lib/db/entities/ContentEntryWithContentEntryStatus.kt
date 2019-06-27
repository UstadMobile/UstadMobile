package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import com.ustadmobile.lib.database.annotation.UmEmbedded

open class ContentEntryWithContentEntryStatus() : ContentEntry() {

    @UmEmbedded
    @Embedded
    var contentEntryStatus: ContentEntryStatus? = null
}
