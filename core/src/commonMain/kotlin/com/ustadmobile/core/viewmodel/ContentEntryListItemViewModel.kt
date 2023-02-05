package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import kotlin.jvm.JvmInline

val ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer.listItemUiState
    get() = ContentEntryListItemUiState(this)

@JvmInline
value class ContentEntryListItemUiState(
    val contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
) {

    val containerAlpha: Double
        get() = if(contentEntry.ceInactive) 0.5 else 1.0

    val progressVisible: Boolean
        get() = (contentEntry.scoreProgress?.progress ?: 0) > 0

    val descriptionVisible: Boolean
        get() = !contentEntry.description.isNullOrBlank()

    val mimetypeVisible: Boolean
        get() = contentEntry.leaf

    val scoreResultText: String
        get() = "(" + (contentEntry.scoreProgress?.resultScore ?: 0) + "/" +
                (contentEntry.scoreProgress?.resultMax ?: 0) + ")"

}