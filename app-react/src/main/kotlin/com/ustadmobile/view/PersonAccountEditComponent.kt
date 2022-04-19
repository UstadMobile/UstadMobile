package com.ustadmobile.view

import com.ustadmobile.core.controller.PersonAccountEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.PersonAccountEditView
import com.ustadmobile.core.view.PersonAccountEditView.Companion.BLOCK_CHARACTER_SET
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultDoubleMarginTop
import com.ustadmobile.util.StyleManager.errorTextClass
import com.ustadmobile.util.StyleManager.hideOnMobile
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.clean
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import react.RBuilder
import react.dom.html.InputType
import react.setState
import styled.css
import styled.styledDiv

class PersonAccountEditComponent(mProps: UmProps) : UstadEditComponent<PersonWithAccount>(mProps),
    PersonAccountEditView {

    private var mPresenter: PersonAccountEditPresenter? = null

    private var showCurrentPassword = false

    private var showNewPassword = false

    private var showConfirmPassword = false

    private var confirmPasswordLabel = FieldLabel(getString(MessageID.confirm_password), id = "confirm-password")

    private var newPasswordLabel = FieldLabel(getString(MessageID.new_password), id = "new-password")

    private var currentPasswordLabel = FieldLabel(getString(MessageID.current_password), id = "current-password")

    private var usernameLabel = FieldLabel(getString(MessageID.username))

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override val mEditPresenter: UstadEditPresenter<*, PersonWithAccount>?
        get() = mPresenter

    override var currentPasswordError: String? = null
        set(value) {
            field = value
            setState {
                currentPasswordLabel = currentPasswordLabel.copy(errorText = value)
            }
        }

    override var newPasswordError: String? = null
        set(value) {
            field = value
            setState {
                newPasswordLabel = newPasswordLabel.copy(errorText = value?.clean() )
            }
        }

    override var confirmedPasswordError: String? = null
        set(value) {
            field = value
            if(noPasswordMatchError == null){
                setState {
                    confirmPasswordLabel = confirmPasswordLabel.copy(errorText = value?.clean())
                }
            }
        }

    override var noPasswordMatchError: String? = null
        set(value) {
            setState {
                confirmPasswordLabel = confirmPasswordLabel.copy(errorText = value?.clean())
            }
            field = value
        }

    override var usernameError: String? = null
        set(value) {
            field = value
            setState {
                usernameLabel = usernameLabel.copy(errorText = value?.clean() )
            }
        }

    override var errorMessage: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var currentPasswordVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var usernameVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: PersonWithAccount? = null
        get() = field
        set(value) {
            ustadComponentTitle = value?.fullName()
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        mPresenter = PersonAccountEditPresenter(this, arguments,this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv{
            css{
                +contentContainer
                +defaultDoubleMarginTop
            }

            umGridContainer {

                umItem(GridSize.cells3){
                    css(hideOnMobile)
                }

                umItem(GridSize.cells12, GridSize.cells6){
                    css(defaultDoubleMarginTop)
                    umGridContainer(rowSpacing = GridSpacing.spacing2) {

                        if(usernameVisible){
                           umItem(GridSize.cells12){
                               umTextField(
                                   label = "${usernameLabel.text}",
                                   value = entity?.username,
                                   error = usernameLabel.error,
                                   disabled = !fieldsEnabled,
                                   helperText = usernameLabel.errorText,
                                   variant = FormControlVariant.outlined,
                                   blockedCharacters = BLOCK_CHARACTER_SET,
                                   allowCapitalLetters = false,
                                   onChange = {
                                       setState {
                                           entity?.username = it
                                           usernameError = null
                                           errorMessage = ""
                                       }
                                   })
                           }
                       }

                        if(currentPasswordVisible){
                            umItem(GridSize.cells12){
                                umFormControl(variant = FormControlVariant.outlined) {
                                    umInputLabel(
                                        "${currentPasswordLabel.text}",
                                        id = currentPasswordLabel.id,
                                        error = currentPasswordLabel.error,
                                        variant = FormControlVariant.outlined,
                                        htmlFor = currentPasswordLabel.id)
                                    umOutlinedInput(
                                        id = currentPasswordLabel.id,
                                        label = "${currentPasswordLabel.text}",
                                        value = entity?.currentPassword,
                                        disabled = !fieldsEnabled,
                                        error = currentPasswordLabel.error,
                                        type =  if(showCurrentPassword) InputType.text else InputType.password,
                                        onChange = {
                                            setState {
                                                entity?.currentPassword = it
                                                currentPasswordError = null
                                            }
                                        }) {
                                        attrs.endAdornment = umIconButton(if(showCurrentPassword) "visibility" else "visibility_off", edge = IconEdge.end, onClick = {
                                            setState {
                                                showCurrentPassword = !showCurrentPassword
                                            }
                                        })

                                    }
                                    currentPasswordLabel.errorText?.let { error ->
                                        umFormHelperText(error){
                                            css(errorTextClass)
                                        }
                                    }
                                }
                            }
                        }

                        umItem(GridSize.cells12){
                            umFormControl(variant = FormControlVariant.outlined) {
                                css(StyleManager.defaultFullWidth)
                                umInputLabel("${newPasswordLabel.text}",
                                    id = newPasswordLabel.id,
                                    error = newPasswordLabel.error,
                                    variant = FormControlVariant.outlined,
                                    htmlFor = newPasswordLabel.id)
                                umOutlinedInput(
                                    id = newPasswordLabel.id,
                                    value = entity?.newPassword,
                                    label = "${newPasswordLabel.text}",
                                    disabled = !fieldsEnabled,
                                    error = newPasswordLabel.error,
                                    type =  if(showNewPassword) InputType.text else InputType.password,
                                    onChange = {
                                        setState {
                                            entity?.newPassword = it
                                            newPasswordError = null
                                        }
                                    }) {
                                    attrs.endAdornment = umIconButton(if(showNewPassword) "visibility" else "visibility_off", edge = IconEdge.end, onClick = {
                                        setState {
                                            showNewPassword = !showNewPassword
                                        }
                                    })

                                }
                                newPasswordLabel.errorText?.let { error ->
                                    umFormHelperText(error){
                                        css(errorTextClass)
                                    }
                                }
                            }
                        }

                        umItem(GridSize.cells12){
                            umFormControl(variant = FormControlVariant.outlined) {
                                umInputLabel("${confirmPasswordLabel.text}",
                                    error = confirmPasswordLabel.error,
                                    id = confirmPasswordLabel.id,
                                    variant = FormControlVariant.outlined,
                                    htmlFor = confirmPasswordLabel.id)
                                umOutlinedInput(
                                    id = confirmPasswordLabel.id,
                                    value = entity?.confirmedPassword,
                                    disabled = !fieldsEnabled,
                                    label = "${confirmPasswordLabel.text}",
                                    error = confirmPasswordLabel.error,
                                    type =  if(showConfirmPassword) InputType.text else InputType.password,
                                    onChange = {
                                        setState {
                                            entity?.confirmedPassword = it
                                            confirmedPasswordError = null
                                            noPasswordMatchError = null
                                        }
                                    }) {
                                    attrs.endAdornment = umIconButton(
                                        if(showConfirmPassword) "visibility" else "visibility_off",
                                        edge = IconEdge.end,
                                        onClick = {
                                            setState {
                                                showConfirmPassword = !showConfirmPassword
                                            }
                                        })

                                }
                                confirmPasswordLabel.errorText?.let { error ->
                                    umFormHelperText(error){
                                        css(errorTextClass)
                                    }
                                }
                            }
                        }

                        if(errorMessage != null){
                            umItem(GridSize.cells12, GridSize.cells6){
                                umTypography(errorMessage,
                                    variant = TypographyVariant.subtitle2,
                                    className = "${StyleManager.name}-errorTextClass",
                                    align = TypographyAlign.center)
                            }
                        }

                    }
                }

                umItem(GridSize.cells3){
                    css(hideOnMobile)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }
}