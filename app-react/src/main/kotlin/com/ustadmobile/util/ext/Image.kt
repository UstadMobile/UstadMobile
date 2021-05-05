package com.ustadmobile.util.ext

import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.html.HTMLTag
import kotlinx.html.IMG

fun IMG.renderEntryThumbnailImg(item: ContentEntry): HTMLTag{
    src = when {
        !item.leaf -> "assets/folder.png"
        !item.thumbnailUrl.isNullOrEmpty() && item.leaf -> item.thumbnailUrl.toString()
        else -> "assets/book.png"
    }
    return this
}