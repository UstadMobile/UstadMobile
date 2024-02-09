package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin

data class ContentEntryAndListDetail(
    @Embedded
    var contentEntry: ContentEntry? = null,

    @Embedded
    var contentEntryParentChildJoin: ContentEntryParentChildJoin? = null,
)
