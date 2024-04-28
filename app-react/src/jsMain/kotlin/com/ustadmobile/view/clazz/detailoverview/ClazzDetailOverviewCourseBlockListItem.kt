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
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazz.blockTypeStringResource
import com.ustadmobile.core.viewmodel.contententry.contentTypeStringResource
import com.ustadmobile.hooks.useHtmlToPlainText
import com.ustadmobile.mui.components.UstadBlockIcon
import com.ustadmobile.util.ext.useLineClamp
import com.ustadmobile.view.clazz.iconComponent
import com.ustadmobile.view.contententry.contentTypeIconComponent
import emotion.react.css
import js.objects.jso
import mui.system.responsive
import react.dom.html.ReactHTML.div

external interface ClazzDetailOverviewCourseBlockListItemProps : Props {

    var courseBlock: CourseBlockAndDisplayDetails?

    var onClickCourseBlock: (CourseBlock) -> Unit

    var onClickContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

    var onClickDownloadContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

}

val ClazzDetailOverviewCourseBlockListItem = FC<ClazzDetailOverviewCourseBlockListItemProps> { props ->
    val courseBlockVal = props.courseBlock?.courseBlock
    val contentEntryVal = props.courseBlock?.contentEntry

    val blockDescription = useHtmlToPlainText(courseBlockVal?.cbDescription ?: "")
    val strings = useStringProvider()

    ListItem {
        ListItemButton {
            sx {
                padding = paddingCourseBlockIndent(courseBlockVal?.cbIndentLevel ?: 0)
            }

            onClick = {
                props.courseBlock?.courseBlock?.also { props.onClickCourseBlock(it) }
            }

            ListItemIcon {
                UstadBlockIcon {
                    title = courseBlockVal?.cbTitle ?: ""
                    pictureUri = props.courseBlock?.courseBlockPicture?.cbpThumbnailUri
                        ?: props.courseBlock?.contentEntryPicture2?.cepThumbnailUri
                    courseBlock = props.courseBlock?.courseBlock
                    contentEntry = props.courseBlock?.contentEntry
                }
            }

            Box {
                sx {
                    width = 10.px
                }
            }

            ListItemText {
                primary = ReactNode(courseBlockVal?.cbTitle ?: "")
                secondary = Stack.create {
                    direction = responsive(StackDirection.column)

                    div {
                        when {
                            contentEntryVal != null -> {
                                val iconType = contentEntryVal.contentTypeIconComponent()
                                if(iconType != null) {
                                    +iconType.create {
                                        fontSize = SvgIconSize.small
                                        ariaLabel = ""
                                        sx {
                                            marginRight = 8.px
                                            padding = 1.px
                                        }
                                    }
                                }
                                + strings[contentEntryVal.contentTypeStringResource]
                            }
                            courseBlockVal != null -> {
                                val iconType = courseBlockVal.iconComponent()
                                if(iconType != null) {
                                    + iconType.create {
                                        fontSize = SvgIconSize.small
                                        ariaLabel = ""
                                        sx {
                                            marginRight = 8.px
                                            padding = 1.px
                                        }
                                    }
                                }

                                + strings[courseBlockVal.blockTypeStringResource]
                            }
                        }
                    }
                    div {
                        css {
                            useLineClamp(1)
                        }
                        + blockDescription
                    }
                }
                secondaryTypographyProps = jso {
                    component = div
                }
            }
        }

        secondaryAction = Tooltip.create {
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
