package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.*
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ccfraser.muirwik.components.form.mFormControl
import com.ccfraser.muirwik.components.form.mFormHelperText
import com.ccfraser.muirwik.components.input.mInputAdornment
import com.ccfraser.muirwik.components.input.mInputLabel
import com.ccfraser.muirwik.components.input.mOutlinedInput
import com.ustadmobile.FieldLabel
import com.ustadmobile.core.controller.Login2Presenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextCenter
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.errorTextClass
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.*
import kotlinx.html.InputType
import react.RBuilder
import react.RProps
import react.RState
import react.setState
import styled.css

class LoginComponent(props: RProps): UstadBaseComponent<RProps,RState>(props), Login2View {

    private lateinit var mPresenter: Login2Presenter

    private var username: String = ""

    private var password: String = ""

    private var showPassword = false

    private var passwordLabel: FieldLabel = FieldLabel(getString(MessageID.password))

    private var usernameLabel: FieldLabel = FieldLabel(getString(MessageID.username))

    private val errorText = getString(MessageID.field_required_prompt)

    override var errorMessage: String = ""
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var viewName: String? = Login2View.VIEW_NAME

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


    override fun onCreate() {
        super.onCreate()
        title = getString(MessageID.login)
        mPresenter = Login2Presenter(this, arguments,this, di)
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        val spacing = MGridSpacing.spacing6
        val gridSizeOnLeft = MGridSize.cells3
        val gridSizeOnCenterOnMdDown = MGridSize.cells6
        val gridSizeOnCenterLgUp = MGridSize.cells4
        umGridContainer(spacing) {
            css {
                +contentContainer
                +alignTextCenter
                marginTop = (if(createAccountVisible || connectAsGuestVisible) 10 else 13).spacingUnits
            }
            umItem(MGridSize.cells12, MGridSize.cells12) {

                umGridContainer(spacing) {
                    css{
                        display = displayProperty(loginIntentMessage != null)
                    }
                    mHidden(xsDown = true) {
                        umItem(MGridSize.cells12, gridSizeOnLeft) {}
                    }
                    umItem(MGridSize.cells12, gridSizeOnCenterOnMdDown) {
                        mTypography(loginIntentMessage,
                            variant = MTypographyVariant.body2,
                            align = MTypographyAlign.center,
                            gutterBottom = true)
                    }
                }

                umGridContainer(spacing) {
                    mHidden(xsDown = true) {
                        umItem(MGridSize.cells12, gridSizeOnLeft, gridSizeOnCenterLgUp) {}
                    }
                    umItem(MGridSize.cells12, gridSizeOnCenterOnMdDown, gridSizeOnCenterLgUp) {
                        mTextField(label = "${usernameLabel.text}",
                            helperText = usernameLabel.errorText,
                            value = username, error = usernameLabel.error,
                            disabled = inProgress,
                            variant = MFormControlVariant.outlined, onChange = {
                                it.persist()
                                setState {
                                    username = it.targetInputValue
                                    isEmptyUsername = false
                                    errorMessage = ""
                                }
                            }) {
                            css(defaultFullWidth)
                        }
                    }
                }

                umGridContainer(spacing) {
                    mHidden(xsDown = true) {
                        umItem(MGridSize.cells12, gridSizeOnLeft,gridSizeOnCenterLgUp) {}
                    }

                    umItem(MGridSize.cells12, gridSizeOnCenterOnMdDown,gridSizeOnCenterLgUp) {
                        mFormControl(variant = MFormControlVariant.outlined) {
                            css(defaultFullWidth)
                            mInputLabel("${passwordLabel.text}",
                                error = passwordLabel.error,
                                variant = MFormControlVariant.outlined,
                                htmlFor = "password-input")
                            mOutlinedInput(labelWidth = passwordLabel.width,id = "password-input",value = password, disabled = inProgress,
                                error = passwordLabel.error,
                                type =  if(showPassword) InputType.text else InputType.password,
                                onChange = {
                                    it.persist()
                                    setState {
                                        password = it.targetInputValue
                                        isEmptyPassword = false
                                        errorMessage = "" }
                                }) {
                                attrs.endAdornment = mInputAdornment {
                                    mIconButton(if(showPassword) "visibility" else "visibility_off", edge = MIconEdge.end, onClick = {
                                        setState { showPassword = !showPassword }
                                    })
                                }

                            }
                            passwordLabel.errorText?.let {
                                mFormHelperText(it){
                                    css(errorTextClass)
                                }
                            }
                        }
                    }
                }

                umGridContainer(spacing) {
                    css{
                        display = displayProperty(errorMessage.isNotEmpty(), true)
                    }
                    mHidden(xsDown = true) {
                        umItem(MGridSize.cells12, gridSizeOnLeft,gridSizeOnCenterLgUp) {}
                    }
                    umItem(MGridSize.cells12, gridSizeOnCenterOnMdDown,gridSizeOnCenterLgUp) {
                        mTypography(errorMessage,
                            variant = MTypographyVariant.subtitle2,
                            className = "${StyleManager.name}-errorTextClass",
                            align = MTypographyAlign.center)
                    }
                }

                umGridContainer(spacing) {
                    mHidden(xsDown = true) {
                        umItem(MGridSize.cells12, gridSizeOnLeft,gridSizeOnCenterLgUp) {}
                    }
                    umItem(MGridSize.cells12, gridSizeOnCenterOnMdDown,gridSizeOnCenterLgUp) {
                        mButton(getString(MessageID.login),
                            size = MButtonSize.large, disabled = inProgress
                            ,color = MColor.secondary,variant = MButtonVariant.contained, onClick = {
                                mPresenter.handleLogin(username, password)
                            }){
                            css {
                                +defaultFullWidth
                                height = 50.px
                            }}
                    }
                }

                umGridContainer(spacing) {
                    css{
                        display = displayProperty(createAccountVisible)
                    }
                    mHidden(xsDown = true) { umItem(MGridSize.cells12, gridSizeOnLeft,gridSizeOnCenterLgUp) {} }
                    umItem(MGridSize.cells12, gridSizeOnCenterOnMdDown,gridSizeOnCenterLgUp) {
                        mButton(getString(MessageID.create_account),
                            variant = MButtonVariant.text, color = MColor.primary, disabled = inProgress,
                            size = MButtonSize.large, onClick = {mPresenter.handleCreateAccount()}){
                            css(defaultFullWidth)
                        }
                    }
                }

                umGridContainer(spacing) {
                    css{
                        display = displayProperty(connectAsGuestVisible)
                    }
                    mHidden(xsDown = true) {
                        umItem(MGridSize.cells12, gridSizeOnLeft,gridSizeOnCenterLgUp) {}
                    }
                    umItem(MGridSize.cells12, gridSizeOnCenterOnMdDown,gridSizeOnCenterLgUp) {
                        mButton(getString(MessageID.connect_as_guest),
                            variant = MButtonVariant.text, color = MColor.primary,disabled = inProgress,
                            size = MButtonSize.large, onClick = { mPresenter.handleConnectAsGuest() }){
                            css(defaultFullWidth)
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

    override fun componentWillUnmount() {
        super.componentWillUnmount()
        mPresenter.onDestroy()
    }
}