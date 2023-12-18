package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ContentEntry

object ContentEntryTypeLabelConstants {

    val TYPE_LABEL_MESSAGE_IDS = listOf(
        MessageIdOption2(MR.strings.ebook, ContentEntry.TYPE_EBOOK),
        MessageIdOption2(MR.strings.video, ContentEntry.TYPE_VIDEO),
        MessageIdOption2(MR.strings.document, ContentEntry.TYPE_DOCUMENT),
        MessageIdOption2(MR.strings.article, ContentEntry.TYPE_ARTICLE),
        MessageIdOption2(MR.strings.collection, ContentEntry.TYPE_COLLECTION),
        MessageIdOption2(MR.strings.interactive, ContentEntry.TYPE_INTERACTIVE_EXERCISE),
        MessageIdOption2(MR.strings.audio, ContentEntry.TYPE_AUDIO)
    )
}