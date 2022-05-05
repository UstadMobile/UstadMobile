package com.ustadmobile.view

import com.ustadmobile.util.FieldLabel
import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ScheduleEditView
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ScheduleEditComponent (mProps: UmProps): UstadEditComponent<Schedule>(mProps),
    ScheduleEditView {

    private var mPresenter: ScheduleEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Schedule>?
        get() = mPresenter

    private val okText = getString(MessageID.ok)

    private val cancelText = getString(MessageID.cancel)

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
                fromTimeLabel = fromTimeLabel.copy(errorText = value)
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
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +StyleManager.fieldsOnlyFormScreen
            }

            umGridContainer(GridSpacing.spacing4) {
                umItem(GridSize.cells12){

                    umTextFieldSelect("${daysOptionLabel.text}",
                        entity?.scheduleDay.toString(),
                        daysOptionLabel.errorText ?: "",
                        error = daysOptionLabel.error,
                        values = dayOptions?.map {
                            Pair(it.code.toString(), it.toString())
                        }?.toList(),
                        onChange = {
                            setState {
                                entity?.scheduleDay = it.toInt()
                            }
                        })
                }

                umItem(GridSize.cells12){
                    umGridContainer(GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6 ) {

                            umTimePicker(
                                label = "${fromTimeLabel.text}",
                                error = fromTimeLabel.error,
                                okText = okText,
                                cancelText = cancelText,
                                helperText = fromTimeLabel.errorText,
                                value = entity?.sceduleStartTime.toDate(),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.sceduleStartTime = it.getTime().toLong()
                                        fromTimeError = null
                                    }
                                })
                        }

                        umItem(GridSize.cells12, GridSize.cells6 ) {
                            umTimePicker(
                                label = "${toTimeLabel.text}",
                                error = toTimeLabel.error,
                                okText = okText,
                                cancelText = cancelText,
                                helperText = toTimeLabel.errorText,
                                value = entity?.scheduleEndTime.toDate(),
                                inputVariant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.scheduleEndTime = it.getTime().toLong()
                                        toTimeError = null
                                    }
                                })
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