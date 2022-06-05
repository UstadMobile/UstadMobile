package com.ustadmobile.view

import com.ustadmobile.core.controller.PersonEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.clean
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.components.AttachmentImageComponent
import com.ustadmobile.view.ext.umEntityAvatar
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.LinearDimension
import kotlinx.css.margin
import kotlinx.css.marginTop
import react.RBuilder
import react.dom.html.InputType
import react.setState
import styled.css
import styled.styledDiv

class PersonEditComponent(mProps: UmProps) : UstadEditComponent<PersonWithAccount>(mProps), PersonEditView {

    private var mPresenter: PersonEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, PersonWithAccount>?
        get() = mPresenter

    private var showPassword = false

    private var showConfirmPassword = false

    private var registrationEnabled = false

    private var minorRegistrationModeEnabled = false

    override var genderOptions: List<MessageIdOption>? = null
        get() = field
        set(value) {
            setState{
                field = value
            }
        }

    override var personPicture: PersonPicture? = null
        get() = field
        set(value) {
            setState{
                field = value
            }
        }

    override var approvalPersonParentJoin: PersonParentJoin? = null
        get() = field
        set(value) {
            setState{
                field = value
            }
        }

    override var registrationMode: Int = 0
        get() = field
        set(value) {
            registrationEnabled = value.hasFlag(PersonEditView.REGISTER_MODE_ENABLED)
            minorRegistrationModeEnabled = value.hasFlag(PersonEditView.REGISTER_MODE_MINOR)

            if(registrationEnabled) {
                ustadComponentTitle = getString(MessageID.register)
            }else {
                setEditTitle(MessageID.add_a_new_person, MessageID.edit_person)
            }

            setState {
                field = value
            }
        }

    private var usernameLabel = FieldLabel(getString(MessageID.username))

    override var usernameError: String? = null
        set(value) {
            setState{
                usernameLabel = usernameLabel.copy(errorText =  value)
            }
            field = value
        }

    override var noMatchPasswordError: String? = null
        set(value) {
            field = value
            setState{
                confirmPasswordLabel = confirmPasswordLabel.copy(errorText = value)
            }
        }

    private var passwordLabel = FieldLabel(getString(MessageID.password), id = "password-field")

    override var passwordError: String?  = null
        set(value) {
            field = value
            setState{
                passwordLabel = passwordLabel.copy(errorText = value)
            }
        }

    private var emailLabel = FieldLabel(getString(MessageID.email))

    private var parentEmailLabel = FieldLabel(getString(MessageID.parents_email_address).clean())

    override var emailError: String?  = null
        set(value) {
            setState{
                emailLabel = emailLabel.copy(errorText = value)
            }
            field = value
        }

    private var confirmPasswordLabel = FieldLabel(
        getString(MessageID.confirm_password), id = "confirm-password-field")
    
    override var confirmError: String?  = null
        set(value) {
            setState{
                confirmPasswordLabel = confirmPasswordLabel.copy(errorText = value)
            }
            field = value
        }

    private var dobLabel = FieldLabel(getString(MessageID.birthday))

    override var dateOfBirthError: String? = null
        set(value) {
            setState {
                dobLabel = dobLabel.copy(errorText = value )
            }
            field = value
        }

    override var parentContactError: String?  = null
        get() = field
        set(value) {
            field = value
            setState{
                parentEmailLabel = parentEmailLabel.copy(errorText = value)
            }
        }

    private var firstNameLabel = FieldLabel(text = getString(MessageID.first_name))

    override var firstNamesFieldError: String? = null
        set(value) {
            field = value
            setState {
                firstNameLabel = firstNameLabel.copy(errorText = value)
            }
        }

    override var lastNameFieldError: String?  = null
        set(value) {
            field = value
        }

    private var genderLabel = FieldLabel(getString(MessageID.field_person_gender))

    override var genderFieldError: String?  = null
        set(value) {
            setState{
                genderLabel = genderLabel.copy(errorText = value)
            }
            field = value
        }

    override var firstNameError: String?  = null
        set(value) {
            setState{
                firstNameLabel = firstNameLabel.copy(errorText = value)
                field = value
            }
        }

    private var lastNameLabel = FieldLabel(getString(MessageID.last_name))

    override var lastNameError: String?  = null
        set(value) {
            setState{
                lastNameLabel = lastNameLabel.copy(errorText = value)
            }
            field = value
        }

    override fun navigateToNextDestination(account: UmAccount?, nextDestination: String) {
        systemImpl.go(nextDestination, mapOf(), this)
    }

    override var fieldsEnabled: Boolean = true
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: PersonWithAccount? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        loading = false
        mPresenter = PersonEditPresenter(this, arguments,this, di, this)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        styledDiv {
            css{
                +contentContainer
                +defaultPaddingTop
            }

            umGridContainer(GridSpacing.spacing4) {
                umItem(GridSize.cells12, GridSize.cells4){
                    css{
                        marginTop = LinearDimension("12px")
                    }

                    child(AttachmentImageComponent::class) {
                        attrs.attachmentUri = personPicture?.personPictureUri
                        attrs.onNewImageSelected = {
                            setState {
                                console.log("ClazzEditComponent: setting course picture uri = $it")
                                personPicture?.personPictureUri = it
                            }
                        }
                        attrs.contentBlock = { attachmentImgSrc ->
                            umEntityAvatar(src = attachmentImgSrc) {}
                        }
                    }
                }

                umItem(GridSize.cells12, GridSize.cells8){

                    umTextField(label = "${firstNameLabel.text}",
                        helperText = firstNameLabel.errorText,
                        value = entity?.firstNames, 
                        error = firstNameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.firstNames = it
                                firstNameError = null
                            }
                        })


                    umTextField(label = "${lastNameLabel.text}",
                        value = entity?.lastName,
                        error = lastNameLabel.error,
                        disabled = !fieldsEnabled,
                        helperText = lastNameLabel.errorText,
                        variant = FormControlVariant.outlined,
                        onChange = {
                            setState {
                                entity?.lastName = it
                                lastNameError = null
                            }
                        })

                    umGridContainer(GridSpacing.spacing4) {

                        if(minorRegistrationModeEnabled){
                            umItem(GridSize.cells12, GridSize.cells6 ) {
                                umTextField(
                                    label = "${parentEmailLabel.text}",
                                    value = approvalPersonParentJoin?.ppjEmail,
                                    error = parentEmailLabel.error,
                                    disabled = !fieldsEnabled,
                                    variant = FormControlVariant.outlined,
                                    onChange = {
                                        setState {
                                            approvalPersonParentJoin?.ppjEmail = it
                                            parentContactError = null
                                        }
                                    })
                            }
                        }

                        if(!registrationEnabled){
                            umItem(GridSize.cells12, GridSize.cells6 ) {

                                umDatePicker(
                                    label = "${dobLabel.text}",
                                    error = dobLabel.error,
                                    helperText = dobLabel.errorText,
                                    value = entity?.dateOfBirth.toDate(),
                                    inputVariant = FormControlVariant.outlined,
                                    onChange = {
                                        setState {
                                            entity?.dateOfBirth = it.getTime().toLong()
                                            dateOfBirthError = null
                                        }
                                    })

                            }
                        }

                        umItem(GridSize.cells12, if(registrationEnabled && !minorRegistrationModeEnabled)
                            GridSize.cells12 else GridSize.cells6 ) {
                            umTextFieldSelect(
                                "${genderLabel.text}",
                                entity?.gender.toString(),
                                genderLabel.errorText ?: "",
                                error = genderLabel.error,
                                values = genderOptions?.map {
                                    Pair(it.code.toString(), it.toString())
                                }?.toList(),
                                onChange = {
                                    setState {
                                        entity?.gender = it.toInt()
                                        genderFieldError = null
                                    }
                                }
                            )
                        }
                    }

                    if(!minorRegistrationModeEnabled){
                        umTextField(
                            label = getString(MessageID.address),
                            value = entity?.personAddress,
                            disabled = !fieldsEnabled,
                            variant = FormControlVariant.outlined,
                            onChange = {
                                setState {
                                    entity?.personAddress = it
                                }
                            })
                    }

                    umGridContainer(GridSpacing.spacing4) {
                        css{
                            margin(
                                bottom = (if(registrationEnabled && !minorRegistrationModeEnabled)
                                0 else 4).spacingUnits
                            )
                        }
                       if(!minorRegistrationModeEnabled){
                           umItem(GridSize.cells12, GridSize.cells6 ) {
                               umTextField(
                                   label = getString(MessageID.phone_number),
                                   value = entity?.phoneNum,
                                   disabled = !fieldsEnabled,
                                   variant = FormControlVariant.outlined,
                                   onChange = {
                                       setState {
                                           entity?.phoneNum = it
                                       }
                                   })
                           }
                       }

                        if(!minorRegistrationModeEnabled){
                            umItem(GridSize.cells12, GridSize.cells6 ) {
                                umTextField(
                                    label = "${emailLabel.text}",
                                    value = entity?.emailAddr,
                                    error = emailLabel.error,
                                    disabled = !fieldsEnabled,
                                    variant = FormControlVariant.outlined,
                                    onChange = {
                                        setState {
                                            entity?.emailAddr = it
                                            emailError = null
                                        }
                                    })
                            }
                        }
                    }

                    if(registrationEnabled){
                        umTextField(
                            autoFocus = false,
                            label = "${usernameLabel.text}",
                            value = entity?.username,
                            error = usernameLabel.error,
                            disabled = !fieldsEnabled,
                            variant = FormControlVariant.outlined,
                            onChange = {
                                setState {
                                    entity?.username = it
                                    usernameError = null
                                }
                            })
                    }

                    umGridContainer(GridSpacing.spacing4) {

                        if(registrationEnabled){
                            umItem(GridSize.cells12, GridSize.cells6 ) {
                                css(defaultMarginTop)
                                umFormControl(variant = FormControlVariant.outlined) {
                                    umInputLabel("${passwordLabel.text}",
                                        id = passwordLabel.id,
                                        error = passwordLabel.error,
                                        variant = FormControlVariant.outlined,
                                        htmlFor = passwordLabel.id)
                                    umOutlinedInput(
                                        id = passwordLabel.id,
                                        value = entity?.newPassword,
                                        disabled = !fieldsEnabled,
                                        label = passwordLabel.text,
                                        error = passwordLabel.error,
                                        type =  if(showPassword) InputType.text else InputType.password,
                                        onChange = {
                                            setState {
                                                entity?.newPassword = it
                                                passwordError = null
                                            }
                                        }) {
                                        attrs.endAdornment = umIconButton(
                                            if(!showPassword) "visibility" else "visibility_off",
                                            edge = IconEdge.end,
                                            onClick = {
                                            setState {
                                                showPassword = !showPassword
                                            }
                                        })

                                    }
                                    passwordLabel.errorText?.let { error ->
                                        umFormHelperText(error){
                                            css(StyleManager.errorTextClass)
                                        }
                                    }
                                }
                            }

                            umItem(GridSize.cells12, GridSize.cells6 ) {
                                css(defaultMarginTop)

                                umFormControl(variant = FormControlVariant.outlined) {
                                    umInputLabel("${confirmPasswordLabel.text}",
                                        id = confirmPasswordLabel.id,
                                        error = confirmPasswordLabel.error,
                                        variant = FormControlVariant.outlined,
                                        htmlFor = confirmPasswordLabel.id)
                                    umOutlinedInput(
                                        id = confirmPasswordLabel.id,
                                        value = entity?.confirmedPassword,
                                        disabled = !fieldsEnabled,
                                        error = confirmPasswordLabel.error,
                                        label = confirmPasswordLabel.text,
                                        type =  if(showConfirmPassword) InputType.text else InputType.password,
                                        onChange = {
                                            setState {
                                                entity?.confirmedPassword = it
                                                confirmError = null
                                            }
                                        }) {
                                        attrs.endAdornment = umIconButton(
                                            if(!showConfirmPassword) "visibility" else "visibility_off",
                                            edge = IconEdge.end,
                                            onClick = {
                                                setState {
                                                    showConfirmPassword = !showConfirmPassword
                                                }
                                            })

                                    }
                                    confirmPasswordLabel.errorText?.let { error ->
                                        umFormHelperText(error){
                                            css(StyleManager.errorTextClass)
                                        }
                                    }
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
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }
}