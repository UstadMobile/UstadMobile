package com.ustadmobile.core.viewmodel.contententry

import com.ustadmobile.lib.db.entities.ContentEntry
import dev.icerock.moko.resources.StringResource
import com.ustadmobile.core.MR

val ContentEntry.contentTypeStringResource: StringResource
    get() {
        return if(!leaf) {
            MR.strings.folder
        }else {
            when(contentTypeFlag) {
                ContentEntry.TYPE_COLLECTION -> MR.strings.collection
                ContentEntry.TYPE_EBOOK -> MR.strings.ebook
                ContentEntry.TYPE_INTERACTIVE_EXERCISE -> MR.strings.interactive
                ContentEntry.TYPE_VIDEO -> MR.strings.video
                ContentEntry.TYPE_AUDIO -> MR.strings.audio
                ContentEntry.TYPE_DOCUMENT -> MR.strings.document
                ContentEntry.TYPE_ARTICLE -> MR.strings.article
                else -> MR.strings.blank
            }
        }
    }
