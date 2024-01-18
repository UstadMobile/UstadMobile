package com.ustadmobile.view.contententry

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.contententry.contentTypeStringResource
import com.ustadmobile.core.viewmodel.contententry.list.listItemUiState
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.mui.common.justifyContent
import com.ustadmobile.mui.ext.paddingCourseBlockIndent
import com.ustadmobile.view.contententry.detailoverviewtab.CONTENT_ENTRY_TYPE_ICON_MAP
import web.cssom.*
//WARNING: DO NOT Replace with import mui.icons.material.[*] - Leads to severe IDE performance issues 10/Apr/23 https://youtrack.jetbrains.com/issue/KT-57897/Intellisense-and-code-analysis-is-extremely-slow-and-unusable-on-Kotlin-JS
import mui.icons.material.BookOutlined as BookOutlinedIcon
import mui.icons.material.Folder as FolderIcon
import mui.icons.material.TextSnippet as TextSnippetIcon
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface UstadContentEntryListItemProps : Props {

    var contentEntry: ContentEntry?

    var onClickContentEntry: (ContentEntry?) -> Unit

    var padding: Padding

}

val UstadContentEntryListItem = FC<UstadContentEntryListItemProps> { props ->

    val uiState = props.contentEntry?.listItemUiState

    ListItem{
        ListItemButton {
            onClick = { props.onClickContentEntry(props.contentEntry) }

            sx {
                padding = props.padding
                opacity = number(uiState?.containerAlpha ?: 1.0)
            }

            ListItemIcon {
                LeadingContent {
                    contentEntryItem = uiState?.contentEntry
                }
            }

            Box{
               sx { width = 10.px }
            }

            ListItemText {
                primary = ReactNode(props.contentEntry?.title ?: "")
                secondary = SecondaryContent.create {
                    contentEntryItem = props.contentEntry
                }
            }
        }
    }

}

private external interface LeadingContentProps: Props {

    var contentEntryItem: ContentEntry?
}

private val LeadingContent = FC<LeadingContentProps> { props ->

    val thumbnail = if (props.contentEntryItem?.leaf == true)
        BookOutlinedIcon
    else
        FolderIcon

    Stack {
        direction = responsive(StackDirection.column)
        spacing = responsive(10.px)
        justifyContent = JustifyContent.center

        thumbnail {
            sx {
                width = 40.px
                height = 40.px
            }
        }
    }
}



private external interface SecondaryContentProps: Props {

    var contentEntryItem: ContentEntry?
}

private val SecondaryContent = FC<SecondaryContentProps> { props ->

    val strings = useStringProvider()
    val uiState = props.contentEntryItem?.listItemUiState

    Stack {
        direction = responsive(StackDirection.column)
        justifyContent = JustifyContent.start


        if (uiState?.descriptionVisible == true){
            Typography {
                + (props.contentEntryItem?.description ?: "")
            }
        }

        Stack {
            direction = responsive(StackDirection.row)

            if (uiState?.mimetypeVisible == true){
                Icon {
                    + (CONTENT_ENTRY_TYPE_ICON_MAP[props.contentEntryItem
                        ?.contentTypeFlag]?.create() ?: TextSnippetIcon.create())
                }

                Typography {
                    + (props.contentEntryItem?.contentTypeStringResource?.let { strings[it] } ?: "")
                }

                Box {
                    sx { width = 20.px }
                }
            }

        }
    }
}


val UstadContentEntryListItemPreview = FC<Props> {

    UstadContentEntryListItem {
        contentEntry = ContentEntry().apply {
            contentEntryUid = 1
            leaf = true
            ceInactive = true
            contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
            title = "Content Title"
            description = "Content Description"
        }
        padding = paddingCourseBlockIndent(6)
    }
}
