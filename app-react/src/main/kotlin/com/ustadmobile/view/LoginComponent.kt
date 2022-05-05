package com.ustadmobile.view

import com.ustadmobile.core.controller.Login2Presenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.centerContainer
import com.ustadmobile.util.StyleManager.defaultDoubleMarginTop
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.errorTextClass
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.height
import kotlinx.css.marginTop
import kotlinx.css.px
import react.RBuilder
import react.dom.html.InputType
import react.setState
import styled.css

class LoginComponent(props: UmProps): UstadBaseComponent<UmProps,UmState>(props), Login2View {

    private var mPresenter: Login2Presenter? = null

    private var username: String = ""

    private var password: String = ""

    private var showPassword = false

    private var passwordLabel: FieldLabel = FieldLabel(getString(MessageID.password), id = "password-input")

    private var usernameLabel: FieldLabel = FieldLabel(getString(MessageID.username), id = "username-input")

    private val errorText = getString(MessageID.field_required_prompt)

    override var errorMessage: String = ""
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var versionInfo: String? = null
        get() = field
        set(value) {
            field = value
        }

    override var loginIntentMessage: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var isEmptyPassword: Boolean = false
        get() = field
        set(value) {
            field = value
            if(value){
                setState {
                    passwordLabel = passwordLabel.copy(errorText = errorText)
                }
            }
        }

    override var isEmptyUsername: Boolean = false
        get() = field
        set(value) {
            field = value
            if(value){
                setState {
                    usernameLabel = usernameLabel.copy(errorText = errorText)
                }
            }
        }

    override var inProgress: Boolean = false
        get() = field
        set(value) {
            field = value
            setState {
                loading = value
            }
        }

    override var createAccountVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var connectAsGuestVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.login)
        mPresenter = Login2Presenter(this, arguments,this, di)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {

        umGridContainer {
            css{
                +centerContainer
                marginTop = (if(createAccountVisible || connectAsGuestVisible) 1 else 3).spacingUnits
            }

            umItem(GridSize.cells10, GridSize.cells4) {
                umGridContainer {

                    umItem(GridSize.cells12) {
                        umTypography(loginIntentMessage,
                            variant = TypographyVariant.body2,
                            align = TypographyAlign.center,
                            gutterBottom = true)
                    }

                    umItem(GridSize.cells12) {
                        umTextField(
                            label = "${usernameLabel.text}",
                            helperText = usernameLabel.errorText,
                            value = username,
                            error = usernameLabel.error,
                            id = usernameLabel.id,
                            disabled = inProgress,
                            variant = FormControlVariant.outlined,
                            onChange = {
                                setState {
                                    username = it
                                    isEmptyUsername = false
                                    errorMessage = ""
                                }
                            }) {
                            css(defaultFullWidth)
                        }
                    }

                    umItem(GridSize.cells12) {
                        umFormControl(variant = FormControlVariant.outlined) {
                            css{
                                +defaultMarginTop
                            }
                            umInputLabel("${passwordLabel.text}",
                                id = passwordLabel.id,
                                error = passwordLabel.error,
                                variant = FormControlVariant.outlined,
                                htmlFor = passwordLabel.id)
                            umOutlinedInput(
                                id = passwordLabel.id,
                                value = password,
                                label = passwordLabel.text,
                                disabled = inProgress,
                                error = passwordLabel.error,
                                type =  if(showPassword) InputType.text else InputType.password,
                                onChange = {
                                    setState {
                                        password = it
                                        isEmptyPassword = false
                                        errorMessage = "" }
                                }) {
                                attrs.endAdornment = umIconButton(
                                    if(!showPassword) "visibility" else "visibility_off",
                                    edge = IconEdge.end,
                                    onClick = {
                                    setState { showPassword = !showPassword }
                                })

                            }
                            passwordLabel.errorText?.let { error ->
                                umFormHelperText(error){
                                    css(errorTextClass)
                                }
                            }
                        }
                    }

                    umItem(GridSize.cells12) {
                        umTypography(errorMessage,
                            variant = TypographyVariant.subtitle2,
                            className = "${StyleManager.name}-errorTextClass",
                            align = TypographyAlign.center)
                        css{
                            marginTop = 2.spacingUnits
                        }
                    }

                    umItem(GridSize.cells12) {
                        umButton(getString(MessageID.login),
                            size = ButtonSize.large,
                            disabled = inProgress,
                            color = UMColor.secondary,
                            id = "login-btn",
                            variant = ButtonVariant.contained,
                            onClick = {
                                mPresenter?.handleLogin(username, password)
                            }){
                            css {
                                +defaultFullWidth
                                +defaultDoubleMarginTop
                                height = 50.px
                            }}
                    }

                    if(createAccountVisible){
                        umItem(GridSize.cells12) {
                            umButton(getString(MessageID.create_account),
                                variant = ButtonVariant.outlined,
                                color = UMColor.primary,
                                disabled = inProgress,
                                size = ButtonSize.large,
                                onClick = {
                                    mPresenter?.handleCreateAccount()
                                }){
                                css{
                                    +defaultFullWidth
                                    marginTop = 2.spacingUnits
                                    height = 50.px
                                }
                            }
                        }
                    }

                    if(connectAsGuestVisible){
                        umItem(GridSize.cells12) {
                            umButton(getString(MessageID.connect_as_guest),
                                variant = ButtonVariant.outlined,
                                color = UMColor.primary,
                                disabled = inProgress,
                                size = ButtonSize.large,
                                onClick = {
                                    mPresenter?.handleConnectAsGuest()
                                }){
                                css{
                                    +defaultFullWidth
                                    marginTop = 2.spacingUnits
                                    height = 50.px
                                }
                            }
                        }
                    }

                }

            }
        }
    }

    override fun clearFields() {
        setState {
            username = ""
            password = ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}