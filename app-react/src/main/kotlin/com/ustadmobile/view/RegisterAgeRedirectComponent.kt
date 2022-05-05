package com.ustadmobile.view

import com.ustadmobile.core.controller.RegisterAgeRedirectPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.RegisterAgeRedirectView
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.centerContainer
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.util.ext.DATE_FORMAT_DD_MMM_YYYY
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.LinearDimension
import kotlinx.css.height
import react.RBuilder
import react.setState
import styled.css
import kotlin.js.Date

class RegisterAgeRedirectComponent(props: UmProps): UstadBaseComponent<UmProps, UmState>(props),
    RegisterAgeRedirectView {

    private var mPresenter: RegisterAgeRedirectPresenter? = null

    private var dateLabel = FieldLabel(text = getString(MessageID.what_is_your_date_of_birth))

    override var dateOfBirth: Long = Date().getTime().toLong()
        get() = field
        set(value) {
            setState {
                field  = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.register)
        mPresenter = RegisterAgeRedirectPresenter(this, arguments, this, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        umGridContainer {
            css(centerContainer)

            umItem(GridSize.cells9, GridSize.cells4) {

                umGridContainer {

                    umItem(GridSize.cells12) {
                        css{
                            +StyleManager.defaultFullWidth
                            +StyleManager.defaultDoubleMarginTop
                        }

                        umDatePicker(
                            label = "${dateLabel.text}",
                            error = dateLabel.error,
                            helperText = dateLabel.errorText,
                            value = dateOfBirth.toDate(),
                            inputFormat = DATE_FORMAT_DD_MMM_YYYY,
                            inputVariant = FormControlVariant.outlined,
                            onChange = {
                                setState {
                                    dateOfBirth = it.getTime().toLong()
                                }
                            })
                    }

                    umItem(GridSize.cells12) {
                        umButton(getString(MessageID.next),
                            size = ButtonSize.large,
                            color = UMColor.secondary,
                            variant = ButtonVariant.contained,
                            onClick = {
                                mPresenter?.handleClickNext()
                            }){
                            css {
                                +StyleManager.defaultFullWidth
                                +StyleManager.defaultDoubleMarginTop
                                height = LinearDimension("50px")
                            }}
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
    }
}