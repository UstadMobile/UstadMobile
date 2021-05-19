package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonSize
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ustadmobile.core.controller.Login2Presenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.util.CssStyleManager
import com.ustadmobile.util.CssStyleManager.defaultMarginTop
import com.ustadmobile.util.CssStyleManager.errorTextMessage
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
            setState { field = value }
        }

    override var isEmptyUsername: Boolean = false
        get() = field
        set(value) {
            setState { field = value }
        }

    override var inProgress: Boolean = false
        get() = field
        set(value) {
            setState { field = value }
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
                mTextField(label = systemImpl.getString(if(isEmptyUsername)
                    MessageID.field_required_prompt else MessageID.username, this),
                    value = username, error = isEmptyUsername, disabled = inProgress,
                    variant = MFormControlVariant.outlined, onChange = {
                            it.persist()
                        setState {
                            username = it.targetInputValue
                            isEmptyUsername = false
                        }
                    }) {css(loginComponentFormElementsMargin)}

                mTextField(label = systemImpl.getString(if(isEmptyPassword)
                    MessageID.field_required_prompt else MessageID.password, this),
                    value = password, disabled = inProgress, error = isEmptyPassword,
                    type = InputType.password, variant = MFormControlVariant.outlined, onChange = {
                        it.persist()
                        setState {
                            password = it.targetInputValue
                            isEmptyPassword = false
                        }
                    }) {css(loginComponentFormElementsMargin)}

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

                mButton(systemImpl.getString(MessageID.login, this),
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
                        mButton(systemImpl.getString(MessageID.create_account,this),
                            variant = MButtonVariant.text, color = MColor.primary, disabled = inProgress,
                            size = MButtonSize.large, onClick = {mPresenter.handleCreateAccount()}){
                            css{
                                display = if(createAccountVisible) Display.block else Display.none
                            }
                        }
                    }

                    mGridItem {
                        mButton(systemImpl.getString(MessageID.connect_as_guest,this),
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