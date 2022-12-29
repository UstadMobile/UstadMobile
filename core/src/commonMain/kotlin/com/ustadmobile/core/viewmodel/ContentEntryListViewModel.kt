package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer

data class ContentEntryListUiState(

    val title: String = "",

    val editOptionVisible: Boolean = true,

    val contentEntryList: List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> = emptyList(),

    val isPickerMode: Boolean = false,

    val selectFolderVisible: Boolean = false,

) {
    val containerAlpha: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer)
    -> Double = {
            contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer ->
        if(contentEntry.ceInactive) 0.5 else 1.0
    }

    val progressVisible: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer)
    -> Boolean = {
            contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer ->
        (contentEntry.scoreProgress?.progress ?: 0) > 0
    }

    val descriptionVisible: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer)
    -> Boolean = {
            contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer ->
        !contentEntry.description.isNullOrBlank()
    }

    val mimetypeVisible: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer)
    -> Boolean = {
            contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer ->
        contentEntry.leaf
    }

    val scoreResultText: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer)
    -> String = {
            contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer ->
        "(" + (contentEntry.scoreProgress?.resultScore ?: 0) + "/" +
                (contentEntry.scoreProgress?.resultMax ?: 0) + ")"
    }

    val selectButtonVisible: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer)
    -> Boolean = {
            contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer ->
        isPickerMode  && (contentEntry.leaf || selectFolderVisible)
    }

}