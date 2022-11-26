package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.ContentEntryDetailOverviewUiState
import com.ustadmobile.lib.db.entities.*
import csstype.px
import mui.material.*
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props

external interface ContentEntryDetailOverviewProps : Props {

    var uiState: ContentEntryDetailOverviewUiState

    var onClickDownload: () -> Unit

    var onClickOpen: () -> Unit

    var onClickMarkComplete: () -> Unit

    var onClickDelete: () -> Unit

    var onClickManageDownload: () -> Unit

    var onClickTranslation: (ContentEntryRelatedEntryJoinWithLanguage) -> Unit

}

val ContentEntryDetailOverviewPreview = FC<Props> {
    val strings = useStringsXml()
    ContentEntryDetailOverviewComponent2 {
        uiState = ContentEntryDetailOverviewUiState(
            contentEntry = ContentEntryWithMostRecentContainer().apply {
                title = "Content Title"
                author = "Author"
                publisher = "Publisher"
                licenseName = "BY_SA"
                container = Container().apply {
                    fileSize = 50
                }
                description = "Content Description"
            },
            scoreProgress = ContentEntryStatementScoreProgress().apply {
                /*@FloatRange(from = 0.0, to = 1.0)*/
                progress = 4

                resultScore = 4
                resultMax = 40
            },
            contentEntryButtons = ContentEntryButtonModel().apply {
                showDownloadButton = true
                showOpenButton = true
                showDeleteButton = true
                showManageDownloadButton = true
            },
            locallyAvailable = true,
            markCompleteVisible = true,
            translationVisibile = true,
            availableTranslationsMap = mapOf(
                1 to ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "English"
                    }
                },
                2 to ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "French"
                    }
                },
                3 to ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "Persian"
                    }
                }
            )
        )
    }
}

private val ContentEntryDetailOverviewComponent2 = FC<ContentEntryDetailOverviewProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"


        Stack {
            spacing = responsive(10.px)

            ContentDetails { uiState = props.uiState }

            if (props.uiState.contentEntryButtons?.showDownloadButton == true) {
                Button {
                    onClick = { props.onClickDownload }
                    variant = ButtonVariant.contained

                    +strings[MessageID.download].uppercase()
                }
            }

            if (props.uiState.contentEntryButtons?.showOpenButton == true){
                Button {
                    onClick = { props.onClickOpen }
                    variant = ButtonVariant.contained

                    + strings[MessageID.open].uppercase()
                }
            }

            if (props.uiState.locallyAvailable) {
                LocallyAvailableRow {}
            }

            Divider { orientation = Orientation.horizontal }

            QuickActionBarsRow {
                uiState = props.uiState
                onClickMarkComplete = props.onClickMarkComplete
                onClickDelete = props.onClickDelete
                onClickManageDownload = props.onClickManageDownload
            }

            + "Locally Available"

            Divider { orientation = Orientation.horizontal }

            + (props.uiState.contentEntry?.description ?: "")

            Divider { orientation = Orientation.horizontal }

            if (props.uiState.translationVisibile){
                + strings[MessageID.also_available_in]
            }

            AvailableTranslations {
                uiState = props.uiState
                onClickTranslation = props.onClickTranslation
            }
        }
    }
}

private val ContentDetails = FC<ContentEntryDetailOverviewProps> { props ->

    Stack {
        direction = responsive(StackDirection.row)
        spacing = responsive(10.px)

        LeftColumn {
            uiState = props.uiState
        }

        RightColumn{
            uiState = props.uiState
        }
    }
}

private val LeftColumn = FC<ContentEntryDetailOverviewProps> { props ->

    Stack {
    }
}

private val RightColumn = FC<ContentEntryDetailOverviewProps> { props ->

}

private val LocallyAvailableRow = FC<ContentEntryDetailOverviewProps> { props ->

}

private val QuickActionBarsRow = FC<ContentEntryDetailOverviewProps> { props ->

}

private val AvailableTranslations = FC<ContentEntryDetailOverviewProps> { props ->

    List{
        props.uiState.availableTranslationsMap.forEach { (_, value)->
            ListItem{
                Button {
                    onClick = {
                        props.onClickTranslation(value)
                    }
                    variant = ButtonVariant.text

                    + (value.language?.name ?: "")
                }
            }
        }
    }
}