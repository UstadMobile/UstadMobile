package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.ContentEntryDetailOverviewUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.UstadQuickActionButton
import csstype.*
import mui.icons.material.*
import mui.material.Icon
import mui.material.*
import mui.material.List
import mui.material.styles.TypographyVariant
import mui.material.Stack
import mui.material.StackDirection
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.create

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
            availableTranslationsMap = listOf(
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "English"
                    }
                },
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "French"
                    }
                },
                ContentEntryRelatedEntryJoinWithLanguage().apply {
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

        Box {
            sx {
                width = 110.px
                height = 110.px
                alignItems = AlignItems.center
            }
            + BookOutlined.create()
        }

        Stack {
            direction = responsive(StackDirection.row)
            sx {
                alignItems = AlignItems.center
            }

            LinearProgress {
                sx {
                    width = 110.px
                    height = 6.px
                }
                value = 50
                variant = LinearProgressVariant.determinate
            }

            Icon {
                sx {
                    width = 45.px
                    color = Color("green")
                }
               + CheckCircle.create()
            }
        }
    }
}

private val RightColumn = FC<ContentEntryDetailOverviewProps> { props ->

    val strings = useStringsXml()

    Stack {

        Typography {
            + (props.uiState.contentEntry?.title ?: "")
            variant = TypographyVariant.h4
        }

        if (props.uiState.authorVisible){
            Typography {
                + (props.uiState.contentEntry?.author ?: "")
            }
        }

        if (props.uiState.publisherVisible){
            Typography {
                + (props.uiState.contentEntry?.publisher ?: "")
            }
        }

        if (props.uiState.licenseNameVisible){
            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(10.px)
                sx {
                    alignItems = AlignItems.center
                }

                Typography {
                    + strings[MessageID.entry_details_license]
                }

                Typography {
                    variant = TypographyVariant.h6

                    +(props.uiState.contentEntry?.licenseName ?: "")
                }
            }
        }

        Stack {
            direction = responsive(StackDirection.row)
            spacing = responsive(16.px)

            if (props.uiState.fileSizeVisible){
                Typography {
                    + (props.uiState.contentEntry?.container?.fileSize.toString())
                }
            }

            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(5.px)

                Box {
                    sx {
                        width = 18.px
                        height = 18.px
                        alignContent = AlignContent.center
                        display = Display.block
                    }
                    + EmojiEvents.create()
                }

                Typography {
                    + (props.uiState.scoreProgress?.progress.toString())
                }
            }


            Typography {
                + ("(${(props.uiState.scoreProgress?.resultScore ?: "")}" +
                        "/${(props.uiState.scoreProgress?.resultMax ?: "")})" )
            }
        }
    }
}

private val LocallyAvailableRow = FC<ContentEntryDetailOverviewProps> { _ ->

    val strings = useStringsXml()

    Stack {
        direction = responsive(StackDirection.row)

        + LocationOn.create()

        + strings[MessageID.download_locally_availability]
    }
}

private val QuickActionBarsRow = FC<ContentEntryDetailOverviewProps> { props ->

    val strings = useStringsXml()

    Stack {
        direction = responsive(StackDirection.row)

        if (props.uiState.markCompleteVisible){
            UstadQuickActionButton {
                icon = CheckBox.create()
                text = strings[MessageID.mark_complete].uppercase()
                onClick = { props.onClickMarkComplete }
            }
        }

        if (props.uiState.contentEntryButtons?.showDeleteButton == true){
            UstadQuickActionButton {
                icon = Delete.create()
                text = strings[MessageID.delete].uppercase()
                onClick = { props.onClickDelete }
            }
        }

        if (props.uiState.contentEntryButtons?.showManageDownloadButton == true){
            UstadQuickActionButton {
                icon = Download.create()
                text = strings[MessageID.manage_download].uppercase()
                onClick = { props.onClickManageDownload }
            }
        }
    }
}

private val AvailableTranslations = FC<ContentEntryDetailOverviewProps> { props ->

    List{
        props.uiState.availableTranslationsMap.forEach { availableTranslation ->
            ListItem{
                Button {
                    onClick = {
                        props.onClickTranslation(availableTranslation)
                    }
                    variant = ButtonVariant.text

                    + (availableTranslation.language?.name ?: "")
                }
            }
        }
    }
}