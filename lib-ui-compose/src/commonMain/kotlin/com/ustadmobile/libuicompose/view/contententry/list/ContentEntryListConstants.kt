package com.ustadmobile.libuicompose.view.contententry.list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.TouchApp
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.ContentEntry

object ClazzAssignmentConstants {

    @JvmField
    val CONTENT_ENTRY_TYPE_ICON_MAP = mapOf(
        ContentEntry.TYPE_EBOOK to Icons.Filled.Book,
        ContentEntry.TYPE_VIDEO to Icons.Filled.SmartDisplay,
        ContentEntry.TYPE_DOCUMENT to Icons.Filled.TextSnippet,
        ContentEntry.TYPE_ARTICLE to Icons.Filled.Article,
        ContentEntry.TYPE_COLLECTION to Icons.Filled.Collections,
        ContentEntry.TYPE_INTERACTIVE_EXERCISE to Icons.Filled.TouchApp,
        ContentEntry.TYPE_AUDIO to Icons.Filled.Audiotrack
    )

    @JvmField
    val CONTENT_ENTRY_TYPE_LABEL_MAP = mapOf(
        ContentEntry.TYPE_EBOOK to MR.strings.ebook,
        ContentEntry.TYPE_VIDEO to MR.strings.video,
        ContentEntry.TYPE_DOCUMENT to MR.strings.document,
        ContentEntry.TYPE_ARTICLE to MR.strings.article,
        ContentEntry.TYPE_COLLECTION to MR.strings.collection,
        ContentEntry.TYPE_INTERACTIVE_EXERCISE to MR.strings.interactive,
        ContentEntry.TYPE_AUDIO to MR.strings.audio
    )

}
