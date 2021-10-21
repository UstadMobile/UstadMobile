package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ccfraser.muirwik.components.form.mFormControl
import com.ccfraser.muirwik.components.form.mFormHelperText
import com.ccfraser.muirwik.components.input.mInputLabel
import com.ccfraser.muirwik.components.input.mOutlinedInput
import com.ccfraser.muirwik.components.menu.mMenuItem
import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ScheduleEditView
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.ext.standardFormat
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

class ScheduleEditComponent (mProps: RProps): UstadEditComponent<Schedule>(mProps),
    ScheduleEditView {

    private var mPresenter: ScheduleEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Schedule>?
        get() = mPresenter

    override val viewName: String
        get() = ScheduleEditView.VIEW_NAME

    private var fromTimeLabel = FieldLabel(text = getString(MessageID.from))

    private var toTimeLabel = FieldLabel(text = getString(MessageID.to))

    private var daysOptionLabel = FieldLabel(text = getString(MessageID.day))

    override var dayOptions: List<ScheduleEditPresenter.DayMessageIdOption>? = null
        get() = field
        set(value) {
            field = value
        }


    override var fromTimeError: String? = null
        set(value) {
            setState {
                fromTimeLabel = toTimeLabel.copy(errorText = value)
            }
        }

    override var toTimeError: String? = null
        set(value) {
            setState {
                toTimeLabel = toTimeLabel.copy(errorText = value)
            }
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: Schedule? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        mPresenter = ScheduleEditPresenter(this, arguments, this,
            di, this)
        setEditTitle(MessageID.add_a_schedule, MessageID.edit_schedule)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {

        styledDiv {
            css {
                +StyleManager.fieldsOnlyFormScreen
            }

            umGridContainer(MGridSpacing.spacing4) {
                umItem(MGridSize.cells12){
                    mFormControl(variant = MFormControlVariant.outlined) {
                        css(defaultFullWidth)
                        mInputLabel("${daysOptionLabel.text}",
                            htmlFor = "days",
                            variant = MFormControlVariant.outlined) {
                            css(alignTextToStart)
                        }
                        mSelect("${entity?.scheduleDay ?: 0}",
                            native = false,
                            input = mOutlinedInput(name = "days",
                                id = "days", addAsChild = false,
                                labelWidth = daysOptionLabel.width),
                            onChange = { it, _ ->
                                setState {
                                    entity?.scheduleDay = it.targetValue.toString().toInt()
                                }
                            }) {
                            dayOptions?.forEach {
                                mMenuItem(primaryText = it.toString(), value = it.optionId.toString()){
                                    css(alignTextToStart)
                                }
                            }
                        }

                        daysOptionLabel.errorText?.let { error ->
                            mFormHelperText(error){
                                css(StyleManager.errorTextClass)
                            }
                        }
                    }
                }

                umItem(MGridSize.cells12){
                    umGridContainer(MGridSpacing.spacing4) {
                        umItem(MGridSize.cells12, MGridSize.cells6 ) {
                            mTextField(label = "${fromTimeLabel.text}",
                                value = Date(entity?.sceduleStartTime ?: 0).standardFormat(),
                                error = fromTimeLabel.error,
                                disabled = !fieldsEnabled,
                                helperText = fromTimeLabel.errorText,
                                variant = MFormControlVariant.outlined,
                                onChange = {
                                    it.persist()
                                    setState {
                                        entity?.sceduleStartTime = it.targetInputValue.toLong()
                                        fromTimeError = null
                                    }
                                }){
                                css(defaultFullWidth)
                            }
                        }

                        umItem(MGridSize.cells12, MGridSize.cells6 ) {
                            mTextField(label = "${toTimeLabel.text}",
                                value = Date(entity?.scheduleEndTime ?: 0).standardFormat(),
                                error = toTimeLabel.error,
                                disabled = !fieldsEnabled,
                                helperText = toTimeLabel.errorText,
                                variant = MFormControlVariant.outlined,
                                onChange = {
                                    it.persist()
                                    setState {
                                        entity?.scheduleEndTime = it.targetInputValue.toLong()
                                        toTimeError = null
                                    }
                                }){
                                css(defaultFullWidth)
                            }
                        }
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
        toTimeError = null
        fromTimeError = null
    }

}