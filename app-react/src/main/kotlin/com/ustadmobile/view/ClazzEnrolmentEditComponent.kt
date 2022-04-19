package com.ustadmobile.view

import com.ustadmobile.util.FieldLabel
import com.ustadmobile.core.controller.ClazzEnrolmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.view.ClazzEnrolmentEditView
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.standardFormat
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

class ClazzEnrolmentEditComponent (mProps: UmProps): UstadEditComponent<ClazzEnrolmentWithLeavingReason>(mProps),
    ClazzEnrolmentEditView {

    private var mPresenter: ClazzEnrolmentEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzEnrolmentWithLeavingReason>?
        get() = mPresenter

    private var startDateLabel = FieldLabel(text = getString(MessageID.start_date))

    private var endDateLabel = FieldLabel(text = getString(MessageID.end_date))

    private var roleLabel = FieldLabel(text = getString(MessageID.role), id = "role-label")

    private var outcomeLabel = FieldLabel(text = getString(MessageID.outcome), id = "outcome-label")

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
            setState {
                startDateLabel = startDateLabel.copy(errorText = startDateValue)
            }
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
        ustadComponentTitle = getString(MessageID.edit_enrolment)
        mPresenter = ClazzEnrolmentEditPresenter(this, arguments, this,
            this, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {

        styledDiv {
            css {
                +StyleManager.fieldsOnlyFormScreen
            }

            umGridContainer(GridSpacing.spacing4) {

                umItem(GridSize.cells12){

                    umGridContainer(GridSpacing.spacing4) {

                        umItem(GridSize.cells12, GridSize.cells6 ){

                            umTextFieldSelect("${roleLabel.text}",
                                entity?.clazzEnrolmentRole.toString(),
                                roleLabel.errorText ?:"",
                                values = roleList?.map {
                                    Pair(it.optionId.toString(), it.toString())
                                }?.toList(),
                                error = roleLabel.error,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.clazzEnrolmentRole = it.toInt()
                                    }
                                }){
                                css(alignTextToStart)
                            }
                        }

                        umItem(GridSize.cells12, GridSize.cells6 ){

                            umTextFieldSelect("${outcomeLabel.text}",
                                entity?.clazzEnrolmentOutcome.toString(),
                                outcomeLabel.errorText ?:"",
                                values = statusList?.map {
                                    Pair(it.optionId.toString(), it.toString())
                                }?.toList(),
                                error = outcomeLabel.error,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.clazzEnrolmentOutcome = it.toInt()
                                    }
                                })
                        }

                    }


                    umGridContainer(GridSpacing.spacing4) {

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umDatePicker(
                                label = "${startDateLabel.text}",
                                error = startDateLabel.error,
                                helperText = startDateLabel.errorText,
                                value = entity?.clazzEnrolmentDateJoined.toDate(),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.clazzEnrolmentDateJoined = it.getTime().toLong()
                                        startDateErrorWithDate = null
                                    }
                                })
                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {

                            umDatePicker(
                                label = "${endDateLabel.text}",
                                error = endDateLabel.error,
                                helperText = endDateLabel.errorText,
                                value = entity?.clazzEnrolmentDateLeft.toDate(),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.clazzEnrolmentDateLeft = it.getTime().toLong()
                                        endDateError = null
                                    }
                                })
                        }
                    }

                    val disabled = !fieldsEnabled ||
                            entity?.clazzEnrolmentOutcome == ClazzEnrolment.OUTCOME_IN_PROGRESS

                    umTextField(
                        label = "${leavingLabel.text}",
                        helperText = leavingLabel.errorText,
                        value = entity?.leavingReason?.leavingReasonTitle,
                        error = leavingLabel.error,
                        disabled = disabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.leavingReason?.leavingReasonTitle = it
                            }
                        }){
                        attrs.onClick = {
                           if(disabled){
                               mPresenter?.handleReasonLeavingClicked()
                           }
                        }
                        css(defaultFullWidth)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }

}