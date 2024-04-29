package com.ustadmobile.view.contententry

import com.ustadmobile.lib.db.entities.ContentEntry
import mui.icons.material.SvgIconComponent

import mui.icons.material.Folder as FolderIcon
import mui.icons.material.Book as BookIcon
import mui.icons.material.SmartDisplay as SmartDisplayIcon
import mui.icons.material.TextSnippet as TextSnippetIcon
import mui.icons.material.Article as ArticleIcon
import mui.icons.material.Collections as CollectionsIcon
import mui.icons.material.TouchApp as TouchAppIcon
import mui.icons.material.Audiotrack as AudioTrackIcon

fun ContentEntry.contentTypeIconComponent(): SvgIconComponent? {
    return if(!leaf) {
        FolderIcon
    }else {
        when(contentTypeFlag) {
            ContentEntry.TYPE_EBOOK -> BookIcon
            ContentEntry.TYPE_VIDEO ->  SmartDisplayIcon
            ContentEntry.TYPE_DOCUMENT -> TextSnippetIcon
            ContentEntry.TYPE_ARTICLE -> ArticleIcon
            ContentEntry.TYPE_COLLECTION -> CollectionsIcon
            ContentEntry.TYPE_INTERACTIVE_EXERCISE -> TouchAppIcon
            ContentEntry.TYPE_AUDIO -> AudioTrackIcon
            else -> null
        }
    }

}