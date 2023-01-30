package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ContentEntry

object ContentEntryTypeLabelConstants {

    val TYPE_LABEL_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.ebook, ContentEntry.TYPE_EBOOK),
        MessageIdOption2(MessageID.video, ContentEntry.TYPE_VIDEO),
        MessageIdOption2(MessageID.document, ContentEntry.TYPE_DOCUMENT),
        MessageIdOption2(MessageID.article, ContentEntry.TYPE_ARTICLE),
        MessageIdOption2(MessageID.collection, ContentEntry.TYPE_COLLECTION),
        MessageIdOption2(MessageID.interactive, ContentEntry.TYPE_INTERACTIVE_EXERCISE),
        MessageIdOption2(MessageID.audio, ContentEntry.TYPE_AUDIO)
    )
}