package com.ustadmobile.view.clazzassignment

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.ext.progressAsFloat
import com.ustadmobile.lib.db.composites.CourseAssignmentSubmissionFileAndTransferJob
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import js.objects.jso
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
import mui.icons.material.Clear as ClearIcon
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.mui.components.ThemeContext
import mui.material.IconButton
import mui.material.LinearProgressVariant
import mui.material.ListItemIcon
import mui.material.Tooltip
import mui.system.sx
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.br
import react.useRequiredContext
import mui.icons.material.TextSnippet as TextSnippetIcon

external interface CourseAssignmentSubmissionFileListItemProps : Props {
    var file: CourseAssignmentSubmissionFileAndTransferJob
    var onRemove: ((CourseAssignmentSubmissionFileAndTransferJob) -> Unit)?
    var onClick: ((CourseAssignmentSubmissionFileAndTransferJob) -> Unit)?
}

val CourseAssignmentSubmissionFileListItem = FC<CourseAssignmentSubmissionFileListItemProps> { props ->
    val strings = useStringProvider()
    val theme by useRequiredContext(ThemeContext)
    val onRemoveVal = props.onRemove

    ListItem {
        ListItemButton {
            props.onClick?.also { onClickFn ->
                onClick = {
                    onClickFn(props.file)
                }
            }

            ListItemIcon {
                TextSnippetIcon()
            }

            ListItemText {
                primary = ReactNode(props.file.submissionFile?.casaFileName ?: "")
                secondary = Stack.create {
                    direction = responsive(StackDirection.column)

                    props.file.submissionFile?.also {
                        + (UMFileUtil.formatFileSize(it.casaSize.toLong()))
                        br()
                    }

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

        secondaryAction = onRemoveVal?.let { onRemoveFn ->
            Tooltip.create {
                title = ReactNode(strings[MR.strings.remove])
                IconButton {
                    ariaLabel = strings[MR.strings.remove]
                    onClick = {
                        onRemoveFn(props.file)
                    }
                    ClearIcon()
                }
            }
        }
    }
}
