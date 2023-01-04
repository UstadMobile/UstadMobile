package com.ustadmobile.mui.components

import com.ustadmobile.core.entityconstants.ProgressConstants
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.ContentEntryTypeLabelConstants
import com.ustadmobile.core.util.ext.progressBadge
import com.ustadmobile.core.viewmodel.ContentEntryListItemUiState
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.lib.db.entities.StatementEntity
import com.ustadmobile.mui.common.justifyContent
import com.ustadmobile.mui.ext.paddingCourseBlockIndent
import com.ustadmobile.view.CONTENT_ENTRY_TYPE_ICON_MAP
import csstype.*
import mui.icons.material.*
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import mui.material.Badge

external interface UstadContentEntryListItemProps : Props {

    var uiState: ContentEntryListItemUiState

    var onClickContentEntry: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

    var onClickDownloadContentEntry: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

}

private val UstadContentEntryListItem = FC<UstadContentEntryListItemProps> { props ->

    ListItem{
        ListItemButton {
            onClick = { props.onClickContentEntry(props.uiState.contentEntry) }

            sx {
                padding = paddingCourseBlockIndent(props.uiState.cbIndentLevel)
                opacity = number(props.uiState.containerAlpha)
            }

            ListItemIcon {
                LeadingContent {
                    uiState = props.uiState
                    contentEntryItem = props.uiState.contentEntry
                }
            }

            Box{
               sx { width = 10.px }
            }

            ListItemText {
                primary = ReactNode(props.uiState.contentEntry.title ?: "")
                secondary = SecondaryContent.create {
                    uiState = props.uiState
                    contentEntryItem = props.uiState.contentEntry
                }
            }
        }

        secondaryAction = SecondaryAction.create {
            contentEntryItem = props.uiState.contentEntry
            onClick = props.onClickDownloadContentEntry
        }
    }

}



external interface LeadingContentProps: Props {

    var uiState: ContentEntryListItemUiState

    var contentEntryItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
}

val LeadingContent = FC<LeadingContentProps> { props ->

    val thumbnail = if (props.contentEntryItem.leaf)
        BookOutlined
    else
        Folder

    var badgeIcon = CheckCircle
    var badgeColor = SvgIconColor.success
    if (props.contentEntryItem.scoreProgress?.progressBadge() == ProgressConstants.BADGE_CROSS) {
        badgeIcon = Cancel
        badgeColor = SvgIconColor.error
    }

    Stack {
        direction = responsive(StackDirection.column)
        spacing = responsive(10.px)
        justifyContent = JustifyContent.center

        thumbnail {
            sx {
                width = 55.px
                height = 55.px
            }
        }

        Badge {

            if (props.contentEntryItem.scoreProgress?.progressBadge() != ProgressConstants.BADGE_NONE) {
                badgeContent = badgeIcon.create {
                    sx {
                        width = 18.px
                        height = 18.px
                    }
                    color = badgeColor
                }
            }

            if (props.uiState.progressVisible){
                LinearProgress {
                    value = props.contentEntryItem.scoreProgress?.progress
                    variant = LinearProgressVariant.determinate
                    sx {
                        width = 45.px
                    }
                }
            }
        }
    }
}



external interface SecondaryContentProps: Props {

    var uiState: ContentEntryListItemUiState

    var contentEntryItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
}

val SecondaryContent = FC<SecondaryContentProps> { props ->

    val strings = useStringsXml()

    Stack {
        direction = responsive(StackDirection.column)
        justifyContent = JustifyContent.start


        if (props.uiState.descriptionVisible){
            Typography {
                + (props.contentEntryItem.description ?: "")
            }
        }

        Stack {
            direction = responsive(StackDirection.row)

            if (props.uiState.mimetypeVisible){
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
                + props.uiState.scoreResultText
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

val UstadContentEntryListItemPreview = FC<Props> {

    UstadContentEntryListItem {
        uiState = ContentEntryListItemUiState(
            contentEntry = ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer()
                .apply {
                    contentEntryUid = 1
                    leaf = true
                    ceInactive = true
                    scoreProgress = ContentEntryStatementScoreProgress().apply {
                        progress = 10
                        penalty = 20
                        success = StatementEntity.RESULT_SUCCESS
                    }
                    contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                    title = "Content Title"
                    description = "Content Description"
                },
            cbIndentLevel = 6
        )
    }
}