package com.ustadmobile.view

import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.JoinWithCodePresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.JoinWithCodeView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.util.*
import com.ustadmobile.util.ext.format
import react.RBuilder
import react.setState

class JoinWithCodeComponent (mProps: UmProps): UstadBaseComponent<UmProps, UmState>(mProps),
    JoinWithCodeView {

    private var mPresenter: JoinWithCodePresenter? = null

    override val viewName: String
        get() = JoinWithCodeView.VIEW_NAME

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
        navController.navigateUp()
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
        title = when (tableId) {
            Clazz.TABLE_ID -> {
                setState { entityType = getString(MessageID.clazz) }
                getString(MessageID.join_existing_class)
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

      /*  styledDiv {
            css {
                +StyleManager.fieldsOnlyFormScreen
            }

            umGridContainer(MGridSpacing.spacing4) {
                umItem(MGridSize.cells12){
                    mTypography(
                        text = getString(MessageID.join_code_instructions),
                        variant = MTypographyVariant.body2,
                        color = MTypographyColor.textPrimary
                    )
                }

                umItem(MGridSize.cells12){
                    umGridContainer(MGridSpacing.spacing4) {
                        umItem(MGridSize.cells12, MGridSize.cells8 ) {
                            mTextField(label = "${codeLabel.text}",
                                helperText = codeLabel.errorText,
                                value = code,
                                error = codeLabel.error,
                                disabled = !buttonEnabled,
                                variant = MFormControlVariant.outlined,
                                onChange = {
                                    it.persist()
                                    setState {
                                        code = it.targetInputValue
                                        errorText = null
                                    }
                                }){
                                css(StyleManager.defaultFullWidth)
                            }
                        }

                        umItem(MGridSize.cells12, MGridSize.cells4 ) {
                            mButton(
                                caption = buttonLabel ?: "",
                                variant = MButtonVariant.contained,
                                color = MColor.secondary,
                                size = MButtonSize.large,
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
        }*/
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