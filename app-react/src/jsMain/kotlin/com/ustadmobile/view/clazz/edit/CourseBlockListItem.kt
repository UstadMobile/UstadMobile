package com.ustadmobile.view.clazz.edit

import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditUiState
import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadBlockIcon
import com.ustadmobile.wrappers.reacteasysort.SortableItem
import web.cssom.number
import web.cssom.px
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.system.sx
import react.*
import react.dom.html.ReactHTML
import web.html.HTMLDivElement

external interface CourseBlockListItemProps : Props {

    var block: CourseBlockAndEditEntities

    var uiState: ClazzEditUiState.CourseBlockUiState

    var onClickEditCourseBlock: (CourseBlockAndEditEntities) -> Unit

    var fieldsEnabled: Boolean

    var onClickHideBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit

    var onClickUnHideBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit

    var onClickIndentBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit

    var onClickUnIndentBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit

    var onClickDeleteBlockPopupMenu: (CourseBlockAndEditEntities) -> Unit

}

val CourseBlockListItem = FC<CourseBlockListItemProps> { props ->
    val theme by useRequiredContext(ThemeContext)

    SortableItem {
        ReactHTML.div {
            val divRef : MutableRefObject<HTMLDivElement> = useRef(null)

            ListItem {
                val courseBlockEditAlpha: Double = if (props.block.courseBlock.cbHidden) 0.5 else 1.0
                val startPadding = (props.block.courseBlock.cbIndentLevel * 24).px

                ListItemButton {
                    sx {
                        opacity = number(courseBlockEditAlpha)
                    }

                    onClick = {
                        //Avoid triggering the onClick listener if the dragging is in process
                        //This might not be needed
                        if(divRef.current?.classList?.contains(COURSE_BLOCK_DRAG_CLASS) != true) {
                            props.onClickEditCourseBlock(props.block)
                        }
                    }

                    ListItemIcon {
                        sx {
                            paddingLeft = startPadding
                            paddingRight = theme.spacing(1)
                        }

                        UstadBlockIcon {
                            title = props.block.courseBlock.cbTitle ?: ""
                            contentEntry = props.block.contentEntry
                            courseBlock = props.block.courseBlock
                            pictureUri = props.block.courseBlockPicture?.cbpPictureUri
                        }
                    }

                    ListItemText {
                        primary = ReactNode(props.block.courseBlock.cbTitle ?: "")
                    }
                }

                secondaryAction = PopUpMenu.create {
                    fieldsEnabled = props.fieldsEnabled
                    onClickHideBlockPopupMenu = props.onClickHideBlockPopupMenu
                    onClickUnHideBlockPopupMenu = props.onClickUnHideBlockPopupMenu
                    onClickIndentBlockPopupMenu = props.onClickIndentBlockPopupMenu
                    onClickUnIndentBlockPopupMenu = props.onClickUnIndentBlockPopupMenu
                    onClickDeleteBlockPopupMenu = props.onClickDeleteBlockPopupMenu
                    uiState = props.uiState
                }
            }
        }
    }
}
