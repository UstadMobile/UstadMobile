package com.ustadmobile.view.clazz.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditUiState
import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import web.cssom.ClassName
import js.objects.jso
import mui.icons.material.MoreVert
import mui.material.IconButton
import mui.material.Menu
import mui.material.MenuItem
import mui.material.PopoverReference
import react.FC
import react.Props
import react.create
import react.dom.aria.ariaLabel
import react.dom.events.MouseEvent
import react.dom.events.MouseEventHandler
import react.dom.html.ReactHTML
import react.useState


external interface PopUpMenuProps : Props {

    var fieldsEnabled: Boolean

    var onClickHideBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit

    var onClickUnHideBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit

    var onClickIndentBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit

    var onClickUnIndentBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit

    var onClickDeleteBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit

    var uiState: ClazzEditUiState.CourseBlockUiState

}

private data class Point(
    val x: Double = 10.0,
    val y: Double = 10.0,
)

val PopUpMenu = FC<PopUpMenuProps> { props ->

    val strings = useStringProvider()

    var point by useState<Point>()

    val handleContextMenu = { event: MouseEvent<*, *> ->
        event.preventDefault()
        point = if (point == null) {
            Point(
                x = event.clientX - 2,
                y = event.clientY - 4,
            )
        } else {
            null
        }
    }

    val handleClose: MouseEventHandler<*> = {
        point = null
    }

    ReactHTML.div {

        IconButton{
            disabled = !(props.fieldsEnabled)
            onClick = handleContextMenu
            ariaLabel = strings[MR.strings.more_options]
            className = ClassName("courseblockpopup")

            + MoreVert.create()
        }

        Menu {
            open = point != null
            onClose = handleClose

            anchorReference = PopoverReference.anchorPosition
            anchorPosition = if (point != null) {
                jso {
                    top = point!!.y
                    left = point!!.x
                }
            } else {
                undefined
            }

            if(props.uiState.showHide) {
                MenuItem {
                    onClick = {
                        props.onClickHideBlockPopupMenu(props.uiState.block)
                        point = null
                    }
                    + strings[MR.strings.hide]
                }
            }

            if(props.uiState.showUnhide) {
                MenuItem {
                    onClick = {
                        props.onClickUnHideBlockPopupMenu(props.uiState.block)
                        point = null
                    }
                    + strings[MR.strings.unhide]
                }
            }

            if(props.uiState.showIndent) {
                MenuItem {
                    onClick = {
                        props.onClickIndentBlockPopupMenu(props.uiState.block)
                        point = null
                    }
                    + strings[MR.strings.indent]
                }
            }

            if (props.uiState.showUnindent) {
                MenuItem {
                    onClick = {
                        props.onClickUnIndentBlockPopupMenu(props.uiState.block)
                        point = null
                    }
                    + strings[MR.strings.unindent]
                }
            }

            MenuItem {
                onClick = {
                    props.onClickDeleteBlockPopupMenu(props.uiState.block)
                    point = null
                }
                + strings[MR.strings.delete]
            }
        }
    }
}
