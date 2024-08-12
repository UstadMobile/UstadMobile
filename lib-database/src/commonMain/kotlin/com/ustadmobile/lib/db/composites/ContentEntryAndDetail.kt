package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryPicture2
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import kotlinx.serialization.Serializable

@Serializable
data class ContentEntryAndDetail(
    @Embedded
    var entry: ContentEntry? = null,

    @Embedded
    var latestVersion: ContentEntryVersion? = null,

    @Embedded
    var picture: ContentEntryPicture2? = null,

    @Embedded
    var status: BlockStatus? = null,
)