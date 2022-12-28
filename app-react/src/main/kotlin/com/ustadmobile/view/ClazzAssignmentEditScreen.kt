package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.*
import com.ustadmobile.core.viewmodel.ClazzAssignmentEditUiState
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.ext.shallowCopyCourseBlockWithEntity
import com.ustadmobile.mui.components.UstadCourseBlockEdit
import com.ustadmobile.mui.components.UstadMessageIdDropDownField
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.util.ext.addOptionalSuffix
import com.ustadmobile.view.components.UstadSwitchField
import csstype.px
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props

external interface ClazzAssignmentEditScreenProps : Props {

    var uiState: ClazzAssignmentEditUiState

    var onChangeCourseBlockWithEntity: (CourseBlockWithEntity?) -> Unit

    var onChangeCourseBlock: (CourseBlock?) -> Unit

    var onClickSubmissionType: () -> Unit

    var onChangedFileRequired: (Boolean) -> Unit

    var onChangedTextRequired: (Boolean) -> Unit

    var onChangedAllowClassComments: (Boolean) -> Unit

    var onChangedAllowPrivateCommentsFromStudents: (Boolean) -> Unit

}

val ClazzAssignmentEditScreenPreview = FC<Props> {
    val strings = useStringsXml()
    ClazzAssignmentEditScreenComponent2 {
        uiState = ClazzAssignmentEditUiState(
            courseBlockEditUiState = CourseBlockEditUiState(
                courseBlock = CourseBlock().apply {
                    cbMaxPoints = 78
                    cbCompletionCriteria = 14
                },
                minScoreVisible = true,
                gracePeriodVisible = true,
            ),
        )
    }
}

private val ClazzAssignmentEditScreenComponent2 = FC<ClazzAssignmentEditScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(20.px)

            UstadTextEditField{
                value = props.uiState.entity?.assignment?.caTitle ?: ""
                label = strings[MessageID.title]
                error = props.uiState.caTitleError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onChangeCourseBlockWithEntity(
                        props.uiState.entity?.shallowCopyCourseBlockWithEntity {
                            assignment?.caTitle = it
                        })
                }
            }

            UstadTextEditField {
                value = props.uiState.entity?.assignment?.caDescription ?: ""
                label = strings[MessageID.description].addOptionalSuffix(strings)
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onChangeCourseBlockWithEntity(
                        props.uiState.entity?.shallowCopyCourseBlockWithEntity {
                            assignment?.caDescription = it
                        })
                }
            }

            UstadCourseBlockEdit {
                uiState = props.uiState.courseBlockEditUiState
                onCourseBlockChange = props.onChangeCourseBlock
            }

            UstadTextEditField {
                value = props.uiState.groupSet?.cgsName ?: ""
                label = strings[MessageID.submission_type]
                enabled = props.uiState.groupSetEnabled
                onChange = {}
                onClick = props.onClickSubmissionType
            }

            UstadSwitchField {
                label = strings[MessageID.require_file_submission]
                checked = props.uiState.entity?.assignment?.caRequireFileSubmission ?: false
                onChanged = { props.onChangedFileRequired(it) }
                enabled = props.uiState.fieldsEnabled
            }

            if (props.uiState.fileSubmissionVisible){
                UstadMessageIdDropDownField {
                    value = props.uiState.entity?.assignment?.caFileType ?: 0
                    label = strings[MessageID.file_type]
                    options = FileTypeConstants.FILE_TYPE_MESSAGE_IDS
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyCourseBlockWithEntity {
                                assignment?.caFileType = it?.value ?: 0
                            })
                    }
                }

                UstadTextEditField {
                    value = (props.uiState.entity?.assignment?.caSizeLimit ?: 0).toString()
                    label = strings[MessageID.size_limit]
                    enabled = props.uiState.fieldsEnabled
                    onChange = { newString ->
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyCourseBlockWithEntity {
                                assignment?.caSizeLimit =
                                    newString.filter { it.isDigit() }.toIntOrNull() ?: 0
                            })
                    }
                }

                UstadTextEditField {
                    value = (props.uiState.entity?.assignment?.caNumberOfFiles ?: 0).toString()
                    label = strings[MessageID.number_of_files]
                    enabled = props.uiState.fieldsEnabled
                    onChange = { newString ->
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyCourseBlockWithEntity {
                                assignment?.caNumberOfFiles =
                                    newString.filter { it.isDigit() }.toIntOrNull() ?: 0
                            })
                    }
                }
            }

            UstadSwitchField {
                label = strings[MessageID.require_text_submission]
                checked = props.uiState.entity?.assignment?.caRequireTextSubmission ?: false
                onChanged = { props.onChangedTextRequired(it) }
                enabled = props.uiState.fieldsEnabled
            }

            if (props.uiState.textSubmissionVisible) {
                UstadMessageIdDropDownField {
                    value = props.uiState.entity?.assignment?.caTextLimitType ?: 0
                    label = strings[MessageID.limit]
                    options = TextLimitTypeConstants.TEXT_LIMIT_TYPE_MESSAGE_IDS
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyCourseBlockWithEntity {
                                assignment?.caTextLimitType = it?.value ?: 0
                            })
                    }
                }

                UstadTextEditField {
                    value = (props.uiState.entity?.assignment?.caTextLimit ?: 0).toString()
                    label = strings[MessageID.maximum]
                    enabled = props.uiState.fieldsEnabled
                    onChange = { newString ->
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyCourseBlockWithEntity {
                                assignment?.caTextLimit =
                                    newString.filter { it.isDigit() }.toIntOrNull() ?: 0
                            })
                    }
                }
            }

            UstadMessageIdDropDownField {
                value = props.uiState.entity?.assignment?.caSubmissionPolicy ?: 0
                label = strings[MessageID.submission_policy]
                options = SubmissionPolicyConstants.SUBMISSION_POLICY_MESSAGE_IDS
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onChangeCourseBlockWithEntity(
                        props.uiState.entity?.shallowCopyCourseBlockWithEntity {
                            assignment?.caSubmissionPolicy = it?.value ?: 0
                        })
                }
            }

            UstadMessageIdDropDownField {
                value = props.uiState.entity?.assignment?.caMarkingType ?: 0
                label = strings[MessageID.marked_by]
                options = MarkingTypeConstants.MARKING_TYPE_MESSAGE_IDS
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onChangeCourseBlockWithEntity(
                        props.uiState.entity?.shallowCopyCourseBlockWithEntity {
                            assignment?.caMarkingType = it?.value ?: 0
                        })
                }
            }

            UstadSwitchField {
                label = strings[MessageID.allow_class_comments]
                checked = props.uiState.entity?.assignment?.caClassCommentEnabled ?: false
                onChanged = { props.onChangedAllowClassComments(it) }
                enabled = props.uiState.fieldsEnabled
            }

            UstadSwitchField {
                label = strings[MessageID.allow_private_comments_from_students]
                checked = props.uiState.entity?.assignment?.caPrivateCommentsEnabled ?: false
                onChanged = { props.onChangedAllowPrivateCommentsFromStudents(it) }
                enabled = props.uiState.fieldsEnabled
            }
        }
    }
}