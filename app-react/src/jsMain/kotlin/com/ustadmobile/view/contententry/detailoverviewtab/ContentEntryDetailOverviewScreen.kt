package com.ustadmobile.view.contententry.detailoverviewtab

import com.ustadmobile.core.entityconstants.ProgressConstants
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.impl.locale.StringProviderJs
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.progressBadge
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.ContentEntryDetailOverviewUiState
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.ContentEntryDetailOverviewViewModel
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.progress
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.ContentEntryAndDetail
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.common.md
import com.ustadmobile.mui.common.xs
import com.ustadmobile.mui.components.UstadLinearProgressListItem
import com.ustadmobile.mui.components.UstadQuickActionButton
import com.ustadmobile.mui.components.UstadRawHtml
import web.cssom.*
import mui.material.*
import mui.material.Badge
import mui.material.styles.TypographyVariant
import mui.system.Container
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.create
import react.useState

//WARNING: DO NOT Replace with import mui.icons.material.[*] - Leads to severe IDE performance issues 10/Apr/23 https://youtrack.jetbrains.com/issue/KT-57897/Intellisense-and-code-analysis-is-extremely-slow-and-unusable-on-Kotlin-JS
import mui.icons.material.Book
import mui.icons.material.SmartDisplay
import mui.icons.material.TextSnippet
import mui.icons.material.Article
import mui.icons.material.Collections
import mui.icons.material.TouchApp
import mui.icons.material.Audiotrack
import mui.icons.material.CheckCircle
import mui.icons.material.Cancel
import mui.icons.material.BookOutlined
import mui.icons.material.EmojiEvents
import mui.icons.material.CheckBoxOutlined
import mui.icons.material.Delete
import mui.icons.material.Download
import react.ReactNode

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

    var onClickOpen: () -> Unit

    var onClickMarkComplete: () -> Unit

    var onClickDelete: () -> Unit

    var onClickManageDownload: () -> Unit

    var onClickTranslation: (ContentEntryRelatedEntryJoinWithLanguage) -> Unit

    var onCancelRemoteImport: (Long) -> Unit

    var onDismissRemoteImportError: (Long) -> Unit

}

val ContentEntryDetailOverviewComponent2 = FC<ContentEntryDetailOverviewScreenProps> { props ->

    val strings = useStringProvider()

    Container {
        Stack {
            spacing = responsive(20.px)

            ContentDetails {
                uiState = props.uiState
            }

            if (props.uiState.openButtonVisible){
                Button {
                    onClick = { props.onClickOpen() }
                    variant = ButtonVariant.contained

                    + strings[MR.strings.open].uppercase()
                }
            }

            Divider { orientation = Orientation.horizontal }

            QuickActionBarsRow {
                uiState = props.uiState
                onClickMarkComplete = props.onClickMarkComplete
                onClickDelete = props.onClickMarkComplete
                onClickManageDownload =  props.onClickMarkComplete
            }

            props.uiState.remoteImportJobs.forEach {
                val canCancelJob = props.uiState.canCancelRemoteImportJob(it)
                UstadLinearProgressListItem {
                    progress = it.progress
                    secondaryContent = ReactNode(strings[MR.strings.importing])
                    error = it.cjiError
                    if(canCancelJob) {
                        onCancel = {
                            props.onCancelRemoteImport(it.cjiUid)
                        }
                        onDismissError = {
                            props.onDismissRemoteImportError(it.cjiUid)
                        }
                    }

                }
            }

            UstadRawHtml {
                html = props.uiState.contentEntry?.entry?.description ?: ""
            }

            Divider { orientation = Orientation.horizontal }

            if (props.uiState.translationVisibile){
                Typography{
                    + strings[MR.strings.also_available_in]
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
            if (props.uiState.scoreProgress?.progressBadge()
                .let { it != null && it != ProgressConstants.BADGE_NONE }
            ) {
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

    val strings: StringProviderJs = useStringProvider()

    Stack {
        direction = responsive(StackDirection.column)

        Typography {
            variant = TypographyVariant.h4
            + (props.uiState.contentEntry?.entry?.title ?: "")
        }

        if(props.uiState.compressedSizeVisible) {
            Typography {
                variant = TypographyVariant.caption
                + strings.format(MR.strings.size_compressed_was,
                    UMFileUtil.formatFileSize(props.uiState.latestContentEntryVersion?.cevStorageSize ?: 0),
                    UMFileUtil.formatFileSize(props.uiState.latestContentEntryVersion?.cevOriginalSize ?: 0),
                )
            }
        }else if(props.uiState.sizeVisible) {
            Typography {
                variant = TypographyVariant.caption
                + strings.format(
                    MR.strings.size,
                    UMFileUtil.formatFileSize(props.uiState.latestContentEntryVersion?.cevStorageSize ?: 0),
                )
            }
        }

        if (props.uiState.authorVisible){
            Typography{
                + (props.uiState.contentEntry?.entry?.author ?: "")
            }
        }

        if (props.uiState.publisherVisible){
            Typography{
                + (props.uiState.contentEntry?.entry?.publisher ?: "")
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
                    + strings[MR.strings.entry_details_license]
                }

                Typography {
                    variant = TypographyVariant.h6
                    + (props.uiState.contentEntry?.entry?.licenseName ?: "")
                }
            }
        }

        Stack {
            direction = responsive(StackDirection.row)
            spacing = responsive(16.px)

            if (props.uiState.fileSizeVisible){
                Typography{
                    + UMFileUtil.formatFileSize(0)
                }
            }

            if (props.uiState.scoreResultVisible){
                Stack {
                    direction = responsive(StackDirection.row)
                    spacing = responsive(5.px)

                    + EmojiEvents.create()

                    Typography{
                        + props.uiState.scoreProgress?.progress.toString()
                    }
                }

                Typography {
                    + ("(" + (props.uiState.scoreProgress?.resultScore ?: "") +
                            "/" + (props.uiState.scoreProgress?.resultMax ?: "") + ")")
                }
            }
        }
    }
}


private val QuickActionBarsRow = FC <ContentEntryDetailOverviewScreenProps> { props ->

    val strings: StringProvider = useStringProvider()

    Stack {
        direction = responsive(StackDirection.row)
        spacing = responsive(5.px)
        sx {
            alignItems = AlignItems.center
        }

        if (props.uiState.markCompleteVisible){
            UstadQuickActionButton {
                icon = CheckBoxOutlined.create()
                text = strings[MR.strings.mark_complete].uppercase()
                onClick = { props.onClickMarkComplete }
            }
        }

        if (props.uiState.contentEntryButtons?.showDeleteButton == true){
            UstadQuickActionButton {
                text = strings[MR.strings.delete].uppercase()
                icon = Delete.create()
                onClick = { props.onClickDelete }
            }
        }

        if (props.uiState.contentEntryButtons?.showManageDownloadButton == true){
            UstadQuickActionButton {
                text = strings[MR.strings.manage_download].uppercase()
                icon = Download.create()
                onClick = { props.onClickManageDownload }
            }
        }
    }
}

val ContentEntryDetailOverviewScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryDetailOverviewViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ContentEntryDetailOverviewUiState())

    ContentEntryDetailOverviewComponent2 {
        uiState = uiStateVal
        onClickOpen = viewModel::onClickOpen
        onCancelRemoteImport = viewModel::onCancelRemoteImport
        onDismissRemoteImportError = viewModel::onDismissRemoteImportError
    }
}

@Suppress("unused")
val ContentEntryDetailOverviewScreenPreview = FC<Props> {

    val uiStateVar by useState {
        ContentEntryDetailOverviewUiState(
            contentEntry = ContentEntryAndDetail(
                entry = ContentEntry().apply {
                    title = "Content Title"
                    author = "Author"
                    publisher = "Publisher"
                    licenseName = "BY_SA"
                    description = "Content Description"
                }
            ),
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
            locallyAvailable = true,
            markCompleteVisible = true,
            translationVisibile = true
        )
    }

    ContentEntryDetailOverviewComponent2 {
        uiState = uiStateVar
    }
}