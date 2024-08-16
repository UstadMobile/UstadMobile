package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.courseblock.CourseBlockViewModelConstants.CompletionCriteria
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.UstadImageSelectButton
import com.ustadmobile.view.components.UstadSelectField
import com.ustadmobile.wrappers.quill.ReactQuill
import web.cssom.px
import js.objects.jso
import kotlinx.datetime.TimeZone
import mui.icons.material.BookOutlined as BookOutlinedIcon
import mui.material.*
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useRequiredContext
import mui.icons.material.Edit as EditIcon

external interface UstadCourseBlockEditProps: Props {

    var uiState: CourseBlockEditUiState

    var onCourseBlockChange: ((CourseBlock?) -> Unit)

    var onClickEditSelectedContentEntry: () -> Unit

    var onPictureChanged: (String?) -> Unit

}

/**
 * Base component for showing detail fields e.g. phone number, start date, end date, etc.
 */
val UstadCourseBlockEdit = FC<UstadCourseBlockEditProps> { props ->

    val strings = useStringProvider()

    val theme by useRequiredContext(ThemeContext)

    Stack{
        spacing = responsive(theme.spacing(2))

        props.uiState.block?.contentEntry?.also { selectedContentEntry ->
            ListItem {
                ListItemIcon {
                    BookOutlinedIcon()
                }

                ListItemText {
                    primary = ReactNode(selectedContentEntry.title ?: "")
                    secondary = ReactNode(strings[MR.strings.selected_content])
                }

                if(props.uiState.canEditSelectedContentEntry) {
                    secondaryAction = Tooltip.create {
                        title = ReactNode(strings[MR.strings.edit])
                        IconButton {
                            onClick = {
                                props.onClickEditSelectedContentEntry()
                            }

                            EditIcon()
                        }
                    }
                }
            }
        }

        UstadImageSelectButton {
            imageUri = props.uiState.block?.courseBlockPicture?.cbpPictureUri
            onImageUriChanged = { imageUri ->
                props.onPictureChanged(imageUri)
            }
            disabled = !props.uiState.fieldsEnabled
        }

        UstadTextField {
            id = "title"
            value = props.uiState.block?.courseBlock?.cbTitle ?: ""
            label = ReactNode(strings[MR.strings.title] + "*")
            disabled = !props.uiState.fieldsEnabled
            fullWidth = true
            error = props.uiState.caTitleError != null
            helperText =  ReactNode(props.uiState.caTitleError ?: strings[MR.strings.required])
            onTextChange = {
                props.onCourseBlockChange(
                    props.uiState.block?.courseBlock?.copy(
                        cbTitle = it
                    )
                )
            }
        }


        ReactQuill {
            value = props.uiState.block?.courseBlock?.cbDescription ?: ""
            id = "description_quill"
            placeholder = strings[MR.strings.description]
            onChange = {
                props.uiState.block?.also { courseBlock ->
                    props.onCourseBlockChange(
                        courseBlock.courseBlock.copy(
                            cbDescription = it
                        )
                    )
                }
            }
        }


        UstadDateTimeField {
            timeInMillis = props.uiState.block?.courseBlock?.cbHideUntilDate ?: 0
            label = ReactNode(strings[MR.strings.dont_show_before])
            id = "hide_until_date"
            disabled = !props.uiState.fieldsEnabled
            helperText = props.uiState.caHideUntilDateError?.let { ReactNode(it) }
            error = props.uiState.caHideUntilDateError != null
            timeZoneId = props.uiState.timeZone
            onChange = {
                props.onCourseBlockChange(
                    props.uiState.block?.courseBlock?.copy(
                        cbHideUntilDate = it
                    )
                )
            }
        }

        Stack {
            direction = responsive(StackDirection.row)
            spacing = responsive(15.px)


            if(props.uiState.completionCriteriaVisible) {
                UstadSelectField<CompletionCriteria> {
                    id = "cbCompletionCriteria"
                    value = CompletionCriteria.valueOf(props.uiState.block?.courseBlock?.cbCompletionCriteria ?: 0)
                    label = strings[MR.strings.completion_criteria]
                    options = props.uiState.completionCriteriaOptions
                    itemValue = { it.value.toString() }
                    itemLabel = { ReactNode(strings[it.stringResource].capitalizeFirstLetter()) }
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onCourseBlockChange(
                            props.uiState.block?.courseBlock?.copy(
                                cbCompletionCriteria = it.value
                            )
                        )
                    }
                }
            }

            if (props.uiState.minScoreVisible){
                UstadNullableNumberTextField {
                    id = "cbMinPoints"
                    numValue = props.uiState.block?.courseBlock?.cbMinPoints
                    asDynamic().InputProps = jso<InputBaseProps> {
                        endAdornment = InputAdornment.create {
                            position = InputAdornmentPosition.end
                            + strings[MR.strings.points]
                        }
                    }
                    label = ReactNode(strings[MR.strings.points])
                    disabled = !props.uiState.fieldsEnabled
                    onChange = {
                        props.onCourseBlockChange(
                            props.uiState.block?.courseBlock?.copy(
                                cbMinPoints = it
                            )
                        )
                    }
                }
            }
        }

        if(props.uiState.maxPointsVisible) {
            UstadNullableNumberTextField {
                id = "cbMaxPoints"
                numValue = props.uiState.block?.courseBlock?.cbMaxPoints
                label = ReactNode(
                    buildString {
                        append(strings[MR.strings.maximum_points])
                        if(props.uiState.maxPointsRequired)
                            append("*")
                    }
                )
                error = (props.uiState.caMaxPointsError != null)
                helperText = props.uiState.caMaxPointsError?.let { ReactNode(it) }
                    ?: if(props.uiState.maxPointsRequired) {
                        ReactNode(strings[MR.strings.required])
                    } else {
                        null
                    }
                disabled = !props.uiState.fieldsEnabled
                onChange = {
                    props.onCourseBlockChange(
                        props.uiState.block?.courseBlock?.copy(
                            cbMaxPoints = it
                        )
                    )
                }
            }
        }


        if(props.uiState.deadlineVisible) {
            UstadDateTimeField {
                id = "cbDeadlineDate"
                timeInMillis = props.uiState.block?.courseBlock?.cbDeadlineDate ?: 0
                timeZoneId = props.uiState.timeZone
                unsetDefault = Long.MAX_VALUE
                label = ReactNode(strings[MR.strings.deadline])
                disabled = !props.uiState.fieldsEnabled
                helperText = props.uiState.caDeadlineError?.let { ReactNode(it) }
                error = props.uiState.caDeadlineError != null
                onChange = {
                    props.onCourseBlockChange(
                        props.uiState.block?.courseBlock?.copy(
                            cbDeadlineDate = it
                        )
                    )
                }
            }
        }


        if (props.uiState.gracePeriodVisible){
            UstadDateTimeField {
                id = "cbGracePeriodDate"
                timeInMillis = props.uiState.block?.courseBlock?.cbGracePeriodDate ?: 0
                timeZoneId = props.uiState.timeZone
                unsetDefault = Long.MAX_VALUE
                label = ReactNode(strings[MR.strings.end_of_grace_period])
                disabled = !props.uiState.fieldsEnabled
                helperText = props.uiState.caGracePeriodError?.let { ReactNode(it) }
                error = props.uiState.caGracePeriodError != null
                timeZoneId = TimeZone.currentSystemDefault().id
                onChange = {
                    props.onCourseBlockChange(
                        props.uiState.block?.courseBlock?.copy(
                            cbGracePeriodDate = it
                        )
                    )
                }
            }
        }

        if(props.uiState.latePenaltyVisible) {
            UstadNumberTextField {
                id = "cbLateSubmissionPenalty"
                numValue = (props.uiState.block?.courseBlock?.cbLateSubmissionPenalty ?: 0).toFloat()
                label = ReactNode(strings[MR.strings.late_submission_penalty])
                disabled = !props.uiState.fieldsEnabled
                helperText = ReactNode(strings[MR.strings.penalty_label])
                asDynamic().InputProps = jso<InputBaseProps> {
                    endAdornment = InputAdornment.create {
                        position = InputAdornmentPosition.end
                        + "%"
                    }
                }
                onChange = { newString ->
                    props.onCourseBlockChange(
                        props.uiState.block?.courseBlock?.copy(
                            cbLateSubmissionPenalty = newString.toInt()
                        )
                    )
                }
            }
        }
    }
}

val UstadCourseBlockEditPreview = FC<Props> {
    Container {
        maxWidth = "lg"

        UstadCourseBlockEdit {
            uiState = CourseBlockEditUiState(
                block = CourseBlockAndEditEntities(
                    courseBlock = CourseBlock().apply {
                        cbMaxPoints = 78f
                        cbCompletionCriteria = ContentEntry.COMPLETION_CRITERIA_MIN_SCORE
                    }
                ),
            )
        }
    }
}