package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MIconEdge
import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ccfraser.muirwik.components.form.mFormControl
import com.ccfraser.muirwik.components.form.mFormHelperText
import com.ccfraser.muirwik.components.input.mInputAdornment
import com.ccfraser.muirwik.components.input.mInputLabel
import com.ccfraser.muirwik.components.input.mOutlinedInput
import com.ccfraser.muirwik.components.menu.mMenuItem
import com.ustadmobile.core.controller.PersonEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.model.UmLabel
import com.ustadmobile.util.CssStyleManager.alignTextToStart
import com.ustadmobile.util.CssStyleManager.defaultFullWidth
import com.ustadmobile.util.CssStyleManager.defaultMarginTop
import com.ustadmobile.util.CssStyleManager.helperText
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umProfileAvatar
import kotlinx.css.Display
import kotlinx.css.display
import kotlinx.html.InputType
import react.RBuilder
import react.RProps
import react.setState
import styled.css

class PersonEditComponent(mProps: RProps) : UstadEditComponent<PersonWithAccount>(mProps), PersonEditView {

    private lateinit var mPresenter: PersonEditPresenter

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
            console.log(value)
            setState{field = value}
        }

    override var personPicturePath: String? = null
        get() = field
        set(value) {
            setState{field = value}
        }

    override var personPicture: PersonPicture? = null
        get() = field
        set(value) {
            setState{field = value}
        }

    override var approvalPersonParentJoin: PersonParentJoin? = null
        get() = field
        set(value) {
            setState{field = value}
        }

    override var clazzList: DoorLiveData<List<ClazzEnrolmentWithClazz>>? = null
        get() = field
        set(value) {
            setState{field = value}
        }

    override var rolesAndPermissionsList: DoorLiveData<List<EntityRoleWithNameAndRole>>? = null
        get() = field
        set(value) {
            setState{field = value}
        }

    override var registrationMode: Int = 0
        get() = field
        set(value) {
            setState { displayRegField = if(value == PersonEditView.REGISTER_MODE_ENABLED)
                Display.flex else Display.none}
            field = value
        }

    private var usernameLabel = UmLabel(getString(MessageID.username))

    override var usernameError: String? = null
        get() = field
        set(value) {
            setState{
                usernameLabel = usernameLabel.copy(caption =  value)
            }
            field = value
        }

    override var noMatchPasswordError: String? = null
    get() = field
    set(value) {
        field = value
        setState{confirmPasswordLabel = confirmPasswordLabel.copy(caption = value)}
    }

    private var passwordLabel = UmLabel(getString(MessageID.password))

    override var passwordError: String?  = null
        get() = field
        set(value) {
            setState{passwordLabel = passwordLabel.copy(caption = value)}
            field = value
        }

    private var emailLabel = UmLabel(getString(MessageID.email))

    override var emailError: String?  = null
        get() = field
        set(value) {
            setState{emailLabel = emailLabel.copy(caption = value)}
            field = value
        }

    private var confirmPasswordLabel = UmLabel(getString(MessageID.confirm_password))
    
    override var confirmError: String?  = null
        get() = field
        set(value) {
            setState{confirmPasswordLabel = confirmPasswordLabel.copy(caption = value)}
            field = value
        }

    private var dobLabel = UmLabel(getString(MessageID.birthday))

    override var dateOfBirthError: String? = null
        set(value) {
            setState { dobLabel = dobLabel.copy(caption = value ) }
            field = value
        }

    override var canDelegatePermissions: Boolean? = null
        get() = field
        set(value) {
            setState { field = value }
        }

    override var parentContactError: String?  = null
        get() = field
        set(value) {
            field = value
        }

    private var firstNameLabel = UmLabel(text = getString(MessageID.first_name))

    override var firstNamesFieldError: String? = null
        set(value) {
            field = value
            setState { firstNameLabel = firstNameLabel.copy(caption = value) }
        }

    override var lastNameFieldError: String?  = null
        set(value) {
            setState{}
            field = value
        }

    private var genderLabel = UmLabel(getString(MessageID.field_person_gender))

    override var genderFieldError: String?  = null
        set(value) {
            setState{genderLabel = genderLabel.copy(caption = value)}
            field = value
        }

    override var firstNameError: String?  = null
        set(value) {
            setState{}
            field = value
        }

    private var lastNameLabel = UmLabel(getString(MessageID.last_name))

    override var lastNameError: String?  = null
        set(value) {
            setState{lastNameLabel = lastNameLabel.copy(caption = value)}
            field = value
        }

    override fun navigateToNextDestination(account: UmAccount?, nextDestination: String) {
        systemImpl.go(nextDestination, mapOf(), this)
    }

    override var fieldsEnabled: Boolean = true
        get() = field
        set(value) {
            setState { field = value }}

    override var entity: PersonWithAccount? = null
        get() = field
        set(value) {
            setState { field = value }
        }

    override fun onComponentReady() {
        super.onComponentReady()
        setState { loading = false }
        if(registrationMode.hasFlag(PersonEditView.REGISTER_MODE_ENABLED)) {
            setState { title = getString(MessageID.register) }
        }else {
            setEditTitle(MessageID.add_a_new_person, MessageID.edit_person)
        }
        mPresenter = PersonEditPresenter(this, getArgs(),this, di, this)
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.render() {

        umGridContainer(MGridSpacing.spacing4) {
            umItem(MGridSize.cells12, MGridSize.cells4){
                umProfileAvatar {

                }
            }

            umItem(MGridSize.cells12, MGridSize.cells8){

                mTextField(label = "${firstNameLabel.text}", helperText = firstNameLabel.caption,
                    value = entity?.firstNames, error = firstNameLabel.error, disabled = !fieldsEnabled,
                    variant = MFormControlVariant.outlined, onChange = {
                        it.persist()
                        setState {
                            entity?.firstNames = it.targetInputValue
                            firstNameError = null }
                    }){css(defaultFullWidth)}


                mTextField(label = "${lastNameLabel.text}", value = entity?.lastName,
                    error = lastNameLabel.error, disabled = !fieldsEnabled, helperText = lastNameLabel.caption,
                    variant = MFormControlVariant.outlined, onChange = {
                        it.persist()
                        setState {
                            entity?.lastName = it.targetInputValue
                            firstNameError = null }
                    }){css(defaultFullWidth)}

                umGridContainer(MGridSpacing.spacing4) {
                    umItem(MGridSize.cells12,MGridSize.cells6 ) {
                        mTextField(label = "${dobLabel.text}", value = entity?.lastName,
                            error = dobLabel.error, disabled = !fieldsEnabled, helperText = dobLabel.caption,
                            variant = MFormControlVariant.outlined, onChange = {
                                it.persist()
                                setState {
                                    entity?.lastName = it.targetInputValue
                                    dateOfBirthError = null }
                            }){css(defaultFullWidth)}
                    }

                    umItem(MGridSize.cells12,MGridSize.cells6 ) {
                        css(defaultMarginTop)
                        mFormControl(variant = MFormControlVariant.outlined) {
                            css(defaultFullWidth)
                            mInputLabel("${genderLabel.text}", htmlFor = "gender", variant = MFormControlVariant.outlined) {
                                css(alignTextToStart)
                            }
                            mSelect(entity?.gender, native = false,
                                input = mOutlinedInput(name = "gender", id = "gender", addAsChild = false,
                                    labelWidth = genderLabel.width), onChange = { it, _ ->
                                    setState { entity?.gender = it.targetValue.toString().toInt() }
                                }) {
                                genderOptions?.forEach {
                                    mMenuItem(primaryText = it.toString(), value = it.messageId.toString()){
                                        css(alignTextToStart)
                                    }
                                }
                            }
                            genderLabel.caption?.let { mFormHelperText(it){ css(helperText) }
                            }
                        }
                    }
                }

                mTextField(label = getString(MessageID.address), value = entity?.personAddress,
                    disabled = !fieldsEnabled, variant = MFormControlVariant.outlined, onChange = {
                        it.persist()
                        setState { entity?.personAddress = it.targetInputValue}
                    }){css(defaultFullWidth)}

                umGridContainer(MGridSpacing.spacing4) {
                    umItem(MGridSize.cells12,MGridSize.cells6 ) {
                        mTextField(label = "${getString(MessageID.phone_number)}", value = entity?.phoneNum,
                            disabled = !fieldsEnabled,
                            variant = MFormControlVariant.outlined, onChange = {
                                it.persist()
                                setState { entity?.phoneNum = it.targetInputValue}
                            }){css(defaultFullWidth)}
                    }

                    umItem(MGridSize.cells12,MGridSize.cells6 ) {
                        mTextField(label = "${emailLabel.text}", value = entity?.emailAddr,
                            error = emailLabel.error, disabled = !fieldsEnabled,
                            variant = MFormControlVariant.outlined, onChange = {
                                it.persist()
                                setState {
                                    entity?.emailAddr = it.targetInputValue
                                    emailError = null }
                            }){css(defaultFullWidth)}
                    }
                }


                mTextField(autoFocus = false,label = "${usernameLabel.text}", value = entity?.lastName,
                    error = usernameLabel.error, disabled = !fieldsEnabled,
                    variant = MFormControlVariant.outlined, onChange = {
                        it.persist()
                        setState {
                            entity?.username = it.targetInputValue
                            usernameError = null }
                    }){
                    css{
                        +defaultFullWidth
                        css{display = displayRegField}
                    }}

                umGridContainer(MGridSpacing.spacing4) {
                    css{display = displayRegField}
                    umItem(MGridSize.cells12,MGridSize.cells6 ) {
                        css{+defaultMarginTop}
                        mFormControl {
                            css(defaultFullWidth)
                            mInputLabel("${passwordLabel.text}", error = passwordLabel.error,
                                variant = MFormControlVariant.outlined, htmlFor = "password")
                            mOutlinedInput(value = entity?.newPassword,
                                labelWidth = passwordLabel.width,error = passwordLabel.error,
                                id = "password",type = if(showPassword) InputType.text else InputType.password, onChange = {
                                    it.persist()
                                    setState {
                                        entity?.newPassword = it.targetInputValue
                                        passwordError = null} }) {
                                attrs{
                                    endAdornment = mInputAdornment {
                                        mIconButton(if(showPassword) "visibility" else "visibility_off",edge = MIconEdge.end, onClick = {
                                            setState { showPassword = !showPassword }
                                        })
                                    }
                                }
                            }

                            passwordLabel.caption?.let { mFormHelperText(it){css(helperText)} }
                        }
                    }
                    umItem(MGridSize.cells12,MGridSize.cells6 ) {
                        css{+defaultMarginTop}
                        mFormControl {
                            css(defaultFullWidth)
                            mInputLabel("${confirmPasswordLabel.text}", error = confirmPasswordLabel.error,
                                variant = MFormControlVariant.outlined, htmlFor = "conf-password")
                            mOutlinedInput(value = entity?.currentPassword,
                                labelWidth = confirmPasswordLabel.width,error = confirmPasswordLabel.error,
                                id = "conf-password",type = if(showConfirmPassword) InputType.text else InputType.password, onChange = {
                                    it.persist()
                                    setState {
                                        entity?.confirmedPassword = it.targetInputValue
                                        passwordError = null} }) {
                                attrs{
                                    endAdornment = mInputAdornment {
                                        mIconButton(if(showConfirmPassword) "visibility" else "visibility_off",edge = MIconEdge.end, onClick = {
                                            setState { showConfirmPassword = !showConfirmPassword }
                                        })
                                    }
                                }
                            }

                            confirmPasswordLabel.caption?.let { mFormHelperText(it){css(helperText)} }
                        }
                    }
                }

            }

        }
    }
}