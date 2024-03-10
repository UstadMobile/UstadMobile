package com.ustadmobile.view.clazzassignment

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.ext.progressAsFloat
import com.ustadmobile.lib.db.composites.CourseAssignmentSubmissionFileAndTransferJob
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import js.core.jso
import mui.material.LinearProgress
import react.FC
import react.Props
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
import mui.material.Stack
import mui.material.StackDirection
import mui.material.SvgIconSize
import mui.system.responsive
import react.ReactNode
import react.create
import react.dom.html.ReactHTML
import mui.icons.material.Error as ErrorIcon
import com.ustadmobile.core.MR
import com.ustadmobile.mui.components.ThemeContext
import mui.material.LinearProgressVariant
import mui.system.sx
import react.useRequiredContext

external interface CourseAssignmentSubmissionFileListItemProps : Props {
    var file: CourseAssignmentSubmissionFileAndTransferJob
}

val CourseAssignmentSubmissionFileListItem = FC<CourseAssignmentSubmissionFileListItemProps> { props ->
    val strings = useStringProvider()
    val theme by useRequiredContext(ThemeContext)

    ListItem {
        ListItemButton {
            ListItemText {
                primary = ReactNode(props.file.submissionFile?.casaFileName ?: "")
                secondary = Stack.create {
                    direction = responsive(StackDirection.column)

                    props.file.transferJobItem?.also { transferJob ->
                        when(transferJob.tjiStatus) {
                            TransferJobItemStatus.STATUS_IN_PROGRESS_INT -> {
                                LinearProgress {
                                    variant = LinearProgressVariant.determinate
                                    value = (transferJob.progressAsFloat * 100).toInt()
                                }
                            }
                            TransferJobItemStatus.STATUS_FAILED -> {
                                ErrorIcon {
                                    sx {
                                        marginRight = theme.spacing(1)
                                    }
                                    fontSize = SvgIconSize.small
                                }

                                +strings[MR.strings.upload_failed]
                            }
                        }
                    }
                }
                secondaryTypographyProps = jso {
                    component = ReactHTML.div
                }
            }
        }
    }
}
