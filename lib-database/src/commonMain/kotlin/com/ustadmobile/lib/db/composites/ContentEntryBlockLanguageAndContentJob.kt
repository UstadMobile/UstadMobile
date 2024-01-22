package com.ustadmobile.lib.db.composites

import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.Language
import kotlinx.serialization.Serializable

/**
 *
 */
@Serializable
data class ContentEntryBlockLanguageAndContentJob(
    val entry: ContentEntry? = null,
    val block: CourseBlock? = null,
    val language: Language? = null,
    val contentJob: ContentJob? = null,
    val contentJobItem: ContentEntryImportJob? = null,
)
