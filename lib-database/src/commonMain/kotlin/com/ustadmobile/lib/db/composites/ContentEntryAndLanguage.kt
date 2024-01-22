package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.Language
import kotlinx.serialization.Serializable

@Serializable
data class ContentEntryAndLanguage(
    @Embedded
    var contentEntry: ContentEntry? = null,
    @Embedded
    var language: Language? = null,
) {
    fun toContentEntryAndBlock(
        block: CourseBlock?
    ): ContentEntryBlockLanguageAndContentJob = ContentEntryBlockLanguageAndContentJob(
        entry = contentEntry,
        language = language,
        block = block,
    )
}
