package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.ContentEntryPicture2

data class ContentEntryAndListDetail(
    @Embedded
    var contentEntry: ContentEntry? = null,

    @Embedded
    var picture: ContentEntryPicture2? = null,

    @Embedded
    var contentEntryParentChildJoin: ContentEntryParentChildJoin? = null,

    @Embedded
    var status: BlockStatus? = null,

)
