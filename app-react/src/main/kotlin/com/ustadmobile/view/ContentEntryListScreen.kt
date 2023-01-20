package com.ustadmobile.view

import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.ContentEntryListUiState
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.mui.components.UstadContentEntryListItem
import csstype.*
import mui.material.*
import mui.material.List
import mui.system.responsive
import react.FC
import react.Props

external interface ContentEntryListScreenProps : Props {

    var uiState: ContentEntryListUiState

    var onClickContentEntry: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

    var onClickDownloadContentEntry: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

}

val ContentEntryListScreenPreview = FC<Props> {
    val strings = useStringsXml()
    ContentEntryListScreenComponent2 {
        uiState = ContentEntryListUiState(
            contentEntryList = listOf(
                ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
                    contentEntryUid = 1
                    leaf = false
                    ceInactive = true
                    scoreProgress = ContentEntryStatementScoreProgress().apply {
                        progress = 10
                        penalty = 20
                    }
                    contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                    title = "Content Title 1"
                    description = "Content Description 1"
                },
                ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
                    contentEntryUid = 2
                    leaf = true
                    ceInactive = false
                    contentTypeFlag = ContentEntry.TYPE_DOCUMENT
                    title = "Content Title 2"
                    description = "Content Description 2"
                }
            ),
        )
    }
}


private val ContentEntryListScreenComponent2 = FC<ContentEntryListScreenProps> { props ->
    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(20.px)

            List{
                props.uiState.contentEntryList.forEach { entry ->

                    UstadContentEntryListItem {
                        onClickContentEntry = props.onClickContentEntry
                        onClickDownloadContentEntry = props.onClickDownloadContentEntry
                        contentEntry = entry
                    }
                }
            }
        }
    }
}
