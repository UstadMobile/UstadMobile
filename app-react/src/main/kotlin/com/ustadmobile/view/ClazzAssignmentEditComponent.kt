package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.fieldsOnlyFormScreen
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.clean
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.renderListItemWithTitleAndSwitch
import com.ustadmobile.view.ext.renderListSectionTitle
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ClazzAssignmentEditComponent(mProps: UmProps): UstadEditComponent<ClazzAssignmentWithCourseBlock>(mProps),
    ClazzAssignmentEditView {

    private var mPresenter: ClazzAssignmentEditPresenter? = null

    override val viewNames: List<String>
        get() = listOf(ClazzAssignmentEditView.VIEW_NAME)

    override val mEditPresenter: UstadEditPresenter<*, ClazzAssignmentWithCourseBlock>?
        get() = mPresenter

    private var nameLabel = FieldLabel(text = getString(MessageID.title))

    private var descriptionLabel = FieldLabel(text = getStringWithOptionalLabel(MessageID.description))

    private var doNotShowBeforeLabel = FieldLabel(text = getString(MessageID.dont_show_before).clean())

    private var startTimeLabel = FieldLabel(text = getString(MessageID.time))

    private var deadlineDateLabel = FieldLabel(text = getStringWithOptionalLabel(MessageID.deadline))

    private var deadlineTimeLabel = FieldLabel(text = getString(MessageID.time))

    private var completionCriteriaLabel = FieldLabel(text = getString(MessageID.completion_criteria))

    private var submissionTypeLabel = FieldLabel(text = getString(MessageID.submission_type))

    private var fileTypeLabel = FieldLabel(text = getString(MessageID.file_type))

    private var maxPointsLabel = FieldLabel(text = getString(MessageID.maximum_points))

    private var fileSizeLimitLabel = FieldLabel(text = getString(MessageID.size_limit))

    private var fileNumberLimitLabel = FieldLabel(text = getString(MessageID.max_number_of_files).format(""))

    private var textLimitTypeLabel = FieldLabel(text = getString(MessageID.limit))

    private var textLimitMaxLabel = FieldLabel(text = getString(MessageID.maximum))

    private var editAfterSubLabel = FieldLabel(text = getString(MessageID.edit_after_submission))

    private var markedByLabel = FieldLabel(text = getString(MessageID.marked_by))

    override var caGracePeriodError: String? = null
        get() = field
        set(value) {
            field = value
            setState {

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

    override var entity: ClazzAssignmentWithCourseBlock? = null
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

                    umTextField(label = "${nameLabel.text}",
                        helperText = nameLabel.errorText,
                        value = entity?.caTitle,
                        error = nameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.caTitle = it
                                caTitleError = null
                            }
                        }
                    )

                    umTextField(label = "${descriptionLabel.text}",
                        helperText = descriptionLabel.errorText,
                        value = entity?.caDescription,
                        error = descriptionLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.caDescription = it
                            }
                        }
                    )

                    umGridContainer(columnSpacing = GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {

                            umDatePicker(
                                label = "${doNotShowBeforeLabel.text}",
                                error = doNotShowBeforeLabel.error,
                                helperText = doNotShowBeforeLabel.errorText,
                                value = startDate.toDate(true),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        startDate = it.getTime().toLong()
                                        caStartDateError = null
                                    }
                                })

                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTimePicker(
                                label = "${startTimeLabel.text}",
                                error = startTimeLabel.error,
                                helperText = startTimeLabel.errorText,
                                value = startDate.toDate(true),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        startDate = it.getTime().toLong()
                                    }
                                })
                        }
                    }

                    umItem {
                        renderListSectionTitle(timeZone ?: "", TypographyVariant.h6)
                    }

                    umGridContainer(columnSpacing = GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {

                            umTextFieldSelect(
                                "${completionCriteriaLabel.text}",
                                entity?.block?.cbCompletionCriteria.toString(),
                                completionCriteriaLabel.errorText ?: "",
                                error = completionCriteriaLabel.error,
                                values = completionCriteriaOptions?.map {
                                    Pair(it.code.toString(), it.toString())
                                }?.toList(),
                                onChange = {
                                    setState {
                                        entity?.block?.cbCompletionCriteria = it.toInt()
                                    }
                                }
                            )

                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextField(label = "${maxPointsLabel.text}",
                                helperText = maxPointsLabel.errorText,
                                value = entity?.block?.cbMaxPoints.toString(),
                                error = maxPointsLabel.error,
                                disabled = !fieldsEnabled,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.block?.cbMaxPoints = it.toInt()
                                        caMaxPointsError = null
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
                                value = deadlineDate.toDate(true),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        deadlineDate = it.getTime().toLong()
                                        caDeadlineError = null
                                    }
                                })

                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTimePicker(
                                label = "${deadlineTimeLabel.text}",
                                error = deadlineTimeLabel.error,
                                helperText = deadlineTimeLabel.errorText,
                                value = deadlineDate.toDate(true),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        deadlineDate = it.getTime().toLong()
                                    }
                                })
                        }
                    }

                    umTextField(label = "${submissionTypeLabel.text}",
                        helperText = submissionTypeLabel.errorText,
                        value = groupSet?.cgsName,
                        error = nameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onClick = {
                            mPresenter?.handleSubmissionTypeClicked()
                        }
                    )

                    umItem {
                        css(defaultMarginTop)
                        renderListItemWithTitleAndSwitch(getString(MessageID.require_file_submission), entity?.caRequireFileSubmission ?: false){
                            setState {
                                entity?.caRequireFileSubmission = !(entity?.caRequireFileSubmission ?: false)
                            }
                        }
                    }

                    umGridContainer(columnSpacing = GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells4 ) {
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

                        umItem(GridSize.cells12, GridSize.cells4 ) {
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
                                }
                            )
                        }

                        umItem(GridSize.cells12, GridSize.cells4 ) {
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
                                }
                            )
                        }
                    }

                    umItem {
                        css(defaultMarginTop)
                        renderListItemWithTitleAndSwitch(getString(MessageID.require_text_submission), entity?.caRequireTextSubmission ?: false){
                            setState {
                                entity?.caRequireTextSubmission = !(entity?.caRequireTextSubmission ?: false)
                            }
                        }
                    }

                    umGridContainer(columnSpacing = GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextFieldSelect(
                                "${textLimitTypeLabel.text}",
                                entity?.caTextLimitType.toString(),
                                textLimitTypeLabel.errorText ?: "",
                                error = textLimitTypeLabel.error,
                                values = textLimitTypeOptions?.map {
                                    Pair(it.code.toString(), it.toString())
                                }?.toList(),
                                onChange = {
                                    setState {
                                        entity?.caTextLimitType = it.toInt()
                                    }
                                }
                            )
                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextField(label = "${textLimitMaxLabel.text}",
                                helperText = textLimitMaxLabel.errorText,
                                value = entity?.caTextLimit.toString(),
                                error = textLimitMaxLabel.error,
                                disabled = !fieldsEnabled,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.caTextLimit = it.toInt()
                                    }
                                }
                            )
                        }
                    }


                    umGridContainer(columnSpacing = GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextFieldSelect(
                                "${editAfterSubLabel.text}",
                                entity?.caEditAfterSubmissionType.toString(),
                                editAfterSubLabel.errorText ?: "",
                                error = editAfterSubLabel.error,
                                values = editAfterSubmissionOptions?.map {
                                    Pair(it.code.toString(), it.toString())
                                }?.toList(),
                                onChange = {
                                    setState {
                                        entity?.caEditAfterSubmissionType = it.toInt()
                                    }
                                }
                            )
                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTextFieldSelect(
                                "${markedByLabel.text}",
                                entity?.caMarkingType.toString(),
                                markedByLabel.errorText ?: "",
                                error = markedByLabel.error,
                                values = markingTypeOptions?.map {
                                    Pair(it.code.toString(), it.toString())
                                }?.toList(),
                                onChange = {
                                    setState {
                                        entity?.caMarkingType = it.toInt()
                                    }
                                }
                            )
                        }
                    }

                    umItem {
                        css(defaultMarginTop)
                        renderListItemWithTitleAndSwitch(getString(MessageID.allow_class_comments), entity?.caClassCommentEnabled ?: false){
                            setState {
                                entity?.caClassCommentEnabled = !(entity?.caClassCommentEnabled ?: false)
                            }
                        }
                    }

                    umItem {
                        renderListItemWithTitleAndSwitch(getString(MessageID.allow_private_comments_from_students),
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