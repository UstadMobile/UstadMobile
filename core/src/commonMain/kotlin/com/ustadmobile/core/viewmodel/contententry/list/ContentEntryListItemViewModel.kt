package com.ustadmobile.core.viewmodel.contententry.list

import com.ustadmobile.lib.db.entities.ContentEntry
import kotlin.jvm.JvmInline

val ContentEntry.listItemUiState
    get() = ContentEntryListItemUiState(this)

@JvmInline
value class ContentEntryListItemUiState(
    val contentEntry: ContentEntry,
) {

    val containerAlpha: Double
        get() = if(contentEntry.ceInactive) 0.5 else 1.0

    val progressVisible: Boolean
        get() = false

    val descriptionVisible: Boolean
        get() = !contentEntry.description.isNullOrBlank()

    val mimetypeVisible: Boolean
        get() = contentEntry.leaf

}