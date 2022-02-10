package com.ustadmobile.view

import Breakpoint
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.core.controller.Login2Presenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextCenter
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultDoubleMarginTop
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.errorTextClass
import com.ustadmobile.util.StyleManager.hideOnMobile
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import down
import kotlinx.css.*
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

    private var usernameLabel: FieldLabel = FieldLabel(getString(MessageID.username))

    private val errorText = getString(MessageID.field_required_prompt)

    override var errorMessage: String = ""
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var viewNames: List<String>? = listOf(Login2View.VIEW_NAME)

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
        val spacing = GridSpacing.spacing1
        val gridSizeOnLeft = GridSize.cells3
        val gridSizeOnCenterOnMdDown = GridSize.cells6
        val gridSizeOnCenterLgUp = GridSize.cells4
        umGridContainer(spacing) {
            css {
                +contentContainer
                +alignTextCenter
                padding = "20px"
                marginTop = (if(createAccountVisible || connectAsGuestVisible) 10 else 13).spacingUnits
                media(StyleManager.theme.breakpoints.down(Breakpoint.sm)) {
                    marginTop = (if(createAccountVisible || connectAsGuestVisible) 15 else 18).spacingUnits
                }

            }
            umItem(GridSize.cells12, GridSize.cells12) {

                umGridContainer(spacing) {
                    css{
                        display = displayProperty(loginIntentMessage != null)
                    }
                    umItem(GridSize.cells12, gridSizeOnLeft) {
                        css(hideOnMobile)
                    }
                    
                    umItem(GridSize.cells12, gridSizeOnCenterOnMdDown) {
                        umTypography(loginIntentMessage,
                            variant = TypographyVariant.body2,
                            align = TypographyAlign.center,
                            gutterBottom = true)
                    }
                }

                umGridContainer(spacing) {
                    umItem(GridSize.cells12, gridSizeOnLeft, gridSizeOnCenterLgUp) {
                        css(hideOnMobile)
                    }
                    umItem(GridSize.cells12, gridSizeOnCenterOnMdDown, gridSizeOnCenterLgUp) {
                        umTextField(
                            label = "${usernameLabel.text}",
                            helperText = usernameLabel.errorText,
                            value = username,
                            error = usernameLabel.error,
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
                }

                umGridContainer(spacing) {
                    umItem(GridSize.cells12, gridSizeOnLeft,gridSizeOnCenterLgUp) {
                        css(hideOnMobile)
                    }

                    umItem(GridSize.cells12, gridSizeOnCenterOnMdDown,gridSizeOnCenterLgUp) {
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
                                attrs.endAdornment = umIconButton(if(showPassword) "visibility" else "visibility_off", edge = IconEdge.end, onClick = {
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
                }

                umGridContainer(spacing) {
                    css{
                        display = displayProperty(errorMessage.isNotEmpty(), true)
                        +defaultMarginTop
                    }
                    umItem(GridSize.cells12, gridSizeOnLeft,gridSizeOnCenterLgUp) {
                        css(hideOnMobile)
                    }
                    umItem(GridSize.cells12, gridSizeOnCenterOnMdDown,gridSizeOnCenterLgUp) {
                        umTypography(errorMessage,
                            variant = TypographyVariant.subtitle2,
                            className = "${StyleManager.name}-errorTextClass",
                            align = TypographyAlign.center)
                    }
                }

                umGridContainer(spacing) {
                    umItem(GridSize.cells12, gridSizeOnLeft,gridSizeOnCenterLgUp) {
                        css(hideOnMobile)
                    }
                    umItem(GridSize.cells12, gridSizeOnCenterOnMdDown,gridSizeOnCenterLgUp) {
                        umButton(getString(MessageID.login),
                            size = ButtonSize.large,
                            disabled = inProgress
                            ,color = UMColor.secondary,
                            variant = ButtonVariant.contained,
                            onClick = {
                                mPresenter?.handleLogin(username, password)
                            }){
                            css {
                                +defaultFullWidth
                                +defaultDoubleMarginTop
                                height = LinearDimension("50px")
                            }}
                    }
                }

                /*umGridContainer(spacing) {
                    css{
                        display = displayProperty(createAccountVisible)
                    }
                    umItem(GridSize.cells12, gridSizeOnLeft,gridSizeOnCenterLgUp) {
                        css(hideOnMobile)
                    }

                    umItem(GridSize.cells12, gridSizeOnCenterOnMdDown,gridSizeOnCenterLgUp) {
                        umButton(getString(MessageID.create_account),
                            variant = ButtonVariant.text,
                            color = UMColor.primary,
                            disabled = inProgress,
                            size = ButtonSize.large,
                            onClick = {
                                mPresenter?.handleCreateAccount()
                            }){
                            css(defaultFullWidth)
                        }
                    }
                }

                umGridContainer(spacing) {
                    css{
                        display = displayProperty(connectAsGuestVisible)
                    }

                    umItem(GridSize.cells12, gridSizeOnLeft,gridSizeOnCenterLgUp) {
                        css(hideOnMobile)
                    }

                    umItem(GridSize.cells12, gridSizeOnCenterOnMdDown,gridSizeOnCenterLgUp) {
                        umButton(getString(MessageID.connect_as_guest),
                            variant = ButtonVariant.text,
                            color = UMColor.primary,
                            disabled = inProgress,
                            size = ButtonSize.large,
                            onClick = {
                                mPresenter?.handleConnectAsGuest() }){
                            css(defaultFullWidth)
                        }
                    }
                }*/
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