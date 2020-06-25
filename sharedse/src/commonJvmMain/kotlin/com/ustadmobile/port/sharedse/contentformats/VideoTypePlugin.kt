package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.catalog.contenttype.VideoType
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.sharedse.contentformats.ContentTypePlugin

import java.io.File

class VideoTypePlugin : VideoType(), ContentTypePlugin {

    override fun getContentEntry(file: File): ContentEntryWithLanguage? {

        fileExtensions.find { file.name.endsWith(it) } ?: return null

        return ContentEntryWithLanguage().apply {
            this.title = ""
            this.description = ""
            this.leaf = true
        }
    }

   override fun isZipped(): Boolean{
        return false
    }
}