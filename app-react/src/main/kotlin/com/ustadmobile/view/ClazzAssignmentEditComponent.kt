package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.mui.components.umTextField
import com.ustadmobile.mui.components.umTextFieldSelect
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.fieldsOnlyFormScreen
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.clean
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.renderCourseBlockCommonFields
import com.ustadmobile.view.ext.renderListItemWithTitleAndSwitch
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import mui.material.FormControlVariant
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ClazzAssignmentEditComponent(mProps: UmProps): UstadEditComponent<CourseBlockWithEntity>(mProps),
    ClazzAssignmentEditView {

    private var mPresenter: ClazzAssignmentEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseBlockWithEntity>?
        get() = mPresenter

    private var nameLabel = FieldLabel(text = getString(MessageID.title))

    private var descriptionLabel = FieldLabel(text = getStringWithOptionalLabel(MessageID.description))

    private var doNotShowBeforeLabel = FieldLabel(text = getString(MessageID.dont_show_before).clean())

    private var startTimeLabel = FieldLabel(text = getString(MessageID.time))

    private var deadlineDateLabel = FieldLabel(text = getStringWithOptionalLabel(MessageID.deadline))

    private var deadlineTimeLabel = FieldLabel(text = getString(MessageID.time))

    private var gracePeriodDateLabel = FieldLabel(text = getStringWithOptionalLabel(MessageID.end_of_grace_period))

    private var gracePeriodTimeLabel = FieldLabel(text = getString(MessageID.time))

    private var completionCriteriaLabel = FieldLabel(text = getString(MessageID.completion_criteria))

    private var submissionTypeLabel = FieldLabel(text = getString(MessageID.submission_type))

    private var fileTypeLabel = FieldLabel(text = getString(MessageID.file_type))

    private var maxPointsLabel = FieldLabel(text = getString(MessageID.maximum_points))

    private var fileSizeLimitLabel = FieldLabel(text = getString(MessageID.size_limit))

    private var fileNumberLimitLabel = FieldLabel(text = getString(MessageID.max_number_of_files).format(""))

    private var textLimitTypeLabel = FieldLabel(text = getString(MessageID.limit))

    private var textLimitMaxLabel = FieldLabel(text = getString(MessageID.maximum))

    private var penaltyLabel = FieldLabel(text = getString(MessageID.late_submission_penalty))

    private var submissionPolicyLabel = FieldLabel(text = getString(MessageID.submission_policy))

    private var markedByLabel = FieldLabel(text = getString(MessageID.marked_by))

    override var caGracePeriodError: String? = null
        get() = field
        set(value) {
            field = value
            setState {
                gracePeriodDateLabel = gracePeriodDateLabel.copy(errorText = value)
            }
        }

    override var caDeadlineError: String? = null
        get() = field
        set(value) {
            field = value
            setState {
                deadlineDateLabel = deadlineDateLabel.copy(errorText = value)
            }
        }

    override var caTitleError: String? = null
        get() = field
        set(value) {
            field = value
            setState {
                nameLabel = nameLabel.copy(errorText = value)
            }
        }

    override var caStartDateError: String? = null
        get() = field
        set(value) {
            field = value
            setState {
                doNotShowBeforeLabel = doNotShowBeforeLabel.copy(errorText = value)
            }
        }
    override var caMaxPointsError: String? = null
        get() = field
        set(value) {
            field = value
            setState {
                maxPointsLabel = maxPointsLabel.copy(errorText = value)
            }
        }

    override var startDate: Long = 0L
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var startTime: Long = 0L
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var deadlineDate: Long = 0L
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var deadlineTime: Long= 0L
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var gracePeriodDate: Long= 0L
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var gracePeriodTime: Long= 0L
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var timeZone: String? = null
        get() = field
        set(value) {
            val newText = getString(MessageID.class_timezone) + " " + value
            setState {
                field = newText
            }
        }

    override var groupSet: CourseGroupSet? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var submissionPolicyOptions: List<ClazzAssignmentEditPresenter.SubmissionPolicyOptionsMessageIdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var fileTypeOptions: List<ClazzAssignmentEditPresenter.FileTypeOptionsMessageIdOption>?= null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var textLimitTypeOptions: List<ClazzAssignmentEditPresenter.TextLimitTypeOptionsMessageIdOption>?= null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var completionCriteriaOptions: List<ClazzAssignmentEditPresenter.CompletionCriteriaOptionsMessageIdOption>?= null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var markingTypeOptions: List<IdOption>?= null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var groupSetEnabled: Boolean = false
        get() = field
        set(value) {
            setState{
                field = value
            }
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: CourseBlockWithEntity? = null
        get() = field
        set(value) {
            setState {
                field = value
                requireFileSubmission = value?.assignment?.caRequireFileSubmission ?: true
                requireTextSubmission = value?.assignment?.caRequireTextSubmission ?: true
                if(value?.cbDeadlineDate != Long.MAX_VALUE){
                    gracePeriodVisiblity = true
                }
            }
        }


    private var requireFileSubmission: Boolean = false
        set(value) {
            setState {
                field = value
            }
        }

    private var requireTextSubmission: Boolean = false
        set(value) {
            setState {
                field = value
            }
        }

    private var gracePeriodVisiblity: Boolean = false
        set(value) {
            setState {
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        setEditTitle(MessageID.new_assignment, MessageID.edit_assignment)
        mPresenter = ClazzAssignmentEditPresenter(this, arguments, this, this, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        styledDiv {
            css{
                +fieldsOnlyFormScreen
            }

            umGridContainer{

                umItem(GridSize.cells12){

                    umTextField(label = "${nameLabel.text}",
                        helperText = nameLabel.errorText,
                        value = entity?.assignment?.caTitle,
                        error = nameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.assignment?.caTitle = it
                                caTitleError = null
                            }
                        }
                    )

                    umTextField(label = "${descriptionLabel.text}",
                        helperText = descriptionLabel.errorText,
                        value = entity?.assignment?.caDescription,
                        error = descriptionLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.assignment?.caDescription = it
                            }
                        }
                    )

                    renderCourseBlockCommonFields(entity,
                        doNotShowBeforeLabel, startDate, startTimeLabel,
                        dateSet = {
                            setState{
                                startDate = it.getTime().toLong()
                                caStartDateError = null
                            }
                        }, timeZone, completionCriteriaLabel, completionCriteriaOptions,
                        completionCriteriaSet = {
                            setState {
                                entity?.cbCompletionCriteria = it
                                completionCriteriaLabel.errorText = null
                            }
                        }, maxPointsLabel, maxPointsSet = {
                            setState {
                                entity?.cbMaxPoints = it
                                caMaxPointsError = null
                            }
                        }, deadlineDateLabel, deadlineTimeLabel, deadlineDate,
                        deadlineDateSet = {
                            setState{
                                deadlineDate = it.getTime().toLong()
                                caDeadlineError = null
                                gracePeriodVisiblity = true
                            }
                        }, gracePeriodDateLabel, gracePeriodTimeLabel, gracePeriodDate, gracePeriodVisiblity,
                        gracePeriodSet = {
                            gracePeriodDate = it.getTime().toLong()
                            caGracePeriodError = null
                        }, penaltyLabel, penaltySet = {
                            setState{
                                entity?.cbLateSubmissionPenalty = it
                            }
                        }, getString(MessageID.penalty_label),
                        false)


                    umTextField(label = "${submissionTypeLabel.text}",
                        helperText = submissionTypeLabel.errorText,
                        value = groupSet?.cgsName,
                        error = nameLabel.error,
                        disabled = !groupSetEnabled,
                        variant = FormControlVariant.outlined,
                        onClick = {
                            mPresenter?.handleSubmissionTypeClicked()
                        }
                    )

                    umItem {
                        css(defaultMarginTop)
                        renderListItemWithTitleAndSwitch(getString(MessageID.require_file_submission), entity?.assignment?.caRequireFileSubmission ?: false){
                            setState {
                                entity?.assignment?.caRequireFileSubmission = !(entity?.assignment?.caRequireFileSubmission ?: false)
                                requireFileSubmission = entity?.assignment?.caRequireFileSubmission ?: false
                            }
                        }
                    }

                    if(requireFileSubmission) {

                        umGridContainer(columnSpacing = GridSpacing.spacing4) {
                            umItem(GridSize.cells12, GridSize.cells4) {
                                umTextFieldSelect(
                                    "${fileTypeLabel.text}",
                                    entity?.assignment?.caFileType.toString(),
                                    fileTypeLabel.errorText ?: "",
                                    error = fileTypeLabel.error,
                                    values = fileTypeOptions?.map {
                                        Pair(it.code.toString(), it.toString())
                                    }?.toList(),
                                    onChange = {
                                        setState {
                                            entity?.assignment?.caFileType = it.toInt()
                                        }
                                    }
                                )
                            }

                            umItem(GridSize.cells12, GridSize.cells4) {
                                umTextField(label = "${fileSizeLimitLabel.text}",
                                    helperText = fileSizeLimitLabel.errorText,
                                    value = entity?.assignment?.caSizeLimit.toString(),
                                    error = fileSizeLimitLabel.error,
                                    disabled = !fieldsEnabled,
                                    variant = FormControlVariant.outlined,
                                    onChange = {
                                        setState {
                                            entity?.assignment?.caSizeLimit = it.toInt()
                                        }
                                    }
                                )
                            }

                            umItem(GridSize.cells12, GridSize.cells4) {
                                umTextField(label = "${fileNumberLimitLabel.text}",
                                    helperText = fileNumberLimitLabel.errorText,
                                    value = entity?.assignment?.caNumberOfFiles.toString(),
                                    error = fileNumberLimitLabel.error,
                                    disabled = !fieldsEnabled,
                                    variant = FormControlVariant.outlined,
                                    onChange = {
                                        setState {
                                            entity?.assignment?.caNumberOfFiles =
                                                it.toIntOrNull() ?: 0
                                        }
                                    }
                                )
                            }
                        }
                    }

                    umItem {
                        css(defaultMarginTop)
                        renderListItemWithTitleAndSwitch(getString(MessageID.require_text_submission), entity?.assignment?.caRequireTextSubmission ?: false){
                            setState {
                                entity?.assignment?.caRequireTextSubmission = !(entity?.assignment?.caRequireTextSubmission ?: false)
                                requireTextSubmission = entity?.assignment?.caRequireTextSubmission ?: false
                            }
                        }
                    }

                    if(requireTextSubmission) {
                        umGridContainer(columnSpacing = GridSpacing.spacing4) {
                            umItem(GridSize.cells12, GridSize.cells6) {
                                umTextFieldSelect(
                                    "${textLimitTypeLabel.text}",
                                    entity?.assignment?.caTextLimitType.toString(),
                                    textLimitTypeLabel.errorText ?: "",
                                    error = textLimitTypeLabel.error,
                                    values = textLimitTypeOptions?.map {
                                        Pair(it.code.toString(), it.toString())
                                    }?.toList(),
                                    onChange = {
                                        setState {
                                            entity?.assignment?.caTextLimitType = it.toInt()
                                        }
                                    }
                                )
                            }

                            umItem(GridSize.cells12, GridSize.cells6) {
                                umTextField(label = "${textLimitMaxLabel.text}",
                                    helperText = textLimitMaxLabel.errorText,
                                    value = entity?.assignment?.caTextLimit.toString(),
                                    error = textLimitMaxLabel.error,
                                    disabled = !fieldsEnabled,
                                    variant = FormControlVariant.outlined,
                                    onChange = {
                                        setState {
                                            entity?.assignment?.caTextLimit = it.toIntOrNull() ?: 0
                                        }
                                    }
                                )
                            }
                        }
                    }


                    umGridContainer(columnSpacing = GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextFieldSelect(
                                "${submissionPolicyLabel.text}",
                                entity?.assignment?.caSubmissionPolicy.toString(),
                                submissionPolicyLabel.errorText ?: "",
                                error = submissionPolicyLabel.error,
                                values = submissionPolicyOptions?.map {
                                    Pair(it.code.toString(), it.toString())
                                }?.toList(),
                                onChange = {
                                    setState {
                                        entity?.assignment?.caSubmissionPolicy = it.toInt()
                                    }
                                }
                            )
                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextFieldSelect(
                                "${markedByLabel.text}",
                                entity?.assignment?.caMarkingType.toString(),
                                markedByLabel.errorText ?: "",
                                error = markedByLabel.error,
                                values = markingTypeOptions?.map {
                                    Pair(it.optionId.toString(), it.toString())
                                }?.toList(),
                                onChange = {
                                    setState {
                                        entity?.assignment?.caMarkingType = it.toInt()
                                    }
                                }
                            )
                        }
                    }

                    umItem {
                        css(defaultMarginTop)
                        renderListItemWithTitleAndSwitch(getString(MessageID.allow_class_comments), entity?.assignment?.caClassCommentEnabled ?: false){
                            setState {
                                entity?.assignment?.caClassCommentEnabled = !(entity?.assignment?.caClassCommentEnabled ?: false)
                            }
                        }
                    }

                    umItem {
                        renderListItemWithTitleAndSwitch(getString(MessageID.allow_private_comments_from_students),
                            entity?.assignment?.caPrivateCommentsEnabled ?: false){
                            setState {
                                entity?.assignment?.caPrivateCommentsEnabled = !(entity?.assignment?.caPrivateCommentsEnabled ?: false)
                            }
                        }
                    }
                }

            }
        }
    }
}