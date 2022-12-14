package com.ustadmobile.view

import com.ustadmobile.core.controller.JoinWithCodePresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.navigation.NavControllerJs
import com.ustadmobile.core.view.JoinWithCodeView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.LinearDimension
import kotlinx.css.marginTop
import kotlinx.css.padding
import mui.material.ButtonColor
import mui.material.ButtonVariant
import mui.material.FormControlVariant
import mui.material.Size
import mui.material.styles.TypographyVariant
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class JoinWithCodeComponent (mProps: UmProps): UstadBaseComponent<UmProps, UmState>(mProps),
    JoinWithCodeView {

    private var mPresenter: JoinWithCodePresenter? = null

    var buttonEnabled = true

    var entityType = ""

    private var codeLabel = FieldLabel()

    override var controlsEnabled: Boolean? = null
        get() = field
        set(value) {
            field = value
        }

    override var errorText: String? = null
        set(value) {
            setState {
                codeLabel = codeLabel.copy(errorText = value)
            }
        }

    override var code: String? = null
        set(value) {
           setState {
               field = value
           }
        }

    override fun finish() {
        //TODO: Why is this only on JS?
        (navController as NavControllerJs).navigateUp()
    }

    override var buttonLabel: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var loading: Boolean
        get() = super.loading
        set(value) {
            setState {
                super.loading = value
                buttonEnabled = !value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        mPresenter = JoinWithCodePresenter(this, arguments, this,
            di)
        val tableId = arguments[UstadView.ARG_CODE_TABLE].toString().toInt()
        ustadComponentTitle = when (tableId) {
            Clazz.TABLE_ID -> {
                setState { entityType = getString(MessageID.course) }
                getString(MessageID.join_existing_course)
            }
            School.TABLE_ID -> {
                setState { entityType = getString(MessageID.school) }
                getString(MessageID.join_existing_school)
            }
            else -> {
                "ERR - Unknown entity type"
            }
        }
        setState {
            codeLabel = codeLabel.copy(text = getString(MessageID.entity_code).format(entityType))
        }
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {

        styledDiv {
            css {
                +StyleManager.fieldsOnlyFormScreen
            }

            umGridContainer(GridSpacing.spacing4) {
                umItem(GridSize.cells12){
                    umTypography(
                        text = getString(MessageID.join_code_instructions),
                        variant = TypographyVariant.body2
                    )
                }

                umItem(GridSize.cells12){
                    umGridContainer(GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells8) {
                            umTextField(label = "${codeLabel.text}",
                                helperText = codeLabel.errorText,
                                value = code,
                                error = codeLabel.error,
                                disabled = !buttonEnabled,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        code = it
                                        errorText = null
                                    }
                                }){
                                css(StyleManager.defaultFullWidth)
                            }
                        }

                        umItem(GridSize.cells12, GridSize.cells4) {
                            umButton(
                                label = buttonLabel ?: "",
                                disabled = code.isNullOrEmpty(),
                                variant = ButtonVariant.contained,
                                color = ButtonColor.secondary,
                                size = Size.large,
                                onClick = {
                                    code?.let { code ->
                                        mPresenter?.handleClickDone(code)
                                    }
                                }
                            ){
                                css{
                                    padding = "15px"
                                    marginTop = LinearDimension("13px")
                                    +StyleManager.defaultFullWidth
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        controlsEnabled = null
        errorText = null
        code = null
        buttonLabel = null
    }

}