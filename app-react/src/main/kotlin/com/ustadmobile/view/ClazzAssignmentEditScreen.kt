package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.*
import com.ustadmobile.core.viewmodel.ClazzAssignmentEditUiState
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.hooks.courseTerminologyResource
import com.ustadmobile.hooks.useCourseTerminologyEntries
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.lib.db.entities.ext.shallowCopyWithEntity
import com.ustadmobile.mui.components.UstadCourseBlockEdit
import com.ustadmobile.mui.components.UstadNumberTextField
import com.ustadmobile.view.components.UstadMessageIdSelectField
import com.ustadmobile.view.components.UstadSelectField
import com.ustadmobile.view.components.UstadSwitchField
import csstype.px
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.dom.onChange

external interface ClazzAssignmentEditScreenProps : Props {

    var uiState: ClazzAssignmentEditUiState

    var onChangeCourseBlockWithEntity: (CourseBlockWithEntity?) -> Unit

    var onChangeCourseBlock: (CourseBlock?) -> Unit

    var onClickSubmissionType: () -> Unit

    var onChangedFileRequired: (Boolean) -> Unit

    var onChangedTextRequired: (Boolean) -> Unit

    var onChangedAllowClassComments: (Boolean) -> Unit

    var onChangedAllowPrivateCommentsFromStudents: (Boolean) -> Unit

    var onClickAssignReviewers: () -> Unit

}

val ClazzAssignmentEditScreenPreview = FC<Props> {

    ClazzAssignmentEditScreenComponent2 {
        uiState = ClazzAssignmentEditUiState(
            courseBlockEditUiState = CourseBlockEditUiState(
                courseBlock = CourseBlock().apply {
                    cbMaxPoints = 78
                    cbCompletionCriteria = 14
                },
                gracePeriodVisible = true,
            ),
            minScoreVisible = true,
            textSubmissionVisible = true,
            fileSubmissionVisible = true,
            entity = CourseBlockWithEntity().apply {
                assignment = ClazzAssignment().apply {
                    caMarkingType = ClazzAssignment.MARKED_BY_PEERS
                }
            }
        )
    }
}

private val ClazzAssignmentEditScreenComponent2 = FC<ClazzAssignmentEditScreenProps> { props ->

    val strings = useStringsXml()

    val terminologyEntries = useCourseTerminologyEntries(props.uiState.courseTerminology)

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(20.px)

            UstadCourseBlockEdit {
                uiState = props.uiState.courseBlockEditUiState
                onCourseBlockChange = props.onChangeCourseBlock
            }

            TextField {
                id = "cgsName"
                variant = FormControlVariant.outlined
                value = props.uiState.groupSet?.cgsName ?: ""
                label = ReactNode(strings[MessageID.submission_type])
                disabled = !props.uiState.groupSetEnabled
                onChange = {}
                onClick = { props.onClickSubmissionType }
            }

            UstadSwitchField {
                id = "caRequireFileSubmission"
                label = strings[MessageID.require_file_submission]
                checked = props.uiState.entity?.assignment?.caRequireFileSubmission ?: false
                onChanged = { props.onChangedFileRequired(it) }
                enabled = props.uiState.fieldsEnabled
            }

            if (props.uiState.fileSubmissionVisible){
                UstadMessageIdSelectField {
                    id = "caFileType"
                    value = props.uiState.entity?.assignment?.caFileType ?: 0
                    label = strings[MessageID.file_type]
                    options = FileTypeConstants.FILE_TYPE_MESSAGE_IDS
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyWithEntity {
                                assignment = props.uiState.entity?.assignment?.shallowCopy {
                                    caFileType = it.value
                                }
                            })
                    }
                }

                UstadNumberTextField {
                    id = "caSizeLimit"
                    variant = FormControlVariant.outlined
                    value = (props.uiState.entity?.assignment?.caSizeLimit ?: 0).toFloat()
                    label = ReactNode(strings[MessageID.size_limit])
                    disabled = !props.uiState.fieldsEnabled
                    onChange = {
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyWithEntity {
                                assignment = props.uiState.entity?.assignment?.shallowCopy {
                                    caSizeLimit = it.toInt()
                                }
                            })
                    }
                }

                UstadNumberTextField {
                    id = "caNumberOfFiles"
                    variant = FormControlVariant.outlined
                    value = (props.uiState.entity?.assignment?.caNumberOfFiles ?: 0).toFloat()
                    label = ReactNode(strings[MessageID.number_of_files])
                    disabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyWithEntity {
                                assignment = props.uiState.entity?.assignment?.shallowCopy {
                                    caNumberOfFiles = it.toInt()
                                }
                            })
                    }
                }
            }

            UstadSwitchField {
                id = "caRequireTextSubmission"
                label = strings[MessageID.require_text_submission]
                checked = props.uiState.entity?.assignment?.caRequireTextSubmission ?: false
                onChanged = { props.onChangedTextRequired(it) }
                enabled = props.uiState.fieldsEnabled
            }

            if (props.uiState.textSubmissionVisible) {
                UstadMessageIdSelectField {
                    id = "caTextLimitType"
                    value = props.uiState.entity?.assignment?.caTextLimitType ?: 0
                    label = strings[MessageID.limit]
                    options = TextLimitTypeConstants.TEXT_LIMIT_TYPE_MESSAGE_IDS
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyWithEntity {
                                assignment = props.uiState.entity?.assignment?.shallowCopy {
                                    caTextLimitType = it.value
                                }
                            })
                    }
                }

                UstadNumberTextField {
                    id = "caTextLimit"
                    value = (props.uiState.entity?.assignment?.caTextLimit ?: 0).toFloat()
                    label = ReactNode(strings[MessageID.maximum])
                    placeholder = strings[MessageID.maximum]
                    disabled = !props.uiState.fieldsEnabled

                    onChange = {
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyWithEntity {
                                assignment = props.uiState.entity?.assignment?.shallowCopy {
                                    caTextLimit = it.toInt()
                                }
                            })
                    }
                }
            }

            UstadMessageIdSelectField {
                id = "caSubmissionPolicy"
                value = props.uiState.entity?.assignment?.caSubmissionPolicy ?: 0
                label = strings[MessageID.submission_policy]
                options = SubmissionPolicyConstants.SUBMISSION_POLICY_MESSAGE_IDS
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onChangeCourseBlockWithEntity(
                        props.uiState.entity?.shallowCopyWithEntity {
                            assignment = props.uiState.entity?.assignment?.shallowCopy {
                                caSubmissionPolicy = it.value
                            }
                        })
                }
            }

            UstadSelectField<Int> {
                id = "caMarkingType"
                value = (props.uiState.entity?.assignment?.caMarkingType
                    ?: ClazzAssignment.MARKED_BY_COURSE_LEADER).toString()
                label = strings[MessageID.marked_by]
                options = listOf(ClazzAssignment.MARKED_BY_COURSE_LEADER, ClazzAssignment.MARKED_BY_PEERS)
                enabled = props.uiState.markingTypeEnabled
                itemLabel = { markingType ->
                    val messageId = MarkingTypeConstants.MARKING_TYPE_MESSAGE_IDS.first {
                        it.value == markingType
                    }.messageId
                    ReactNode(courseTerminologyResource(
                        terminologyEntries, strings, messageId))
                }
                itemValue = { it.toString() }
                onChange = {
                    props.onChangeCourseBlockWithEntity(
                        props.uiState.entity?.shallowCopyWithEntity {
                            assignment = props.uiState.entity?.assignment?.shallowCopy {
                                caMarkingType = it
                            }
                        })
                }
            }

            if (props.uiState.peerMarkingVisible) {
                Stack {
                    direction = responsive(StackDirection.row)

                    Stack {
                        UstadNumberTextField {
                            id = "caPeerReviewerCount"
                            fullWidth = true
                            variant = FormControlVariant.outlined
                            value = (props.uiState.entity?.assignment?.caPeerReviewerCount ?: 0).toFloat()
                            label = ReactNode(strings[MessageID.reviews_per_user_group])
                            disabled = !props.uiState.fieldsEnabled
                            error = props.uiState.reviewerCountError != null
                            helperText = props.uiState.reviewerCountError?.let { ReactNode(it) }
                            onChange = {
                                props.onChangeCourseBlockWithEntity(
                                    props.uiState.entity?.shallowCopyWithEntity{
                                        assignment = props.uiState.entity?.assignment?.shallowCopy {
                                            caPeerReviewerCount = it.toInt()
                                        }
                                    })
                            }
                        }
                    }

                    Button {
                        fullWidth = true
                        onClick = { props.onClickAssignReviewers() }
                        disabled = !props.uiState.fieldsEnabled
                        variant = ButtonVariant.contained
                        + strings[MessageID.assign_reviewers]
                    }
                }
            }

            UstadSwitchField {
                id = "caClassCommentEnabled"
                label = strings[MessageID.allow_class_comments]
                checked = props.uiState.entity?.assignment?.caClassCommentEnabled ?: false
                onChanged = { props.onChangedAllowClassComments(it) }
                enabled = props.uiState.fieldsEnabled
            }

            UstadSwitchField {
                id = "caPrivateCommentsEnabled"
                label = strings[MessageID.allow_private_comments_from_students]
                checked = props.uiState.entity?.assignment?.caPrivateCommentsEnabled ?: false
                onChanged = { props.onChangedAllowPrivateCommentsFromStudents(it) }
                enabled = props.uiState.fieldsEnabled
            }
        }
    }
}