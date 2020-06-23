package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.catalog.contenttype.VideoType
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.sharedse.contentformats.ContentTypePlugin

import java.io.File

class VideoTypePlugin : VideoType(), ContentTypePlugin {

    override fun getContentEntry(file: File): ContentEntryWithLanguage? {
        return ContentEntryWithLanguage().apply {
            this.title = "Video Title"
            this.description = "Video Description"
            this.leaf = true
            val language = Language()
            language.iso_639_1_standard = "en"
            this.language = language
        }
    }
}