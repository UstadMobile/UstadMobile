package com.ustadmobile.view

import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.ClazzLogEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzLogEditView
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.startOfDay
import com.ustadmobile.util.ext.timeInMillsFromStartOfDay
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import react.RBuilder
import react.setState
import styled.css
import kotlin.js.Date

class ClazzLogEditComponent (mProps: UmProps): UstadEditComponent<ClazzLog>(mProps),
    ClazzLogEditView {

    private var mPresenter: ClazzLogEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzLog>?
        get() = mPresenter

    override val viewName: String
        get() = ClazzLogEditView.VIEW_NAME

    private var dateLabel = FieldLabel(text = getString(MessageID.date))

    private var timeLabel = FieldLabel(text = getString(MessageID.time))


    override var date: Long = 0L
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var time: Long = 0L
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var dateError: String? = null
        get() = field
        set(value) {
           setState {
               field = value
               dateLabel = dateLabel.copy(errorText = value)
           }
        }

    override var timeZone: String? = null
        get() = field
        set(value) {
           setState {
               field = value
           }
        }

    override var timeError: String? = null
        get() = field
        set(value) {
            setState {
                field = value
                timeLabel = timeLabel.copy(errorText = value)
            }
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    var dateFieldValue: Long = Date().getTime().toLong()

    var timeFieldValue: Long = Date().getTime().toLong()

    override var entity: ClazzLog? = null
        get() = field
        set(value) {
            field = value
        }

    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.add_a_new_occurrence)
        mPresenter = ClazzLogEditPresenter(this, arguments, this,
            this, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        umGridContainer(GridSpacing.spacing4) {
            css{
                +contentContainer
                +defaultPaddingTop
            }
            umItem(GridSize.cells12, GridSize.cells6 ) {
                umDatePicker(
                    label = "${dateLabel.text}",
                    error = dateLabel.error,
                    helperText = dateLabel.errorText,
                    value = dateFieldValue.toDate(),
                    inputVariant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            date = it.startOfDay(timeZone).getTime().toLong()
                            dateFieldValue = it.getTime().toLong()
                            dateError = null
                        }
                    })
            }

            umItem(GridSize.cells12, GridSize.cells6 ) {

                umTimePicker(
                    label = "${timeLabel.text}",
                    error = timeLabel.error,
                    helperText = timeLabel.errorText,
                    value = timeFieldValue.toDate(),
                    inputVariant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            time = it.timeInMillsFromStartOfDay(timeZone)
                            timeFieldValue = it.getTime().toLong()
                            timeError = null
                        }
                    })
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