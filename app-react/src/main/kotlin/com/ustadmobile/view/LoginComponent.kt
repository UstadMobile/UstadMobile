package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonSize
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ustadmobile.core.controller.Login2Presenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.model.statemanager.AppBarState
import com.ustadmobile.util.CssStyleManager.defaultMarginTop
import com.ustadmobile.util.CssStyleManager.loginComponentFormElements
import com.ustadmobile.util.CssStyleManager.loginComponentContainer
import com.ustadmobile.util.CssStyleManager.loginComponentForm
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.util.StateManager
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.display
import kotlinx.html.InputType
import react.RBuilder
import react.RProps
import react.RState
import react.setState
import styled.css
import styled.styledDiv

class LoginComponent(props: RProps): UmBaseComponent<RProps,RState>(props), Login2View {

    private lateinit var mPresenter: Login2Presenter

    private var username: String? = null

    private var password: String? = null

    override var errorMessage: String = ""
        get() = field
        set(value) {
            field = value
        }

    override var versionInfo: String? = null
        get() = field
        set(value) {
            field = value
        }

    override var isEmptyPassword: Boolean = false
        get() = field
        set(value) {
            field = value
        }

    override var isEmptyUsername: Boolean = false
        get() = field
        set(value) {
            field = value
        }

    override var inProgress: Boolean = false
        get() = field
        set(value) {
            StateManager.dispatch(AppBarState(loading = value))
            field = value
        }

    override var createAccountVisible: Boolean = false
        get() = field
        set(value) {
            field = value
        }

    override var connectAsGuestVisible: Boolean = false
        get() = field
        set(value) {
            field = value
        }

    override fun componentDidMount() {
        super.componentDidMount()
        mPresenter = Login2Presenter(this, getArgs(),this, di)
    }

    override fun RBuilder.render() {
        styledDiv {
            css(loginComponentContainer)
            styledDiv {
                css{ +loginComponentForm}
                val userNameLabel = if(isEmptyPassword) errorMessage
                else systemImpl.getString(MessageID.username, this)

                val passwordLabel = if(isEmptyUsername) errorMessage
                else systemImpl.getString(MessageID.password, this)

                mTextField(label = userNameLabel, value = username,
                    error = isEmptyUsername,
                    variant = MFormControlVariant.outlined, onChange = {
                            event -> event.persist()
                        setState {  username = event.targetInputValue  }
                    }) {css(loginComponentFormElements)}

                mTextField(label = passwordLabel, value = username,
                    error = isEmptyPassword,type = InputType.password,
                    variant = MFormControlVariant.outlined, onChange = {
                            event -> event.persist()
                        setState {  password = event.targetInputValue  }
                    }) {css(loginComponentFormElements)}

                mTypography(errorMessage, variant = MTypographyVariant.subtitle2){
                    css{
                        +defaultMarginTop
                        display = if(errorMessage.isEmpty()) Display.none else Display.block
                    }
                }

                mButton(systemImpl.getString(MessageID.login, this), size = MButtonSize.large
                    ,color = MColor.secondary,variant = MButtonVariant.contained, onClick = {
                        mPresenter.handleLogin(username, password) }){
                    css {
                        +loginComponentFormElements
                        +defaultMarginTop
                    }}

            }
        }
    }

    override fun clearFields() {
        setState {
            username = null
            password = null
        }
    }
}