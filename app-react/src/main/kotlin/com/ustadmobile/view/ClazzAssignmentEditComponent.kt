package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.FieldLabel
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

class ClazzAssignmentEditComponent(mProps: UmProps): UstadEditComponent<ClazzAssignmentWithCourseBlock>(mProps),
    ClazzAssignmentEditView {

    private var mPresenter: ClazzAssignmentEditPresenter? = null

    override val viewNames: List<String>
        get() = listOf(ClazzAssignmentEditView.VIEW_NAME)

    override val mEditPresenter: UstadEditPresenter<*, ClazzAssignmentWithCourseBlock>?
        get() = mPresenter

    private var contentList: List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> = listOf()

    private var titleLabel = FieldLabel(text = getString(MessageID.title))

    private var instructionLabel = FieldLabel(text = getStringWithOptionalLabel(MessageID.instructions_for_students))

    private var startDateLabel = FieldLabel(text = getString(MessageID.start_date))

    private var deadlineDateLabel = FieldLabel(text = getStringWithOptionalLabel(MessageID.deadline))

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
    override var caMaxPointsError: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var startDate: Long
        get() = TODO("Not yet implemented")
        set(value) {}
    override var startTime: Long
        get() = TODO("Not yet implemented")
        set(value) {}
    override var deadlineDate: Long
        get() = TODO("Not yet implemented")
        set(value) {}
    override var deadlineTime: Long
        get() = TODO("Not yet implemented")
        set(value) {}
    override var gracePeriodDate: Long
        get() = TODO("Not yet implemented")
        set(value) {}
    override var gracePeriodTime: Long
        get() = TODO("Not yet implemented")
        set(value) {}

    override var timeZone: String? = null
        get() = field
        set(value) {
            val newText = getString(MessageID.class_timezone) + " " + value
            setState {
                field = newText
            }
        }
    override var editAfterSubmissionOptions: List<ClazzAssignmentEditPresenter.EditAfterSubmissionOptionsMessageIdOption>?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var fileTypeOptions: List<ClazzAssignmentEditPresenter.FileTypeOptionsMessageIdOption>?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var textLimitTypeOptions: List<ClazzAssignmentEditPresenter.TextLimitTypeOptionsMessageIdOption>?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var submissionTypeOptions: List<ClazzAssignmentEditPresenter.SubmissionTypeOptionsMessageIdOption>?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var completionCriteriaOptions: List<ClazzAssignmentEditPresenter.CompletionCriteriaOptionsMessageIdOption>?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var markingTypeOptions: List<ClazzAssignmentEditPresenter.MarkingTypeOptionsMessageIdOption>?
        get() = TODO("Not yet implemented")
        set(value) {}

    private val contentListObserver = ObserverFnWrapper<List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>> {
        setState {
            contentList = it
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
                                value = entity?.block?.cbHideUntilDate.toDate(true),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.block?.cbHideUntilDate = it.getTime().toLong()
                                        caStartDateError = null
                                    }
                                })

                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTimePicker(
                                label = "${startTimeLabel.text}",
                                error = startTimeLabel.error,
                                helperText = startTimeLabel.errorText,
                                value = entity?.block?.cbHideUntilDate.toDate(true),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.block?.cbHideUntilDate =  it.getTime().toLong()
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
                                value = entity?.block?.cbDeadlineDate.toDate(true),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.block?.cbDeadlineDate = it.getTime().toLong()
                                        caDeadlineError = null
                                    }
                                })

                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTimePicker(
                                label = "${deadlineTimeLabel.text}",
                                error = deadlineTimeLabel.error,
                                helperText = deadlineTimeLabel.errorText,
                                value = entity?.block?.cbDeadlineDate.toDate(true),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.block?.cbDeadlineDate =  it.getTime().toLong()
                                    }
                                })
                        }
                    }

                    umItem {
                        createListSectionTitle(timeZone ?: "", TypographyVariant.h6)
                    }

                    // TODO design changed
                   /* umTextFieldSelect(
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
                    )*/

                    // TODO design changed

                            umGridContainer(spacing = GridSpacing.spacing4) {

                                umItem(GridSize.cells12, GridSize.cells6 ) {
                                    umDatePicker(
                                        label = "${endOfGraceLabel.text}",
                                        error = endOfGraceLabel.error,
                                        helperText = endOfGraceLabel.errorText,
                                        value = entity?.block?.cbGracePeriodDate.toDate(true),
                                        inputVariant = FormControlVariant.outlined,
                                        onChange = {
                                            setState {
                                                entity?.block?.cbGracePeriodDate = it.getTime().toLong()
                                                caGracePeriodError = null
                                            }
                                        })

                            }

                            umItem(GridSize.cells12, GridSize.cells6 ) {
                                umTimePicker(
                                    label = "${endOgraceTimeLabel.text}",
                                    error = endOgraceTimeLabel.error,
                                    helperText = endOgraceTimeLabel.errorText,
                                    value = entity?.block?.cbGracePeriodDate.toDate(true),
                                    inputVariant = FormControlVariant.outlined,
                                    onChange = {
                                        setState {
                                            entity?.block?.cbGracePeriodDate = entity?.block?.cbGracePeriodDate ?: 0 + it.getTime().toLong()
                                        }
                                    })
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
                            value = entity?.block?.cbLateSubmissionPenalty.toString(),
                            error = penaltyLabel.error,
                            type = InputType.number,
                            disabled = !fieldsEnabled,
                            onChange = {
                                setState {
                                    entity?.block?.cbLateSubmissionPenalty = it.toInt()
                                }
                            }) {
                            attrs.endAdornment = umTypography("%")
                        }
                        }

                    umSpacer()

                    createListSectionTitle(getString(MessageID.content), TypographyVariant.h6)

                    // TODO no more entries
                    /*
                 val createNewItem = CreateNewItem(true, MessageID.add_content){
                     mPresenter?.contentOneToManyJoinListener?.onClickNew()
                 }

              mPresenter?.let { presenter ->
                     renderContentEntries(presenter.contentOneToManyJoinListener,
                         contentList.toSet().toList(), createNewItem = createNewItem){
                         mPresenter?.contentOneToManyJoinListener?.onClickEdit(it)
                     }
                 }*/

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