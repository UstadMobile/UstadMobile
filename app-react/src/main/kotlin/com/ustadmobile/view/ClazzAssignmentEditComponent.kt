package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.fieldsOnlyFormScreen
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.clean
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.*
import react.RBuilder
import react.dom.html.InputType
import react.setState
import styled.css
import styled.styledDiv

class ClazzAssignmentEditComponent(mProps: UmProps): UstadEditComponent<ClazzAssignment>(mProps),
    ClazzAssignmentEditView {

    private var mPresenter: ClazzAssignmentEditPresenter? = null

    override val viewNames: List<String>
        get() = listOf(ClazzAssignmentEditView.VIEW_NAME)

    override val mEditPresenter: UstadEditPresenter<*, ClazzAssignment>?
        get() = mPresenter

    private var titleLabel = FieldLabel(text = getString(MessageID.title))

    private var instructionLabel = FieldLabel(text = getStringWithOptionalLabel(MessageID.description))

    private var doNotShowBeforeLabel = FieldLabel(text = getString(MessageID.dont_show_before).clean())

    private var deadlineDateLabel = FieldLabel(text = getStringWithOptionalLabel(MessageID.deadline))

    private var startTimeLabel = FieldLabel(text = getString(MessageID.time))

    private var deadlineTimeLabel = FieldLabel(text = getString(MessageID.time))

    private var endOgraceTimeLabel = FieldLabel(text = getString(MessageID.time))

    private var completionCriteriaLabel = FieldLabel(text = getString(MessageID.completion_criteria))

    private var submissionTypeLabel = FieldLabel(text = getString(MessageID.submission_type))

    private var fileTypeLabel = FieldLabel(text = getString(MessageID.file_type))

    private var maxPointsLabel = FieldLabel(text = getString(MessageID.maximum_points))

    private var penaltyLabel = FieldLabel(text = getString(MessageID.late_submission_penalty), id = "penalty")

    private var endOfGraceLabel = FieldLabel(text = getString(MessageID.end_of_grace_period))

    private var fileSizeLimitLabel = FieldLabel(text = getString(MessageID.size_limit))

    private var fileNumberLimitLabel = FieldLabel(text = getString(MessageID.max_number_of_files))

    override var caGracePeriodError: String? = null
        get() = field
        set(value) {
            field = value
            setState {
                endOfGraceLabel = endOfGraceLabel.copy(errorText = value)
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
                titleLabel = titleLabel.copy(errorText = value)
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
    override var editAfterSubmissionOptions: List<ClazzAssignmentEditPresenter.EditAfterSubmissionOptionsMessageIdOption>? = null
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

    override var submissionTypeOptions: List<ClazzAssignmentEditPresenter.SubmissionTypeOptionsMessageIdOption>?= null
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

    override var markingTypeOptions: List<ClazzAssignmentEditPresenter.MarkingTypeOptionsMessageIdOption>?= null
        get() = field
        set(value) {
            setState {
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

    override var entity: ClazzAssignment? = null
        get() = field
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

                    umTextField(label = "${titleLabel.text}",
                        helperText = titleLabel.errorText,
                        value = entity?.caTitle,
                        error = titleLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.caTitle = it
                                caTitleError = null
                            }
                        })

                    umGridContainer(columnSpacing = GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {

                            umDatePicker(
                                label = "${doNotShowBeforeLabel.text}",
                                error = doNotShowBeforeLabel.error,
                                helperText = doNotShowBeforeLabel.errorText,
                                value = entity?.caStartDate.toDate(true),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.caStartDate = it.getTime().toLong()
                                        caStartDateError = null
                                    }
                                })

                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTimePicker(
                                label = "${startTimeLabel.text}",
                                error = startTimeLabel.error,
                                helperText = startTimeLabel.errorText,
                                value = entity?.caStartDate.toDate(true),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.caStartDate =  it.getTime().toLong()
                                    }
                                })
                        }
                    }

                    umTextField(label = "${instructionLabel.text}",
                        helperText = instructionLabel.errorText,
                        value = entity?.caDescription,
                        error = instructionLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.caDescription = it
                            }
                        })

                    umGridContainer(columnSpacing = GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {

                            umTextFieldSelect(
                                "${completionCriteriaLabel.text}",
                                entity?.caCompletionCriteria.toString(),
                                completionCriteriaLabel.errorText ?: "",
                                error = completionCriteriaLabel.error,
                                values = completionCriteriaOptions?.map {
                                    Pair(it.code.toString(), it.toString())
                                }?.toList(),
                                onChange = {
                                    setState {
                                        entity?.caCompletionCriteria = it.toInt()
                                        completionCriteriaOptions = null
                                    }
                                }
                            )

                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextField(label = "${maxPointsLabel.text}",
                                helperText = maxPointsLabel.errorText,
                                value = entity?.caMaxPoints.toString(),
                                error = maxPointsLabel.error,
                                disabled = !fieldsEnabled,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.caMaxPoints = it.toInt()
                                        caMaxPointsError = null
                                    }
                                })
                        }
                    }

                    umItem {
                        createListSectionTitle(timeZone ?: "", TypographyVariant.h6)
                    }

                    umGridContainer(spacing = GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umDatePicker(
                                label = "${endOfGraceLabel.text}",
                                error = endOfGraceLabel.error,
                                helperText = endOfGraceLabel.errorText,
                                value = entity?.caGracePeriodDate.toDate(true),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.caGracePeriodDate = it.getTime().toLong()
                                        caGracePeriodError = null
                                    }
                                })

                        }
                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTimePicker(
                                label = "${endOgraceTimeLabel.text}",
                                error = endOgraceTimeLabel.error,
                                helperText = endOgraceTimeLabel.errorText,
                                value = entity?.caGracePeriodDate.toDate(true),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.caGracePeriodDate = entity?.caGracePeriodDate ?: 0 + it.getTime().toLong()
                                    }
                                })
                        }
                    }


                    umGridContainer(columnSpacing = GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {

                            umDatePicker(
                                label = "${deadlineDateLabel.text}",
                                error = deadlineDateLabel.error,
                                helperText = deadlineDateLabel.errorText,
                                value = entity?.caDeadlineDate.toDate(true),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.caDeadlineDate = it.getTime().toLong()
                                        caDeadlineError = null
                                    }
                                })

                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTimePicker(
                                label = "${deadlineTimeLabel.text}",
                                error = deadlineTimeLabel.error,
                                helperText = deadlineTimeLabel.errorText,
                                value = entity?.caDeadlineDate.toDate(true),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.caDeadlineDate =  it.getTime().toLong()
                                    }
                                })
                        }
                    }

                    umItem {
                        css(defaultMarginTop)
                        createListItemWithTitleAndSwitch(getString(MessageID.require_file_submission), entity?.caRequireFileSubmission ?: false){
                            setState {
                                entity?.caRequireFileSubmission = !(entity?.caRequireFileSubmission ?: false)
                            }
                        }
                    }

                    umGridContainer(columnSpacing = GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {

                            umTextFieldSelect(
                                "${submissionTypeLabel.text}",
                                entity?.caSubmissionType.toString(),
                                submissionTypeLabel.errorText ?: "",
                                error = submissionTypeLabel.error,
                                values = submissionTypeOptions?.map {
                                    Pair(it.code.toString(), it.toString())
                                }?.toList(),
                                onChange = {
                                    setState {
                                        entity?.caSubmissionType = it.toInt()
                                    }
                                }
                            )

                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextFieldSelect(
                                "${fileTypeLabel.text}",
                                entity?.caFileType.toString(),
                                fileTypeLabel.errorText ?: "",
                                error = fileTypeLabel.error,
                                values = fileTypeOptions?.map {
                                    Pair(it.code.toString(), it.toString())
                                }?.toList(),
                                onChange = {
                                    setState {
                                        entity?.caFileType = it.toInt()
                                    }
                                }
                            )
                        }
                    }

                    umGridContainer(columnSpacing = GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextField(label = "${fileSizeLimitLabel.text}",
                                helperText = fileSizeLimitLabel.errorText,
                                value = entity?.caSizeLimit.toString(),
                                error = fileSizeLimitLabel.error,
                                disabled = !fieldsEnabled,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.caSizeLimit = it.toInt()
                                    }
                                })
                        }
                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextField(label = "${fileNumberLimitLabel.text}",
                                helperText = fileNumberLimitLabel.errorText,
                                value = entity?.caNumberOfFiles.toString(),
                                error = fileNumberLimitLabel.error,
                                disabled = !fieldsEnabled,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.caNumberOfFiles = it.toInt()
                                    }
                                })
                        }
                    }

                    umItem {
                        css(defaultMarginTop)
                        createListItemWithTitleAndSwitch(getString(MessageID.require_text_submission), entity?.caRequireTextSubmission ?: false){
                            setState {
                                entity?.caRequireTextSubmission = !(entity?.caRequireTextSubmission ?: false)
                            }
                        }
                    }


                    // TODO need to change
                        umFormControl(variant = FormControlVariant.outlined) {
                            css{
                                +defaultMarginTop
                            }
                            umInputLabel("${penaltyLabel.text}",
                                id = penaltyLabel.id,
                                error = penaltyLabel.error,
                                variant = FormControlVariant.outlined,
                                htmlFor = penaltyLabel.id)

                        umOutlinedInput(
                            id = penaltyLabel.id,
                            label = "${penaltyLabel.text}",
                            value = entity?.caLateSubmissionPenalty.toString(),
                            error = penaltyLabel.error,
                            type = InputType.number,
                            disabled = !fieldsEnabled,
                            onChange = {
                                setState {
                                    entity?.caLateSubmissionPenalty = it.toInt()
                                }
                            }) {
                            attrs.endAdornment = umTypography("%")
                        }
                        }

                    umSpacer()

                    createListSectionTitle(getString(MessageID.content), TypographyVariant.h6)

                    umItem {
                        css(defaultMarginTop)
                        createListItemWithTitleAndSwitch(getString(MessageID.allow_class_comments), entity?.caClassCommentEnabled ?: false){
                            setState {
                                entity?.caClassCommentEnabled = !(entity?.caClassCommentEnabled ?: false)
                            }
                        }
                    }

                    umItem {
                        createListItemWithTitleAndSwitch(getString(MessageID.allow_private_comments_from_students),
                            entity?.caPrivateCommentsEnabled ?: false){
                            setState {
                                entity?.caPrivateCommentsEnabled = !(entity?.caPrivateCommentsEnabled ?: false)
                            }
                        }
                    }
                }

            }
        }
    }
}