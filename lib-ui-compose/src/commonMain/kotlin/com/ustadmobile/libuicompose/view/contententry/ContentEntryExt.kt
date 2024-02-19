package com.ustadmobile.libuicompose.view.contententry

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.ui.graphics.vector.ImageVector
import com.ustadmobile.lib.db.entities.ContentEntry

val ContentEntry.contentTypeImageVector: ImageVector
    get() {
        return if(!leaf) {
            Icons.Filled.Folder
        }else {
            when(contentTypeFlag) {
                ContentEntry.TYPE_EBOOK -> Icons.Filled.Book
                ContentEntry.TYPE_VIDEO ->  Icons.Filled.SmartDisplay
                ContentEntry.TYPE_DOCUMENT -> Icons.Filled.TextSnippet
                ContentEntry.TYPE_ARTICLE -> Icons.Filled.Article
                ContentEntry.TYPE_COLLECTION -> Icons.Filled.Collections
                ContentEntry.TYPE_INTERACTIVE_EXERCISE -> Icons.Filled.TouchApp
                ContentEntry.TYPE_AUDIO -> Icons.Filled.Audiotrack
                else -> Icons.Filled.Article
            }
        }
    }