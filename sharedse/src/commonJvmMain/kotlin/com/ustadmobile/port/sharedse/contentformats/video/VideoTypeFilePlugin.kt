package com.ustadmobile.port.sharedse.contentformats.video

import com.ustadmobile.core.catalog.contenttype.VideoTypePlugin
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.port.sharedse.contentformats.ContentTypeFilePlugin
import com.ustadmobile.port.sharedse.contentformats.ContentTypeUtil
import java.io.File
import java.net.URI

class VideoTypeFilePlugin : VideoTypePlugin(), ContentTypeFilePlugin {

    override fun getContentEntry(file: File): ContentEntryWithLanguage? {

        fileExtensions.find { file.name.endsWith(it, true) } ?: return null

        return ContentEntryWithLanguage().apply {
            this.title = file.nameWithoutExtension
            this.leaf = true
            this.contentTypeFlag = ContentEntry.TYPE_VIDEO
        }
    }

    override fun importMode(): Int {
        return ContentTypeUtil.FILE
    }
}