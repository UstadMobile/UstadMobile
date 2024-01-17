package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

class ContentJobItemAndContentJob {

    @Embedded
    var contentJobItem: ContentEntryImportJob? = null

    @Embedded
    var contentJob: ContentJob? = null

}