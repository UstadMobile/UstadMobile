package com.ustadmobile.view

import com.ustadmobile.util.FieldLabel
import com.ustadmobile.core.controller.HolidayEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.HolidayEditView
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.padding
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class HolidayEditComponent(mProps: UmProps): UstadEditComponent<Holiday>(mProps), HolidayEditView {

    private var mPresenter: HolidayEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Holiday>?
        get() = mPresenter

    private val holidayName = FieldLabel(getString(MessageID.name))

    private val holidayStart = FieldLabel(getString(MessageID.start_date))

    private val holidayEnd = FieldLabel(getString(MessageID.end_date))

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: Holiday? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var loading: Boolean = false

    override fun onCreateView() {
        super.onCreateView()
        setEditTitle(MessageID.add_a_holiday, MessageID.edit_holiday)
        mPresenter = HolidayEditPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +contentContainer
                +defaultPaddingTop
            }

            umGridContainer {
                css{
                    padding(2.spacingUnits, 2.spacingUnits,0.spacingUnits)
                }
                umItem(GridSize.cells12) {
                    umTextField(
                        label = "${holidayName.text}",
                        helperText = holidayName.errorText,
                        value = entity?.holName, error = holidayName.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.holName = it
                            }
                        }) {}
                }

                umGridContainer(GridSpacing.spacing4) {

                    umItem(GridSize.cells6) {

                        umDatePicker(
                            label = "${holidayStart.text}",
                            error = holidayStart.error,
                            helperText = holidayStart.errorText,
                            value = entity?.holStartTime.toDate(),
                            inputVariant = FormControlVariant.outlined,
                            onChange = {
                                setState {
                                    entity?.holStartTime = it.getTime().toLong()
                                }
                            })
                    }

                    umItem(GridSize.cells6) {

                        umDatePicker(
                            label = "${holidayEnd.text}",
                            error = holidayEnd.error,
                            helperText = holidayEnd.errorText,
                            value = entity?.holEndTime.toDate(),
                            inputVariant = FormControlVariant.outlined,
                            onChange = {
                                setState {
                                    entity?.holEndTime = it.getTime().toLong()
                                }
                            })
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