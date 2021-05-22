package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.*
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ccfraser.muirwik.components.form.mFormControl
import com.ccfraser.muirwik.components.form.mFormHelperText
import com.ccfraser.muirwik.components.input.*
import com.ustadmobile.core.controller.Login2Presenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.model.UmLabel
import com.ustadmobile.util.CssStyleManager
import com.ustadmobile.util.CssStyleManager.defaultMarginTop
import com.ustadmobile.util.CssStyleManager.errorTextMessage
import com.ustadmobile.util.CssStyleManager.helperText
import com.ustadmobile.util.CssStyleManager.loginComponentContainer
import com.ustadmobile.util.CssStyleManager.loginComponentForm
import com.ustadmobile.util.CssStyleManager.loginComponentFormElementsMargin
import com.ustadmobile.util.RouteManager.getArgs
import kotlinx.css.*
import kotlinx.html.InputType
import react.RBuilder
import react.RProps
import react.RState
import react.setState
import styled.css
import styled.styledDiv

class LoginComponent(props: RProps): UstadBaseComponent<RProps,RState>(props), Login2View {

    private lateinit var mPresenter: Login2Presenter

    private var username: String = ""

    private var password: String = ""

    private var showPassword = false

    private var passwordLabel: UmLabel = UmLabel(getString(MessageID.password))

    private var usernameLabel: UmLabel = UmLabel(getString(MessageID.username))

    private val caption = getString(MessageID.field_required_prompt)

    override var errorMessage: String = ""
        get() = field
        set(value) {
            setState { field = value }
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
            setState { field = value }
        }

    override var isEmptyPassword: Boolean = false
        get() = field
        set(value) {
            field = value
            if(value){
                setState { passwordLabel = passwordLabel.copy(caption = caption) }
            }
        }

    override var isEmptyUsername: Boolean = false
        get() = field
        set(value) {
            field = value
            if(value){
                setState { usernameLabel = usernameLabel.copy(caption = caption)}
            }
        }

    override var inProgress: Boolean = false
        get() = field
        set(value) {
            field = value
            setState {loading = value}
        }

    override var createAccountVisible: Boolean = false
        get() = field
        set(value) {
            setState { field = value }
        }

    override var connectAsGuestVisible: Boolean = false
        get() = field
        set(value) {
            setState { field = value }
        }

    override fun onComponentReady() {
        mPresenter = Login2Presenter(this, getArgs(),this, di)
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
            css(loginComponentContainer)

            if(loginIntentMessage != null){
                mTypography(loginIntentMessage, variant = MTypographyVariant.body2,
                    align = MTypographyAlign.center, gutterBottom = true)
            }

            styledDiv {
                css{ +loginComponentForm}
                mTextField(label = "${usernameLabel.text}", helperText = usernameLabel.caption,
                    value = username, error = usernameLabel.error, disabled = inProgress,
                    variant = MFormControlVariant.outlined, onChange = {
                            it.persist()
                        setState {
                            username = it.targetInputValue
                            isEmptyUsername = false
                            errorMessage = ""
                        }
                    }) {css(loginComponentFormElementsMargin)}


                mFormControl(variant = MFormControlVariant.outlined) {
                    css(loginComponentFormElementsMargin)
                    mInputLabel("${passwordLabel.text}", error = passwordLabel.error,
                        variant = MFormControlVariant.outlined, htmlFor = "password-input")
                    mOutlinedInput(labelWidth = passwordLabel.width,id = "password-input",value = password, disabled = inProgress,
                        error = passwordLabel.error,
                        type =  if(showPassword) InputType.text else InputType.password, onChange = {
                            it.persist()
                            setState {
                                password = it.targetInputValue
                                isEmptyPassword = false
                                errorMessage = "" }}) {
                        attrs{
                            endAdornment = mInputAdornment {
                                mIconButton(if(showPassword) "visibility" else "visibility_off", edge = MIconEdge.end, onClick = {
                                        setState { showPassword = !showPassword }
                                    })
                            }
                        }

                    }
                    passwordLabel.caption?.let { mFormHelperText(it){css(helperText)} }
                }

                styledDiv {
                    css{
                        +defaultMarginTop
                        +loginComponentFormElementsMargin
                        +errorTextMessage
                        display = if(errorMessage.isEmpty()) Display.none else Display.block
                    }
                    mTypography(errorMessage, variant = MTypographyVariant.subtitle2,
                        className = "${CssStyleManager.name}-errorOnInput",
                        align = MTypographyAlign.center)
                }

                mButton(getString(MessageID.login),
                    size = MButtonSize.large, disabled = inProgress
                    ,color = MColor.secondary,variant = MButtonVariant.contained, onClick = {
                        mPresenter.handleLogin(username, password)
                    }){
                    css {
                        +loginComponentFormElementsMargin
                        +defaultMarginTop
                        height = 50.px
                    }}

                mGridContainer(spacing= MGridSpacing.spacing6){
                    css{
                        +defaultMarginTop
                    }
                    mGridItem {
                        css{
                            marginLeft = 16.px
                        }
                        mButton(getString(MessageID.create_account),
                            variant = MButtonVariant.text, color = MColor.primary, disabled = inProgress,
                            size = MButtonSize.large, onClick = {mPresenter.handleCreateAccount()}){
                            css{
                                display = if(createAccountVisible) Display.block else Display.none
                            }
                        }
                    }

                    mGridItem {
                        mButton(getString(MessageID.connect_as_guest),
                            variant = MButtonVariant.text, color = MColor.primary,disabled = inProgress,
                            size = MButtonSize.large, onClick = { mPresenter.handleConnectAsGuest() }){
                            css{
                                display = if(connectAsGuestVisible) Display.block else Display.none
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

    override fun componentWillUnmount() {
        super.componentWillUnmount()
        mPresenter.onDestroy()
    }
}