package com.ustadmobile.lib.db.composites

import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.Language
import kotlinx.serialization.Serializable

/**
 *
 */
@Serializable
data class ContentEntryAndBlockLanguageAndContentJob(
    val entry: ContentEntry? = null,
    val block: CourseBlock? = null,
    val language: Language? = null,
    val contentJob: ContentJob? = null,
    val contentJobItem: ContentJobItem? = null,
)
