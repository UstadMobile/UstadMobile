package com.ustadmobile.port.sharedse.contentformats.video

import com.ustadmobile.core.catalog.contenttype.VideoTypePlugin
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.port.sharedse.contentformats.ContentTypeFilePlugin

import java.io.File

class VideoTypeFilePlugin : VideoTypePlugin(), ContentTypeFilePlugin {

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