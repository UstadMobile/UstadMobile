package com.ustadmobile.view

import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.fieldsOnlyFormScreen
import com.ustadmobile.util.UmProps
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

    private var contentList: List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> = listOf()

    private var titleLabel = FieldLabel(text = getString(MessageID.title))

    private var instructionLabel = FieldLabel(text = optional(MessageID.instructions_for_students))

    private var startDateLabel = FieldLabel(text = getString(MessageID.start_date))

    private var deadlineDateLabel = FieldLabel(text = optional(MessageID.deadline))

    private var startTimeLabel = FieldLabel(text = getString(MessageID.time))

    private var deadlineTimeLabel = FieldLabel(text = getString(MessageID.time))

    private var endOgraceTimeLabel = FieldLabel(text = getString(MessageID.time))

    private var lateSubLabel = FieldLabel(text = getString(MessageID.late_submission))

    private var penaltyLabel = FieldLabel(text = getString(MessageID.late_submission_penalty), id = "penalty")

    private var endOfGraceLabel = FieldLabel(text = getString(MessageID.end_of_grace_period))

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
                startDateLabel = startDateLabel.copy(errorText = value)
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

    override var lateSubmissionOptions: List<ClazzAssignmentEditPresenter.LateSubmissionOptionsMessageIdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    private val contentListObserver = ObserverFnWrapper<List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>> {
        setState {
            contentList = it
        }
    }

    override var clazzAssignmentContent: DoorMutableLiveData<List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>>? = null
        get() = field
        set(value) {
            field?.removeObserver(contentListObserver)
            field = value
            value?.observe(this, contentListObserver)
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
                                label = "${startDateLabel.text}",
                                error = startDateLabel.error,
                                helperText = startDateLabel.errorText,
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
                        createListSectionTitle(timeZone ?: "", TypographyVariant.h6)
                    }

                    umTextFieldSelect(
                        "${lateSubLabel.text}",
                        entity?.caLateSubmissionType.toString(),
                        lateSubLabel.errorText ?: "",
                        error = lateSubLabel.error,
                        values = lateSubmissionOptions?.map {
                            Pair(it.code.toString(), it.toString())
                        }?.toList(),
                        onChange = {
                            setState {
                                entity?.caLateSubmissionType = it.toInt()
                            }
                        }
                    )

                    if(entity?.caLateSubmissionType == ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_PENALTY ||
                        entity?.caLateSubmissionType == ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_ACCEPT){

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
                    }


                    if(entity?.caLateSubmissionType == ClazzAssignment.ASSIGNMENT_LATE_SUBMISSION_PENALTY){
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
                    }

                    umSpacer()

                    createListSectionTitle(getString(MessageID.content), TypographyVariant.h6)

                    val createNewItem = CreateNewItem(true, MessageID.add_content){
                        mPresenter?.contentOneToManyJoinListener?.onClickNew()
                    }

                    mPresenter?.let { presenter ->
                        renderContentEntries(presenter.contentOneToManyJoinListener,
                            contentList.toSet().toList(), createNewItem = createNewItem){
                            mPresenter?.contentOneToManyJoinListener?.onClickEdit(it)
                        }
                    }

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