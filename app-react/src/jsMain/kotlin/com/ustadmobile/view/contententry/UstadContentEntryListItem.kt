package com.ustadmobile.view.contententry

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.appstate.UstadContextMenuItem
import com.ustadmobile.core.viewmodel.contententry.contentTypeStringResource
import com.ustadmobile.core.viewmodel.contententry.list.listItemUiState
import com.ustadmobile.hooks.useHtmlToPlainText
import com.ustadmobile.lib.db.composites.ContentEntryAndListDetail
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.mui.common.justifyContent
import com.ustadmobile.mui.components.UstadBlockIcon
import com.ustadmobile.mui.components.UstadBlockStatusProgressBar
import com.ustadmobile.mui.ext.paddingCourseBlockIndent
import com.ustadmobile.util.ext.useAbsolutePositionBottom
import com.ustadmobile.util.ext.useLineClamp
import js.objects.jso
import web.cssom.*
//WARNING: DO NOT Replace with import mui.icons.material.[*] - Leads to severe IDE performance issues 10/Apr/23 https://youtrack.jetbrains.com/issue/KT-57897/Intellisense-and-code-analysis-is-extremely-slow-and-unusable-on-Kotlin-JS
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
                Box {
                    sx {
                        position = Position.relative
                        width = 40.px
                        height = 40.px
                    }

                    UstadBlockStatusProgressBar {
                        sx {
                            useAbsolutePositionBottom()
                            width = 100.pct
                        }

                        blockStatus = props.contentEntry?.status
                    }

                    UstadBlockIcon {
                        contentEntry = props.contentEntry?.contentEntry
                        courseBlock = null
                        title = props.contentEntry?.contentEntry?.title ?: ""
                        pictureUri = props.contentEntry?.picture?.cepThumbnailUri
                    }
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

private external interface SecondaryContentProps: Props {

    var contentEntryItem: ContentEntry?
}

private val SecondaryContent = FC<SecondaryContentProps> { props ->

    val strings = useStringProvider()
    val descriptionPlainText = useHtmlToPlainText(props.contentEntryItem?.description ?: "")

    val uiState = props.contentEntryItem?.listItemUiState

    Stack {
        direction = responsive(StackDirection.column)
        justifyContent = JustifyContent.start


        if (uiState?.descriptionVisible == true){
            Typography {
                sx {
                    useLineClamp(2)
                }

                + descriptionPlainText
            }
        }

        Stack {
            direction = responsive(StackDirection.row)

            if (uiState?.mimetypeVisible == true){
                uiState.contentEntry.contentTypeIconComponent()?.also {
                    + it.create()
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
