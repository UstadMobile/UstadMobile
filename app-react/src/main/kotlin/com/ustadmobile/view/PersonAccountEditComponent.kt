package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MIconEdge
import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ccfraser.muirwik.components.form.mFormControl
import com.ccfraser.muirwik.components.input.mInputAdornment
import com.ccfraser.muirwik.components.input.mInputLabel
import com.ccfraser.muirwik.components.input.mOutlinedInput
import com.ustadmobile.core.controller.PersonAccountEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.PersonAccountEditView
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.model.UmLabel
import com.ustadmobile.util.CssStyleManager
import com.ustadmobile.util.CssStyleManager.errorTextMessage
import com.ustadmobile.util.CssStyleManager.formFieldsContainer
import com.ustadmobile.util.CssStyleManager.formTextField
import com.ustadmobile.util.CssStyleManager.loginComponentFormElementsMargin
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.util.ext.clean
import kotlinx.css.Display
import kotlinx.css.display
import kotlinx.html.InputType
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv

class PersonAccountEditComponent(mProps: RProps) : UstadEditComponent<PersonWithAccount>(mProps),
    PersonAccountEditView {

    private lateinit var mPresenter: PersonAccountEditPresenter

    private var showCurrentPassword = false

    private var showNewPassword = false

    private var showConfirmPassword = false

    private var confirmPasswordLabel = UmLabel(systemImpl.getString(MessageID.confirm_password, this))

    private var newPasswordLabel = UmLabel(systemImpl.getString(MessageID.new_password, this))

    private var currentPasswordLabel = UmLabel(systemImpl.getString(MessageID.current_password, this))

    private var usernameLabel = UmLabel(systemImpl.getString(MessageID.username, this))

    override val viewName: String
        get() = PersonAccountEditView.VIEW_NAME

    override var fieldsEnabled: Boolean = true

    override val mEditPresenter: UstadEditPresenter<*, PersonWithAccount>?
        get() = mPresenter

    override var currentPasswordError: String? = null
        set(value) {
            field = value
            setState { currentPasswordLabel = currentPasswordLabel.copy(
                text = value ?: systemImpl.getString(MessageID.current_password,this),
                error = value != null) }
        }

    override var newPasswordError: String? = null
        set(value) {
            field = value
            setState { newPasswordLabel = newPasswordLabel.copy(
                text = value ?: systemImpl.getString(MessageID.new_password,this),
                error = value != null) }
        }

    override var confirmedPasswordError: String? = null
        set(value) {
            field = value
            field = value
            if(noPasswordMatchError == null){
                setState { confirmPasswordLabel = confirmPasswordLabel.copy(
                    text = value ?: systemImpl.getString(MessageID.confirm_password,this),
                    error = value != null) }
            }
        }

    override var noPasswordMatchError: String? = null
        get() = field
        set(value) {
            setState {
                confirmPasswordLabel = confirmPasswordLabel.copy(
                text = value ?: systemImpl.getString(MessageID.confirm_password,this),
                error = value != null) }
            field = value
        }

    override var usernameError: String? = null
        set(value) {
            field = value
            setState { usernameLabel = usernameLabel.copy(
                text = value ?: systemImpl.getString(MessageID.username,this),
                error = value != null) }
        }

    override var errorMessage: String? = null
        get() = field
        set(value) {
            setState { field = value }
        }

    override var currentPasswordVisible: Boolean = false
        get() = field
        set(value) {
            setState { field = value }
        }

    override var entity: PersonWithAccount? = null
        get() = field
        set(value) {
            setState { field = value }
        }

    override fun onComponentReady() {
        super.onComponentReady()
        mPresenter = PersonAccountEditPresenter(this, getArgs(),this, di, this)
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
            css{formFieldsContainer}

            mTextField(label = "${usernameLabel.text}", value = entity?.username,
                error = usernameLabel.error, disabled = !fieldsEnabled,
                variant = MFormControlVariant.outlined, onChange = {
                    it.persist()
                    setState {
                        entity?.username = it.targetInputValue
                        usernameError = null
                        errorMessage = "" }
                }) {
                css(formTextField)
            }

            mFormControl {
                css{
                    +formTextField
                    display = if(currentPasswordVisible) Display.flex else Display.none
                }
                mInputLabel("${currentPasswordLabel.text}", error = currentPasswordLabel.error,
                    variant = MFormControlVariant.outlined, htmlFor = "current-password")
                mOutlinedInput(value = entity?.currentPassword, labelWidth = currentPasswordLabel.width,error = currentPasswordError != null,
                    id = "current-password",type = if(showCurrentPassword) InputType.text else InputType.password, onChange = {
                    it.persist()
                    setState {
                        entity?.currentPassword = it.targetInputValue
                        currentPasswordError = null
                        errorMessage = "" } }) {
                    attrs{
                        endAdornment = mInputAdornment {
                            mIconButton(if(showCurrentPassword) "visibility" else "visibility_off",edge = MIconEdge.end, onClick = {
                                setState { showCurrentPassword = !showCurrentPassword }
                            })
                        }
                    }
                }
            }

            mFormControl {
                css(formTextField)
                val label = UmLabel(if(newPasswordError == null) systemImpl.getString(MessageID.new_password, this)
                else newPasswordError.toString())
                mInputLabel("${label.text}",error = newPasswordError != null,
                    variant = MFormControlVariant.outlined, htmlFor = "new-password")
                mOutlinedInput(value = entity?.newPassword, labelWidth = label.width,error = newPasswordError != null,
                    id = "new-password", type = if(showNewPassword) InputType.text else InputType.password, onChange = {
                    setState {
                        entity?.newPassword = it.targetInputValue
                        newPasswordError = null
                        errorMessage = "" } }) {
                    attrs{
                        endAdornment = mInputAdornment {
                            mIconButton(if(showNewPassword) "visibility" else "visibility_off",edge = MIconEdge.end,onClick = {
                                setState { showNewPassword = !showNewPassword }
                            })
                        }
                    }
                }
            }

            mFormControl {
                css(formTextField)
                mInputLabel("${confirmPasswordLabel.text?.clean()}",error = confirmPasswordLabel.error,
                    variant = MFormControlVariant.outlined, htmlFor = "confirm-password")
                mOutlinedInput(value = entity?.confirmedPassword, labelWidth = confirmPasswordLabel.width,error = confirmPasswordLabel.error,
                    id = "confirm-password", type = if(showConfirmPassword) InputType.text else InputType.password,onChange = {
                    setState {
                        entity?.confirmedPassword = it.targetInputValue
                        confirmedPasswordError = null
                        errorMessage = "" } }) {
                    attrs{
                        endAdornment = mInputAdornment {
                            mIconButton(if(showConfirmPassword) "visibility" else "visibility_off",edge = MIconEdge.end, onClick = {
                                setState { showConfirmPassword = !showConfirmPassword }
                            })
                        }
                    }
                }
            }

            styledDiv {
                css{
                    +loginComponentFormElementsMargin
                    +errorTextMessage
                    display = if(errorMessage?.isEmpty() == true) Display.none else Display.block
                }
                mTypography(errorMessage, variant = MTypographyVariant.subtitle2,
                    className = "${CssStyleManager.name}-errorOnInput",
                    align = MTypographyAlign.center)
            }

        }
    }


}