package com.ustadmobile.view.clazzassignment.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.entityconstants.*
import com.ustadmobile.core.viewmodel.clazzassignment.edit.ClazzAssignmentEditUiState
import com.ustadmobile.core.viewmodel.clazzassignment.ClazzAssignmentViewModelConstants.TextLimitType
import com.ustadmobile.core.viewmodel.clazzassignment.ClazzAssignmentViewModelConstants.MarkingType
import com.ustadmobile.core.viewmodel.clazzassignment.edit.ClazzAssignmentEditViewModel
import com.ustadmobile.hooks.courseTerminologyResource
import com.ustadmobile.hooks.useCourseTerminologyEntries
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.common.input
import com.ustadmobile.mui.common.readOnly
import com.ustadmobile.mui.components.UstadCourseBlockEdit
import com.ustadmobile.mui.components.UstadNumberTextField
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTextField
import com.ustadmobile.view.components.UstadMessageIdSelectField
import com.ustadmobile.view.components.UstadSelectField
import com.ustadmobile.view.components.UstadSwitchField
import web.cssom.Cursor
import web.cssom.px
import js.objects.jso
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode

external interface ClazzAssignmentEditScreenProps : Props {

    var uiState: ClazzAssignmentEditUiState

    var onAssignmentChanged: (ClazzAssignment?) -> Unit

    //Used by embedded CourseBlockEdit
    var onChangeCourseBlock: (CourseBlock?) -> Unit

    var onClickSubmissionType: () -> Unit

    var onClickAssignReviewers: () -> Unit

    var onGroupSubmissionOnChanged: (Boolean) -> Unit

    var onPictureChanged: (String?) -> Unit

}


private val ClazzAssignmentEditScreenComponent2 = FC<ClazzAssignmentEditScreenProps> { props ->

    val strings = useStringProvider()

    val terminologyEntries = useCourseTerminologyEntries(props.uiState.courseTerminology)

    UstadStandardContainer {

        Stack {
            spacing = responsive(20.px)

            UstadCourseBlockEdit {
                uiState = props.uiState.courseBlockEditUiState
                onCourseBlockChange = props.onChangeCourseBlock
                onPictureChanged = props.onPictureChanged
            }


            UstadSwitchField {
                label = strings[MR.strings.group_assignment]
                checked = props.uiState.groupSubmissionOn
                onChanged = props.onGroupSubmissionOnChanged
                enabled = props.uiState.fieldsEnabled
                id = "group_submission_on"
            }

            if(props.uiState.groupSubmissionOn) {
                UstadTextField {
                    id = "cgsName"
                    sx {
                        input {
                            cursor = Cursor.pointer
                        }
                    }
                    variant = FormControlVariant.outlined
                    value = props.uiState.entity?.assignmentCourseGroupSetName
                        ?: "(${strings[MR.strings.unset]})"
                    label = ReactNode(strings[MR.strings.groups])
                    disabled = !props.uiState.groupSetEnabled
                    inputProps = jso {
                        readOnly = true
                    }
                    onClick = { props.onClickSubmissionType() }
                    error = props.uiState.groupSetError != null
                    helperText = ReactNode(props.uiState.groupSetError ?: strings[MR.strings.required])
                }
            }


            UstadSwitchField {
                id = "caRequireFileSubmission"
                label = strings[MR.strings.require_file_submission]
                checked = props.uiState.entity?.assignment?.caRequireFileSubmission ?: false
                error = props.uiState.submissionRequiredError
                onChanged = {
                    props.onAssignmentChanged(props.uiState.entity?.assignment?.shallowCopy {
                        caRequireFileSubmission = it
                    })
                }
                enabled = props.uiState.fieldsEnabled
            }

            if (props.uiState.fileSubmissionVisible){
                /* Not currently enforced
                UstadMessageIdSelectField {
                    id = "caFileType"
                    value = props.uiState.entity?.assignment?.caFileType ?: 0
                    label = strings[MR.strings.file_type]
                    options = FileTypeConstants.FILE_TYPE_MESSAGE_IDS
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onAssignmentChanged(
                            props.uiState.entity?.assignment?.shallowCopy {
                                caFileType = it.value
                            }
                        )
                    }
                }*/

                UstadNumberTextField {
                    id = "caSizeLimit"
                    variant = FormControlVariant.outlined
                    numValue = (props.uiState.entity?.assignment?.caSizeLimit ?: 0).toFloat()
                    label = ReactNode(strings[MR.strings.size_limit])
                    disabled = !props.uiState.fieldsEnabled
                    error = props.uiState.sizeLimitError != null
                    helperText = props.uiState.sizeLimitError?.let { ReactNode(it) }
                    onChange = {
                        props.onAssignmentChanged(
                            props.uiState.entity?.assignment?.shallowCopy {
                                caSizeLimit = it.toInt()
                            }
                        )
                    }
                }

                UstadNumberTextField {
                    id = "caNumberOfFiles"
                    variant = FormControlVariant.outlined
                    numValue = (props.uiState.entity?.assignment?.caNumberOfFiles ?: 0).toFloat()
                    label = ReactNode(strings[MR.strings.number_of_files])
                    disabled = !props.uiState.fieldsEnabled
                    onChange = {
                        props.onAssignmentChanged(
                            props.uiState.entity?.assignment?.shallowCopy {
                                caNumberOfFiles = it.toInt()
                            }
                        )
                    }
                }
            }

            UstadSwitchField {
                id = "caRequireTextSubmission"
                label = strings[MR.strings.require_text_submission]
                checked = props.uiState.entity?.assignment?.caRequireTextSubmission ?: false
                error = props.uiState.submissionRequiredError
                onChanged = {
                    props.onAssignmentChanged(props.uiState.entity?.assignment?.shallowCopy {
                        caRequireTextSubmission = it
                    })
                }
                enabled = props.uiState.fieldsEnabled
            }

            if (props.uiState.textSubmissionVisible) {
                UstadSelectField<TextLimitType> {
                    id = "caTextLimitType"
                    options = TextLimitType.values().toList()
                    value = TextLimitType.values().firstOrNull {
                        it.value == props.uiState.entity?.assignment?.caTextLimitType
                    } ?: TextLimitType.LIMIT_WORDS
                    itemValue = { it.value.toString() }
                    itemLabel = { ReactNode(strings[it.stringResource]) }
                    label = strings[MR.strings.limit]
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onAssignmentChanged(
                            props.uiState.entity?.assignment?.shallowCopy {
                                caTextLimitType = it.value
                            }
                        )
                    }
                }

                UstadNumberTextField {
                    id = "caTextLimit"
                    numValue = (props.uiState.entity?.assignment?.caTextLimit ?: 0).toFloat()
                    label = ReactNode(strings[MR.strings.maximum])
                    placeholder = strings[MR.strings.maximum]
                    disabled = !props.uiState.fieldsEnabled

                    onChange = {
                        props.onAssignmentChanged(
                            props.uiState.entity?.assignment?.shallowCopy {
                                caTextLimit = it.toInt()
                            }
                        )
                    }
                }
            }

            UstadMessageIdSelectField {
                id = "caSubmissionPolicy"
                value = props.uiState.entity?.assignment?.caSubmissionPolicy
                    ?: ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
                label = strings[MR.strings.submission_policy]
                options = SubmissionPolicyConstants.SUBMISSION_POLICY_MESSAGE_IDS
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onAssignmentChanged(
                        props.uiState.entity?.assignment?.shallowCopy {
                            caSubmissionPolicy = it.value
                        }
                    )
                }
            }

            UstadSelectField<MarkingType> {
                id = "caMarkingType"
                value = MarkingType.valueOf(props.uiState.entity?.assignment?.caMarkingType ?: 0)
                label = strings[MR.strings.marked_by]
                options = MarkingType.entries
                enabled = props.uiState.markingTypeEnabled
                itemLabel = { markingType ->
                    val text = if(markingType == MarkingType.PEERS){
                        strings[MR.strings.peers]
                    }else {
                        courseTerminologyResource(terminologyEntries, strings, MR.strings.teacher)
                    }

                    ReactNode(text)
                }
                itemValue = { it.toString() }
                onChange = {
                    props.onAssignmentChanged(
                        props.uiState.entity?.assignment?.shallowCopy {
                            caMarkingType = it.value
                        }
                    )
                }
            }

            if (props.uiState.peerMarkingVisible) {
                Stack {
                    direction = responsive(StackDirection.row)
                    spacing = responsive(10.px)

                    UstadNumberTextField {
                        id = "caPeerReviewerCount"
                        fullWidth = true
                        variant = FormControlVariant.outlined
                        numValue = (props.uiState.entity?.assignment?.caPeerReviewerCount ?: 0).toFloat()
                        label = ReactNode(strings[MR.strings.reviews_per_user_group])
                        disabled = !props.uiState.fieldsEnabled
                        error = props.uiState.reviewerCountError != null
                        helperText = props.uiState.reviewerCountError?.let { ReactNode(it) }
                        onChange = {
                            props.onAssignmentChanged(
                                props.uiState.entity?.assignment?.shallowCopy {
                                    caPeerReviewerCount = it.toInt()
                                }
                            )
                        }
                    }

                    Button {
                        id = "buttonAssignReviewers"
                        fullWidth = true
                        onClick = { props.onClickAssignReviewers() }
                        disabled = !props.uiState.fieldsEnabled
                        variant = ButtonVariant.outlined
                        + strings[MR.strings.assign_reviewers]
                    }
                }
            }

            UstadSwitchField {
                id = "caClassCommentEnabled"
                label = strings[MR.strings.allow_class_comments]
                checked = props.uiState.entity?.assignment?.caClassCommentEnabled ?: false
                onChanged = {
                    props.onAssignmentChanged(props.uiState.entity?.assignment?.shallowCopy {
                        caClassCommentEnabled = it
                    })
                }
                enabled = props.uiState.fieldsEnabled
            }

            UstadSwitchField {
                id = "caPrivateCommentsEnabled"
                label = strings[MR.strings.allow_private_comments_from_students]
                checked = props.uiState.entity?.assignment?.caPrivateCommentsEnabled ?: false
                onChanged = {
                    props.onAssignmentChanged(props.uiState.entity?.assignment?.shallowCopy {
                        caPrivateCommentsEnabled = it
                    })
                }
                enabled = props.uiState.fieldsEnabled
            }
        }
    }
}

val ClazzAssignmentEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzAssignmentEditViewModel(di, savedStateHandle)
    }

    val uiStateVal: ClazzAssignmentEditUiState by viewModel.uiState.collectAsState(
        ClazzAssignmentEditUiState())

    ClazzAssignmentEditScreenComponent2 {
        uiState = uiStateVal
        onAssignmentChanged = viewModel::onAssignmentChanged
        onChangeCourseBlock = viewModel::onCourseBlockChanged
        onClickSubmissionType = viewModel::onClickSubmissionType
        onClickAssignReviewers = viewModel::onClickAssignReviewers
        onGroupSubmissionOnChanged = viewModel::onGroupSubmissionOnChanged
        onPictureChanged = viewModel::onPictureChanged
    }

}