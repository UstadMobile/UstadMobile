package com.ustadmobile.view

import com.ustadmobile.FieldLabel
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
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import kotlinx.css.Display
import react.RBuilder
import com.ustadmobile.util.*
import react.setState

class PersonEditComponent(mProps: UmProps) : UstadEditComponent<PersonWithAccount>(mProps), PersonEditView {

    private var mPresenter: PersonEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, PersonWithAccount>?
        get() = mPresenter

    private var showPassword = false

    private var showConfirmPassword = false

    private var displayRegField = Display.flex

    override val viewName: String
        get() = PersonEditView.VIEW_NAME

    override var genderOptions: List<MessageIdOption>? = null
        get() = field
        set(value) {
            setState{
                field = value
            }
        }

    override var personPicturePath: String? = null
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
            setState {
                displayRegField = displayProperty(
                    value == PersonEditView.REGISTER_MODE_ENABLED, true)
            }
            field = value
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

    private var passwordLabel = FieldLabel(getString(MessageID.password))

    override var passwordError: String?  = null
        set(value) {
            setState{
                passwordLabel = passwordLabel.copy(errorText = value)
            }
            field = value
        }

    private var emailLabel = FieldLabel(getString(MessageID.email))

    override var emailError: String?  = null
        set(value) {
            setState{
                emailLabel = emailLabel.copy(errorText = value)
            }
            field = value
        }

    private var confirmPasswordLabel = FieldLabel(getString(MessageID.confirm_password))
    
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
            setState{}
            field = value
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
        if(registrationMode.hasFlag(PersonEditView.REGISTER_MODE_ENABLED)) {
            setState {
                title = getString(MessageID.register)
            }
        }else {
            setEditTitle(MessageID.add_a_new_person, MessageID.edit_person)
        }
        mPresenter = PersonEditPresenter(this, arguments,this, di, this)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
       /* styledDiv {
            css{
                +contentContainer
                +defaultPaddingTop
            }

            umGridContainer(MGridSpacing.spacing4) {
                umItem(MGridSize.cells12, MGridSize.cells4){
                    css{
                        marginTop = 12.px
                    }
                    umEntityAvatar {}
                }

                umItem(MGridSize.cells12, MGridSize.cells8){

                    mTextField(label = "${firstNameLabel.text}",
                        helperText = firstNameLabel.errorText,
                        value = entity?.firstNames, error = firstNameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.firstNames = it.targetInputValue
                                firstNameError = null
                            }
                        }){
                        css(defaultFullWidth)
                    }


                    mTextField(label = "${lastNameLabel.text}",
                        value = entity?.lastName,
                        error = lastNameLabel.error, disabled = !fieldsEnabled,
                        helperText = lastNameLabel.errorText,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.lastName = it.targetInputValue
                                firstNameError = null
                            }
                        }){
                        css(defaultFullWidth)
                    }

                    umGridContainer(MGridSpacing.spacing4) {
                        umItem(MGridSize.cells12,MGridSize.cells6 ) {

                            mDateTimePicker(
                                label = "${dobLabel.text}",
                                ruleSet = defaultFullWidth,
                                error = dobLabel.error,
                                helperText = dobLabel.errorText,
                                value = entity?.dateOfBirth.toDate(),
                                inputVariant = MFormControlVariant.outlined,
                                pickerType = MDateTimePickerType.date,
                                onChange = { mills, utc ->
                                    setState {
                                        entity?.dateOfBirth = mills
                                        dateOfBirthError = null
                                    }
                                })

                        }

                        umItem(MGridSize.cells12,MGridSize.cells6 ) {
                            css(defaultMarginTop)
                            mFormControl(variant = MFormControlVariant.outlined) {
                                css(defaultFullWidth)
                                mInputLabel("${genderLabel.text}",
                                    htmlFor = "gender",
                                    variant = MFormControlVariant.outlined) {
                                    css(alignTextToStart)
                                }
                                mSelect("${entity?.gender ?: 0}",
                                    native = false,
                                    input = mOutlinedInput(name = "gender",
                                        id = "gender", addAsChild = false,
                                        labelWidth = genderLabel.width),
                                    onChange = { it, _ ->
                                        setState {
                                            entity?.gender = it.targetValue.toString().toInt()
                                        }
                                    }) {
                                    genderOptions?.forEach {
                                        mMenuItem(primaryText = it.toString(), value = it.messageId.toString()){
                                            css(alignTextToStart)
                                        }
                                    }
                                }
                                genderLabel.errorText?.let { error ->
                                    mFormHelperText(error){
                                        css(errorTextClass)
                                    }
                                }
                            }
                        }
                    }

                    mTextField(label = getString(MessageID.address),
                        value = entity?.personAddress,
                        disabled = !fieldsEnabled,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.personAddress = it.targetInputValue
                            }
                        }){
                        css(defaultFullWidth)
                    }

                    umGridContainer(MGridSpacing.spacing4) {
                        umItem(MGridSize.cells12,MGridSize.cells6 ) {
                            mTextField(label = getString(MessageID.phone_number),
                                value = entity?.phoneNum,
                                disabled = !fieldsEnabled,
                                variant = MFormControlVariant.outlined,
                                onChange = {
                                    it.persist()
                                    setState { entity?.phoneNum = it.targetInputValue}
                                }){
                                css(defaultFullWidth)
                            }
                        }

                        umItem(MGridSize.cells12,MGridSize.cells6 ) {
                            mTextField(label = "${emailLabel.text}",
                                value = entity?.emailAddr,
                                error = emailLabel.error,
                                disabled = !fieldsEnabled,
                                variant = MFormControlVariant.outlined,
                                onChange = {
                                    it.persist()
                                    setState {
                                        entity?.emailAddr = it.targetInputValue
                                        emailError = null
                                    }
                                }){
                                css(defaultFullWidth)
                            }
                        }
                    }


                    mTextField(autoFocus = false,label = "${usernameLabel.text}",
                        value = entity?.lastName,
                        error = usernameLabel.error,
                        disabled = !fieldsEnabled,
                        variant = MFormControlVariant.outlined,
                        onChange = {
                            it.persist()
                            setState {
                                entity?.username = it.targetInputValue
                                usernameError = null
                            }
                        }){
                        css{
                            +defaultFullWidth
                            display = displayRegField
                        }}

                    umGridContainer(MGridSpacing.spacing4) {
                        css{
                            display = displayRegField
                        }

                        umItem(MGridSize.cells12,MGridSize.cells6 ) {
                            css(defaultMarginTop)

                            mFormControl {
                                css(defaultFullWidth)
                                mInputLabel("${passwordLabel.text}",
                                    error = passwordLabel.error,
                                    variant = MFormControlVariant.outlined,
                                    htmlFor = "password")

                                mOutlinedInput(value = entity?.newPassword,
                                    labelWidth = passwordLabel.width,
                                    error = passwordLabel.error,
                                    id = "password",
                                    type = if(showPassword) InputType.text else InputType.password, onChange = {
                                        it.persist()
                                        setState {
                                            entity?.newPassword = it.targetInputValue
                                            passwordError = null
                                        }
                                    }) {
                                    attrs.endAdornment = mInputAdornment {
                                        mIconButton(if(showPassword) "visibility" else "visibility_off",
                                            edge = MIconEdge.end,
                                            onClick = {
                                                setState {
                                                    showPassword = !showPassword
                                                }
                                            }
                                        )
                                    }
                                }

                                passwordLabel.errorText?.let { error ->
                                    mFormHelperText(error){
                                        css(errorTextClass)
                                    }
                                }
                            }
                        }
                        umItem(MGridSize.cells12,MGridSize.cells6 ) {
                            css(defaultMarginTop)

                            mFormControl {
                                css(defaultFullWidth)
                                mInputLabel("${confirmPasswordLabel.text}",
                                    error = confirmPasswordLabel.error,
                                    variant = MFormControlVariant.outlined,
                                    htmlFor = "conf-password")

                                mOutlinedInput(value = entity?.currentPassword,
                                    labelWidth = confirmPasswordLabel.width,error = confirmPasswordLabel.error,
                                    id = "conf-password",
                                    type = if(showConfirmPassword) InputType.text else InputType.password, onChange = {
                                        it.persist()
                                        setState {
                                            entity?.confirmedPassword = it.targetInputValue
                                            passwordError = null
                                        }
                                    }) {
                                    attrs.endAdornment = mInputAdornment {
                                        mIconButton(if(showConfirmPassword) "visibility" else "visibility_off",
                                            edge = MIconEdge.end,
                                            onClick = {
                                                setState {
                                                    showConfirmPassword = !showConfirmPassword
                                                }
                                            }
                                        )
                                    }
                                }

                                confirmPasswordLabel.errorText?.let { error ->
                                    mFormHelperText(error){
                                        css(errorTextClass)
                                    }
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
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }
}