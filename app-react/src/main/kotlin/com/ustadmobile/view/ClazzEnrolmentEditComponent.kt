package com.ustadmobile.view

import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.ClazzEnrolmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.view.ClazzEnrolmentEditView
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.standardFormat
import react.RBuilder
import com.ustadmobile.util.*
import react.setState
import kotlin.js.Date

class ClazzEnrolmentEditComponent (mProps: UmProps): UstadEditComponent<ClazzEnrolmentWithLeavingReason>(mProps),
    ClazzEnrolmentEditView {

    private var mPresenter: ClazzEnrolmentEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzEnrolmentWithLeavingReason>?
        get() = mPresenter

    override val viewName: String
        get() = ClazzEnrolmentEditView.VIEW_NAME

    private var startDateLabel = FieldLabel(text = getString(MessageID.start_date))

    private var endDateLabel = FieldLabel(text = getString(MessageID.end_date))

    private var roleLabel = FieldLabel(text = getString(MessageID.role))

    private var outcomeLabel = FieldLabel(text = getString(MessageID.outcome))

    private var leavingLabel = FieldLabel(text = getString(MessageID.leaving_reason))


    override var roleList: List<IdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var statusList: List<IdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var startDateErrorWithDate: Pair<String, Long>? = null
        get() = field
        set(value) {
            field = value
            val startDateValue: String? = if(value?.first?.contains("%1\$s") == true){
                value.first.replace("%1\$s", Date(value.second).standardFormat())
            }else{
                value?.first
            }
            startDateLabel = startDateLabel.copy(errorText = startDateValue)
        }

    override var endDateError: String? = null
        set(value) {
            setState {
                endDateLabel = endDateLabel.copy(errorText = value)
            }
        }

    override var roleSelectionError: String? = null
        set(value) {
            setState {
                roleLabel = roleLabel.copy(errorText = value)
            }
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: ClazzEnrolmentWithLeavingReason? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        mPresenter = ClazzEnrolmentEditPresenter(this, arguments, this,
            this, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {

        /*styledDiv {
            css {
                +StyleManager.fieldsOnlyFormScreen
            }

            umGridContainer(MGridSpacing.spacing4) {

                umItem(MGridSize.cells12){

                    umGridContainer(MGridSpacing.spacing4) {

                        umItem(MGridSize.cells12, MGridSize.cells6 ){
                            mFormControl(variant = MFormControlVariant.outlined) {
                                css(defaultFullWidth)
                                mInputLabel("${roleLabel.text}",
                                    htmlFor = "role",
                                    variant = MFormControlVariant.outlined) {
                                    css(alignTextToStart)
                                }
                                mSelect("${entity?.clazzEnrolmentRole ?: 0}",
                                    native = false,
                                    input = mOutlinedInput(name = "role",
                                        id = "role", addAsChild = false,
                                        labelWidth = roleLabel.width),
                                    onChange = { it, _ ->
                                        setState {
                                            entity?.clazzEnrolmentRole = it.targetValue.toString().toInt()
                                        }
                                    }) {
                                    roleList?.forEach {
                                        mMenuItem(primaryText = it.toString(), value = it.optionId.toString()){
                                            css(alignTextToStart)
                                        }
                                    }
                                }

                                roleLabel.errorText?.let { error ->
                                    mFormHelperText(error){
                                        css(StyleManager.errorTextClass)
                                    }
                                }
                            }
                        }

                        umItem(MGridSize.cells12, MGridSize.cells6 ){
                            mFormControl(variant = MFormControlVariant.outlined) {
                                css(defaultFullWidth)
                                mInputLabel("${outcomeLabel.text}",
                                    htmlFor = "outcome",
                                    variant = MFormControlVariant.outlined) {
                                    css(alignTextToStart)
                                }
                                mSelect("${entity?.clazzEnrolmentOutcome ?: 0}",
                                    native = false,
                                    input = mOutlinedInput(name = "outcome",
                                        id = "outcome", addAsChild = false,
                                        labelWidth = outcomeLabel.width),
                                    onChange = { it, _ ->
                                        setState {
                                            entity?.clazzEnrolmentOutcome = it.targetValue.toString().toInt()
                                        }
                                    }) {
                                    statusList?.forEach {
                                        mMenuItem(primaryText = it.toString(), value = it.optionId.toString()){
                                            css(alignTextToStart)
                                        }
                                    }
                                }

                                outcomeLabel.errorText?.let { error ->
                                    mFormHelperText(error){
                                        css(StyleManager.errorTextClass)
                                    }
                                }
                            }
                        }

                    }


                    umGridContainer(MGridSpacing.spacing4) {

                        umItem(MGridSize.cells12, MGridSize.cells6 ) {
                            mDateTimePicker(
                                label = "${startDateLabel.text}",
                                ruleSet = defaultFullWidth,
                                error = startDateLabel.error,
                                helperText = startDateLabel.errorText,
                                value = entity?.clazzEnrolmentDateJoined.toDate(),
                                inputVariant = MFormControlVariant.outlined,
                                pickerType = MDateTimePickerType.date,
                                onChange = { mills, _ ->
                                    setState {
                                        entity?.clazzEnrolmentDateJoined = mills
                                        startDateErrorWithDate = null;
                                    }
                                })
                        }

                        umItem(MGridSize.cells12, MGridSize.cells6 ) {

                            mDateTimePicker(
                                label = "${endDateLabel.text}",
                                ruleSet = defaultFullWidth,
                                error = endDateLabel.error,
                                helperText = endDateLabel.errorText,
                                value = entity?.clazzEnrolmentDateLeft.toDate(),
                                inputVariant = MFormControlVariant.outlined,
                                pickerType = MDateTimePickerType.date,
                                onChange = { mills, utc ->
                                    setState {
                                        entity?.clazzEnrolmentDateLeft = mills
                                        endDateError = null
                                    }
                                })
                        }
                    }

                    mTextField(
                        label = "${leavingLabel.text}",
                        helperText = leavingLabel.errorText,
                        value = entity?.leavingReason?.leavingReasonTitle,
                        error = leavingLabel.error,
                        disabled = !fieldsEnabled ||
                                entity?.clazzEnrolmentOutcome == ClazzEnrolment.OUTCOME_IN_PROGRESS,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.leavingReason?.leavingReasonTitle = it.targetInputValue
                            }
                        }){
                        attrs.asDynamic().onClick = {
                            mPresenter?.handleReasonLeavingClicked()
                        }
                        css(defaultFullWidth)
                    }
                }
            }
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }

}