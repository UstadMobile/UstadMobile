package com.ustadmobile.view.contententry

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.appstate.UstadContextMenuItem
import com.ustadmobile.core.viewmodel.contententry.contentTypeStringResource
import com.ustadmobile.core.viewmodel.contententry.list.listItemUiState
import com.ustadmobile.lib.db.composites.ContentEntryAndListDetail
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.mui.common.justifyContent
import com.ustadmobile.mui.ext.paddingCourseBlockIndent
import com.ustadmobile.util.ext.useLineClamp
import com.ustadmobile.view.contententry.detailoverviewtab.CONTENT_ENTRY_TYPE_ICON_MAP
import js.objects.jso
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
import react.useState
import react.dom.html.ReactHTML.div

external interface UstadContentEntryListItemProps : Props {

    var contentEntry: ContentEntryAndListDetail?

    var onClickContentEntry: (ContentEntry?) -> Unit

    var onSetSelected: (entry: ContentEntryAndListDetail, selected: Boolean) -> Unit

    var padding: Padding

    var selected: Boolean

    var contextMenuItems: (ContentEntryAndListDetail) -> List<UstadContextMenuItem>

}

val UstadContentEntryListItem = FC<UstadContentEntryListItemProps> { props ->
    var contextMenuPos: Pair<Double, Double>? by useState { null }
    val contextMenuPosVal = contextMenuPos

    ListItem {
        ListItemButton {
            //As per https://mui.com/material-ui/react-menu/#context-menu
            onContextMenu = { evt ->
                evt.preventDefault()
                val hasContextMenuItems = props.contentEntry?.let {
                    props.contextMenuItems(it)
                }?.isNotEmpty() ?: false

                contextMenuPos = if(hasContextMenuItems && contextMenuPos == null) {
                    (evt.clientX + 2) to (evt.clientY + 6)
                }else {
                    null
                }
            }

            sx {
                padding = props.padding
            }

            onClick = { evt ->
                when {
                    contextMenuPosVal != null -> {
                        //Do nothing - we don't want to intercept the that click
                    }

                    evt.shiftKey || evt.ctrlKey -> {
                        props.contentEntry?.also {
                            props.onSetSelected(it, !props.selected)
                        }
                    }

                    else -> {
                        props.contentEntry?.contentEntry?.also {
                            props.onClickContentEntry(it)
                        }
                    }
                }
            }
            selected = props.selected

            ListItemIcon {
                LeadingContent {
                    contentEntryItem = props.contentEntry?.contentEntry
                }
            }

            Box{
               sx { width = 10.px }
            }

            ListItemText {
                primary = ReactNode(props.contentEntry?.contentEntry?.title ?: "")
                secondary = SecondaryContent.create {
                    contentEntryItem = props.contentEntry?.contentEntry
                }

                secondaryTypographyProps = jso {
                    component = div
                }
            }

            Menu {
                open = contextMenuPos != null
                onClose = {
                    contextMenuPos = null
                }
                anchorReference = PopoverReference.anchorPosition
                anchorPosition = if(contextMenuPosVal != null) {
                    jso {
                        left = contextMenuPosVal.first
                        top = contextMenuPosVal.second
                    }
                }else {
                    null
                }

                if(contextMenuPosVal != null) {
                    props.contentEntry?.let { props.contextMenuItems(it) }?.forEach {
                        MenuItem {
                            onClick = { _ ->
                                contextMenuPos = null
                                it.onClick()
                            }
                            + it.label
                        }
                    }
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
                sx {
                    useLineClamp(2)
                }

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
        contentEntry = ContentEntryAndListDetail(
            contentEntry = ContentEntry().apply {
                contentEntryUid = 1
                leaf = true
                ceInactive = true
                contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                title = "Content Title"
                description = "Content Description"
            }
        )
        padding = paddingCourseBlockIndent(6)
    }
}
