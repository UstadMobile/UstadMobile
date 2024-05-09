package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryPicture2
import kotlinx.serialization.Serializable

@Serializable
data class ContentEntryAndPicture(
    @Embedded
    var entry: ContentEntry? = null,
    @Embedded
    var picture: ContentEntryPicture2? = null,
)
