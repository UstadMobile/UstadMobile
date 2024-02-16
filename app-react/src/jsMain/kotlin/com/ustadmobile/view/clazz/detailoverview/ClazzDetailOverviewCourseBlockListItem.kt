package com.ustadmobile.view.clazz.detailoverview

import com.ustadmobile.lib.db.composites.CourseBlockAndDisplayDetails
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.ext.paddingCourseBlockIndent
import web.cssom.px
import mui.material.*
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import mui.icons.material.KeyboardArrowUp
import mui.icons.material.KeyboardArrowDown
import react.dom.aria.ariaLabel
import mui.icons.material.Forum as ForumIcon
import mui.icons.material.Folder as FolderIcon
import mui.icons.material.Title as TitleIcon
import mui.icons.material.AssignmentTurnedIn as AssignmentTurnedInIcon
import mui.icons.material.Book as BookIcon
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useHtmlToPlainText
import com.ustadmobile.util.ext.useLineClamp
import js.core.jso
import react.dom.html.ReactHTML.div

external interface ClazzDetailOverviewCourseBlockListItemProps : Props {

    var courseBlock: CourseBlockAndDisplayDetails?

    var onClickCourseBlock: (CourseBlock) -> Unit

    var onClickContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

    var onClickDownloadContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

}

private val MODULE_TYPE_TO_ICON_MAP = mapOf(
    CourseBlock.BLOCK_MODULE_TYPE to FolderIcon,
    CourseBlock.BLOCK_DISCUSSION_TYPE to ForumIcon,
    CourseBlock.BLOCK_TEXT_TYPE to TitleIcon,
    CourseBlock.BLOCK_ASSIGNMENT_TYPE to AssignmentTurnedInIcon,
    CourseBlock.BLOCK_CONTENT_TYPE to BookIcon,
)

val ClazzDetailOverviewCourseBlockListItem = FC<ClazzDetailOverviewCourseBlockListItemProps> { props ->
    val courseBlockVal = props.courseBlock?.courseBlock

    val blockDescription = useHtmlToPlainText(courseBlockVal?.cbDescription ?: "")

    ListItem {
        ListItemButton {
            sx {
                padding = paddingCourseBlockIndent(courseBlockVal?.cbIndentLevel ?: 0)
            }

            onClick = {
                props.courseBlock?.courseBlock?.also { props.onClickCourseBlock(it) }
            }

            ListItemIcon {
                + MODULE_TYPE_TO_ICON_MAP[courseBlockVal?.cbType ?: CourseBlock.BLOCK_MODULE_TYPE]?.create {
                    sx {
                        width = ICON_SIZE
                        height = ICON_SIZE
                    }
                }
            }

            Box{
                sx {
                    width = 10.px
                }
            }

            ListItemText {
                primary = ReactNode(courseBlockVal?.cbTitle ?: "")
                secondary = ReactNode(blockDescription)
                secondaryTypographyProps = jso {
                    component = div
                    sx {
                        useLineClamp(2)
                    }
                }
            }
        }

        secondaryAction = Tooltip.create {
            val strings = useStringProvider()
            val labelText = if(props.courseBlock?.expanded == true) {
                strings[MR.strings.collapse]
            }else {
                strings[MR.strings.expand]
            }

            title = ReactNode(labelText)

            IconButton {
                if(courseBlockVal?.cbType == CourseBlock.BLOCK_MODULE_TYPE) {
                    val trailingIcon = if(props.courseBlock?.expanded == true)
                        KeyboardArrowUp
                    else
                        KeyboardArrowDown

                    ariaLabel = labelText

                    onClick = {
                        props.courseBlock?.courseBlock?.also { props.onClickCourseBlock(it) }
                    }
                    + trailingIcon.create()
                }
            }
        }
    }
}
