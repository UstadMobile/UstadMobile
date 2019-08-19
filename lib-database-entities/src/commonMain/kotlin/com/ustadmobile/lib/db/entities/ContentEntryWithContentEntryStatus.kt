package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

open class ContentEntryWithContentEntryStatus() : ContentEntry() {

    @Embedded
    var contentEntryStatus: ContentEntryStatus? = null
}
