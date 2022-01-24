package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

class ContentJobItemAndContentJob {

    @Embedded
    var contentJobItem: ContentJobItem? = null

    @Embedded
    var contentJob: ContentJob? = null

}