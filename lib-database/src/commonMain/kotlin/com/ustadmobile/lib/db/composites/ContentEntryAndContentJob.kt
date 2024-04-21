package com.ustadmobile.lib.db.composites

import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import kotlinx.serialization.Serializable

/**
 *
 */
@Serializable
data class ContentEntryAndContentJob(
    val entry: ContentEntry? = null,
    val contentJob: ContentJob? = null,
    val contentJobItem: ContentEntryImportJob? = null,
)
