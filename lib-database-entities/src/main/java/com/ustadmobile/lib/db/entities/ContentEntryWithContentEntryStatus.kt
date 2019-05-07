package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEmbedded

open class ContentEntryWithContentEntryStatus : ContentEntry() {

    @UmEmbedded
    var contentEntryStatus: ContentEntryStatus? = null
}
