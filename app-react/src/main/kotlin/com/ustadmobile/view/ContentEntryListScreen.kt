package com.ustadmobile.view

import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.ContentEntryTypeLabelConstants
import com.ustadmobile.core.viewmodel.ContentEntryListUiState
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.mui.common.justifyContent
import csstype.*
import mui.icons.material.*
import mui.material.*
import mui.material.List
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

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

private val CONTENT_ENTRY_TYPE_ICON_MAP = mapOf(
    ContentEntry.TYPE_EBOOK to Book,
    ContentEntry.TYPE_VIDEO to SmartDisplay,
    ContentEntry.TYPE_DOCUMENT to TextSnippet,
    ContentEntry.TYPE_ARTICLE to Article,
    ContentEntry.TYPE_COLLECTION to Collections,
    ContentEntry.TYPE_INTERACTIVE_EXERCISE to TouchApp,
    ContentEntry.TYPE_AUDIO to Audiotrack,
)

private val ContentEntryListScreenComponent2 = FC<ContentEntryListScreenProps> { props ->
    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(20.px)

            List{
                props.uiState.contentEntryList.forEach { contentEntry ->

                    ListItem{
                        ListItemButton {
                            onClick = { props.onClickContentEntry(contentEntry) }

                            sx {
                                opacity = number(props.uiState.containerAlpha(contentEntry))
                            }

                            LeadingContent {
                                uiState = props.uiState
                                contentEntryItem = contentEntry
                            }

                            ListItemText {
                                primary = ReactNode(contentEntry.title ?: "")
                                secondary = SecondaryContent.create {
                                    uiState = props.uiState
                                    contentEntryItem = contentEntry
                                }
                            }

                        }

                        secondaryAction = SecondaryAction.create {
                            contentEntryItem = contentEntry
                            onClick = props.onClickDownloadContentEntry
                        }
                    }
                }
            }
        }
    }
}



external interface LeadingContentProps: Props {

    var uiState: ContentEntryListUiState

    var contentEntryItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
}

val LeadingContent = FC<LeadingContentProps> { props ->

    val thumbnail = if (props.contentEntryItem.leaf)
        BookOutlined
    else
        Folder

    ListItemIcon {
        Stack {
            direction = responsive(StackDirection.column)
            justifyContent = JustifyContent.center

            thumbnail {
                sx {
                    border = Border(1.px, csstype.LineStyle.solid)
                    borderRadius = 27.5.px
                    width = 55.px
                    height = 55.px
                    padding = 10.px
                }
            }


            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(10.px)

                sx {
                    alignItems = AlignItems.center
                }


                Box {
                    sx {
                        width = 80.px
                    }

                    if (props.uiState.progressVisible(props.contentEntryItem)){
                        LinearProgress {
                            value = 0.5
                            variant = LinearProgressVariant.determinate
                        }
                    }
                }

                Icon {
                    + CheckCircle.create()
                    color = IconColor.success
                }
            }
        }
    }
}



external interface SecondaryContentProps: Props {

    var uiState: ContentEntryListUiState

    var contentEntryItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
}

val SecondaryContent = FC<SecondaryContentProps> { props ->

    val strings = useStringsXml()

    Stack {
        direction = responsive(StackDirection.column)
        justifyContent = JustifyContent.start


        Typography {
            + (props.contentEntryItem.title ?: "")
        }

        Stack {
            direction = responsive(StackDirection.row)

            if (props.uiState.mimetypeVisible(props.contentEntryItem)){
                Icon {
                    + (CONTENT_ENTRY_TYPE_ICON_MAP[props.contentEntryItem
                        .contentTypeFlag]?.create() ?: TextSnippet.create())
                }

                Typography {
                    + (strings[ContentEntryTypeLabelConstants
                        .TYPE_LABEL_MESSAGE_IDS[props.contentEntryItem.contentTypeFlag]
                        .messageId])
                }

                Box {
                    sx { width = 20.px }
                }
            }

            Icon {
                + EmojiEvents.create()
            }

            Typography {
                + "${props.contentEntryItem.scoreProgress?.progress ?: 0}%"
            }

            Typography {
                + props.uiState.scoreResultText(props.contentEntryItem)
            }
        }
    }
}



external interface SecondaryActionProps: Props {

    var contentEntryItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer

    var onClick: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit
}

val SecondaryAction = FC<SecondaryActionProps> { props ->

    Button {

        onClick = { props.onClick(props.contentEntryItem) }

        CircularProgress {
            sx {
                position = Position.absolute
            }

            variant = CircularProgressVariant.determinate
            value = 100
        }

        + Download.create()
    }
}