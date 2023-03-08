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
import com.ustadmobile.util.ext.addOptionalSuffix
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
                gracePeriodVisible = true,
            ),
            minScoreVisible = true,
        )
    }
}

private val ClazzAssignmentEditScreenComponent2 = FC<ClazzAssignmentEditScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(20.px)

            TextField {
                value = props.uiState.entity?.assignment?.caTitle ?: ""
                label = ReactNode(strings[MessageID.title])
                error = props.uiState.caTitleError != null
                disabled = !props.uiState.fieldsEnabled
                onChange = {
                    val currentVal = it.target.asDynamic().value
                    props.uiState.caTitleError = null
                    props.onChangeCourseBlockWithEntity(
                        props.uiState.entity?.shallowCopyCourseBlockWithEntity {
                            assignment?.caTitle = currentVal?.toString() ?: ""
                        })
                }
            }

            TextField {
                value = props.uiState.entity?.assignment?.caDescription ?: ""
                label = ReactNode(strings[MessageID.description].addOptionalSuffix(strings))
                disabled = !props.uiState.fieldsEnabled
                onChange = {
                    val currentVal = (it.target.asDynamic().value)?.toString() ?: ""
                    props.onChangeCourseBlockWithEntity(
                        props.uiState.entity?.shallowCopyCourseBlockWithEntity {
                            assignment?.caDescription = currentVal
                        })
                }
            }

            UstadCourseBlockEdit {
                uiState = props.uiState.courseBlockEditUiState
                onCourseBlockChange = props.onChangeCourseBlock
            }

            TextField {
                value = props.uiState.groupSet?.cgsName ?: ""
                label = ReactNode(strings[MessageID.submission_type])
                disabled = !props.uiState.groupSetEnabled
                onChange = {}
                onClick = { props.onClickSubmissionType }
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

                TextField {
                    value = (props.uiState.entity?.assignment?.caSizeLimit ?: 0).toString()
                    label = ReactNode(strings[MessageID.size_limit])
                    disabled = !props.uiState.fieldsEnabled
                    onChange = {
                        val currentVal = (it.target.asDynamic().value)?.toString() ?: ""
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyCourseBlockWithEntity {
                                assignment?.caSizeLimit =
                                    currentVal.filter { it.isDigit() }.toIntOrNull() ?: 0
                            })
                    }
                }

                TextField {
                    value = (props.uiState.entity?.assignment?.caNumberOfFiles ?: 0).toString()
                    label = ReactNode(strings[MessageID.number_of_files])
                    disabled = props.uiState.fieldsEnabled
                    onChange = {
                        val currentVal = (it.target.asDynamic().value)?.toString() ?: ""
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyCourseBlockWithEntity {
                                assignment?.caNumberOfFiles =
                                    currentVal.filter { it.isDigit() }.toIntOrNull() ?: 0
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

                TextField {
                    value = (props.uiState.entity?.assignment?.caTextLimit ?: 0).toString()
                    label = ReactNode(strings[MessageID.maximum])
                    disabled = !props.uiState.fieldsEnabled
                    onChange = {
                        val currentVal = (it.target.asDynamic().value)?.toString() ?: ""
                        props.onChangeCourseBlockWithEntity(
                            props.uiState.entity?.shallowCopyCourseBlockWithEntity {
                                assignment?.caTextLimit =
                                    currentVal.filter { it.isDigit() }.toIntOrNull() ?: 0
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