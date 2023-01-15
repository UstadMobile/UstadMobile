package com.ustadmobile.view

import com.ustadmobile.core.entityconstants.ProgressConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.progressBadge
import com.ustadmobile.core.viewmodel.ContentEntryDetailOverviewUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.common.justifyContent
import com.ustadmobile.mui.common.md
import com.ustadmobile.mui.common.xs
import com.ustadmobile.mui.components.UstadQuickActionButton
import csstype.*
import csstype.Padding
import csstype.PlaceContent.Companion.end
import mui.icons.material.*
import mui.material.*
import mui.material.Badge
import mui.material.List
import mui.material.styles.TypographyVariant
import mui.system.Container
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.create
import react.useState

val CONTENT_ENTRY_TYPE_ICON_MAP = mapOf(
    ContentEntry.TYPE_EBOOK to Book,
    ContentEntry.TYPE_VIDEO to SmartDisplay,
    ContentEntry.TYPE_DOCUMENT to TextSnippet,
    ContentEntry.TYPE_ARTICLE to Article,
    ContentEntry.TYPE_COLLECTION to Collections,
    ContentEntry.TYPE_INTERACTIVE_EXERCISE to TouchApp,
    ContentEntry.TYPE_AUDIO to Audiotrack,
)

external interface ContentEntryDetailOverviewScreenProps : Props {

    var uiState: ContentEntryDetailOverviewUiState

    var onClickDownload: () -> Unit

    var onClickOpen: () -> Unit

    var onClickMarkComplete: () -> Unit

    var onClickDelete: () -> Unit

    var onClickManageDownload: () -> Unit

    var onClickTranslation: (ContentEntryRelatedEntryJoinWithLanguage) -> Unit

    var onClickContentJobItem: () -> Unit
}

val ContentEntryDetailOverviewComponent2 = FC<ContentEntryDetailOverviewScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    Container {
        Stack {
            spacing = responsive(20.px)

            ContentDetails{
                uiState = props.uiState
            }

            if (props.uiState.contentEntryButtons?.showDownloadButton == true){
                Button{
                    variant = ButtonVariant.contained
                    onClick = { props.onClickDownload }

                    + strings[MessageID.download].uppercase()
                }
            }

            if (props.uiState.contentEntryButtons?.showOpenButton == true){
                Button {
                    onClick = { props.onClickOpen }
                    variant = ButtonVariant.contained

                    + strings[MessageID.open].uppercase()
                }
            }

            ContentJobList{
                uiState = props.uiState
            }

            if (props.uiState.locallyAvailable) {
                LocallyAvailableRow()
            }

            Divider { orientation = Orientation.horizontal }

            QuickActionBarsRow {
                uiState = props.uiState
                onClickMarkComplete = props.onClickMarkComplete
                onClickDelete = props.onClickMarkComplete
                onClickManageDownload =  props.onClickMarkComplete
            }

            Typography{
                + (props.uiState.contentEntry?.description ?: "")
            }

            Divider { orientation = Orientation.horizontal }

            if (props.uiState.translationVisibile){
                Typography{
                    + strings[MessageID.also_available_in]
                }

                Grid {
                    direction = responsive(GridDirection.row)
                    container = true

                    props.uiState.availableTranslations.forEach { translation ->
                        Grid {
                            item = true
                            xs = 2
                            md = 1

                            Box {
                                sx {
                                    padding = 8.px
                                    textAlign = TextAlign.center
                                }

                                Button {
                                    variant = ButtonVariant.text
                                    onClick = { props.onClickTranslation(translation) }

                                    + (translation.language?.name ?: "")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val ContentDetails = FC<ContentEntryDetailOverviewScreenProps> { props ->

    Stack {
        direction = responsive(StackDirection.row)
        spacing = responsive(20.px)

        ContentDetailLeftColumn {
            uiState = props.uiState
        }

        ContentDetailRightColumn {
            uiState = props.uiState
        }
    }
}

private val ContentDetailLeftColumn = FC <ContentEntryDetailOverviewScreenProps> { props ->

    var badgeIcon = CheckCircle.create()
    var badgeColor = IconColor.success
    if (props.uiState.scoreProgress?.progressBadge() == ProgressConstants.BADGE_CROSS) {
        badgeIcon = Cancel.create()
        badgeColor = IconColor.error
    }

    Stack {
        direction = responsive(StackDirection.column)
        spacing = responsive(10.px)

        BookOutlined{
            sx {
                height = 110.px
                width = 110.px
            }
        }

        Badge {

            if (props.uiState.scoreProgress?.progressBadge() != ProgressConstants.BADGE_NONE) {
                badgeContent = Icon.create {
                    + badgeIcon
                    color = badgeColor
                }
            }

            if (props.uiState.scoreProgressVisible){
                LinearProgress {
                    value = props.uiState.scoreProgress?.progress
                    variant = LinearProgressVariant.determinate
                    sx {
                        width = 95.px
                    }
                }
            }
        }
    }
}

private val ContentDetailRightColumn = FC <ContentEntryDetailOverviewScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    Stack {
        direction = responsive(StackDirection.column)

        Typography{
            variant = TypographyVariant.h4
            + (props.uiState.contentEntry?.title ?: "")
        }


        if (props.uiState.authorVisible){
            Typography{
                + (props.uiState.contentEntry?.author ?: "")
            }
        }

        if (props.uiState.publisherVisible){
            Typography{
                + (props.uiState.contentEntry?.publisher ?: "")
            }
        }

        if (props.uiState.licenseNameVisible){
            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(5.px)
                sx {
                    alignItems = AlignItems.center
                }

                Typography{
                    + strings[MessageID.entry_details_license]
                }

                Typography {
                    variant = TypographyVariant.h6
                    + (props.uiState.contentEntry?.licenseName ?: "")
                }
            }
        }

        Stack {
            direction = responsive(StackDirection.row)
            spacing = responsive(16.px)

            if (props.uiState.fileSizeVisible){
                Typography{
                    + UMFileUtil.formatFileSize(
                        props.uiState.contentEntry?.container?.fileSize ?: 0
                    )
                }
            }


            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(5.px)

                + EmojiEvents.create()

                Typography{
                    + props.uiState.scoreProgress?.progress.toString()
                }
            }


            if (props.uiState.scoreResultVisible){
                Typography {
                    + ("(" + (props.uiState.scoreProgress?.resultScore ?: "") +
                            "/" + (props.uiState.scoreProgress?.resultMax ?: "") + ")")
                }
            }
        }
    }
}

private val ContentJobList = FC <ContentEntryDetailOverviewScreenProps> { props ->

    List{
        props.uiState.activeContentJobItems.forEach {
            onClick = { props.onClickContentJobItem }
            ListItemText{
                sx {
                    padding = 10.px
                }

                Stack {
                    Stack {
                        direction = responsive(StackDirection.row)
                        justifyContent = JustifyContent.spaceBetween


                        Typography {
                            + (it.progressTitle ?: "")
                        }

                        Typography {
                            + (it.progress.toString()+" %")
                        }
                    }

                    LinearProgress {
                        value = it.progress / it.total
                        variant = LinearProgressVariant.determinate
                    }
                }
            }
        }
    }
}

private val LocallyAvailableRow = FC <ContentEntryDetailOverviewScreenProps> {

    val strings: StringsXml = useStringsXml()

    Stack {
        direction = responsive(StackDirection.row)
        spacing = responsive(10.px)

        + LocationOnOutlined.create()

        Typography{
            +strings[MessageID.download_locally_availability]
        }
    }
}

private val QuickActionBarsRow = FC <ContentEntryDetailOverviewScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    Stack {
        direction = responsive(StackDirection.row)
        spacing = responsive(5.px)
        sx {
            alignItems = AlignItems.center
        }

        if (props.uiState.markCompleteVisible){
            UstadQuickActionButton {
                icon = CheckBoxOutlined.create()
                text = strings[MessageID.mark_complete].uppercase()
                onClick = { props.onClickMarkComplete }
            }
        }

        if (props.uiState.contentEntryButtons?.showDeleteButton == true){
            UstadQuickActionButton {
                text = strings[MessageID.delete].uppercase()
                icon = Delete.create()
                onClick = { props.onClickDelete }
            }
        }

        if (props.uiState.contentEntryButtons?.showManageDownloadButton == true){
            UstadQuickActionButton {
                text = strings[MessageID.manage_download].uppercase()
                icon = Download.create()
                onClick = { props.onClickManageDownload }
            }
        }
    }
}

val ContentEntryDetailOverviewScreenPreview = FC<Props> {

    val uiStateVar by useState {
        ContentEntryDetailOverviewUiState(
            contentEntry = ContentEntryWithMostRecentContainer().apply {
                title = "Content Title"
                author = "Author"
                publisher = "Publisher"
                licenseName = "BY_SA"
                container = com.ustadmobile.lib.db.entities.Container().apply {
                    fileSize = 50
                }
                description = "Content Description"
            },
            scoreProgress = ContentEntryStatementScoreProgress().apply {
                /*@FloatRange(from = 0.0, to = 1.0)*/
                progress = 4

                success = StatementEntity.RESULT_SUCCESS
                resultScore = 4
                resultMax = 40
            },
            contentEntryButtons = ContentEntryButtonModel().apply {
                showDownloadButton = true
                showOpenButton = true
                showDeleteButton = true
                showManageDownloadButton = true
            },
            availableTranslations = listOf(
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "Persian"
                    }
                },
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "English"
                    }
                },
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "Korean"
                    }
                },
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "Tamil"
                    }
                },
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "Turkish"
                    }
                },
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "Telugu"
                    }
                },
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "Marathi"
                    }
                },
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "Vietnamese"
                    }
                },
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "Japanese"
                    }
                },
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "Russian"
                    }
                },
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "Portuguese"
                    }
                },
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "Bengali"
                    }
                },
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "Spanish"
                    }
                },
                ContentEntryRelatedEntryJoinWithLanguage().apply {
                    language = Language().apply {
                        name = "Hindi"
                    }
                }
            ),
            activeContentJobItems = listOf(
                ContentJobItemProgress().apply {
                    progressTitle = "First"
                    progress = 30
                    total = 2
                },
                ContentJobItemProgress().apply {
                    progressTitle = "Second"
                    progress = 10
                    total = 5
                },
                ContentJobItemProgress().apply {
                    progressTitle = "Third"
                    progress = 70
                    total = 4
                }
            ),
            locallyAvailable = true,
            markCompleteVisible = true,
            translationVisibile = true
        )
    }

    ContentEntryDetailOverviewComponent2 {
        uiState = uiStateVar
    }
}