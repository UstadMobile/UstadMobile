package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.HolidayEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.HolidayEditView
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.padding
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv

class HolidayEditComponent(mProps: RProps): UstadEditComponent<Holiday>(mProps), HolidayEditView {

    private var mPresenter: HolidayEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Holiday>?
        get() = mPresenter

    private val holidayName = FieldLabel(getString(MessageID.name))

    private val holidayStart = FieldLabel(getString(MessageID.start_date))

    private val holidayEnd = FieldLabel(getString(MessageID.end_date))

    override val viewName: String
        get() = HolidayEditView.VIEW_NAME

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
                umItem(MGridSize.cells12) {
                    mTextField(label = "${holidayName.text}",
                        helperText = holidayName.errorText,
                        value = entity?.holName, error = holidayName.error,
                        disabled = !fieldsEnabled,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.holName = it.targetInputValue
                            }
                        }) {
                        css(defaultFullWidth)
                    }
                }

                umGridContainer(MGridSpacing.spacing4) {

                    umItem(MGridSize.cells6) {
                        mTextField(label = "${holidayStart.text}",
                            helperText = holidayStart.errorText,
                            value = entity?.holStartTime.toString(), error = holidayStart.error,
                            disabled = !fieldsEnabled,
                            variant = MFormControlVariant.outlined,
                            onChange = {
                                it.persist()
                                setState {
                                    //entity?.holStartTime = it.targetInputValue
                                }
                            }) {
                            css(defaultFullWidth)
                        }
                    }

                    umItem(MGridSize.cells6) {
                        mTextField(label = "${holidayEnd.text}",
                            helperText = holidayEnd.errorText,
                            value = entity?.holStartTime.toString(), error = holidayEnd.error,
                            disabled = !fieldsEnabled,
                            variant = MFormControlVariant.outlined,
                            onChange = {
                                it.persist()
                                setState {
                                    //entity?.holStartTime = it.targetInputValue
                                }
                            }) {
                            css(defaultFullWidth)
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
    }
}