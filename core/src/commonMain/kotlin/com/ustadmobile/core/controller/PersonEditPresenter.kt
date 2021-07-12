package com.ustadmobile.core.controller

import com.ustadmobile.core.account.AccountRegisterOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.PersonEditView.Companion.REGISTER_MODE_MINOR
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.onDbThenRepoWithTimeout
import com.ustadmobile.lib.db.entities.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI
import org.kodein.di.instance


class PersonEditPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: PersonEditView,
    di: DI,
    lifecycleOwner: DoorLifecycleOwner
) : UstadEditPresenter<PersonEditView, PersonWithAccount>(
    context,
    arguments,
    view,
    di,
    lifecycleOwner,
    activeSessionRequired = !arguments.containsKey(PersonEditView.ARG_REGISTRATION_MODE)) {

    private lateinit var serverUrl: String

    private val impl: UstadMobileSystemImpl by instance()

    private lateinit var nextDestination: String

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    /**
     * The registration mode flags. This can include none, enabled (when registration is taking place),
     * and REGISTER_MODE_MINOR
     */
    private var registrationModeFlags: Int = PersonEditView.REGISTER_MODE_NONE

    private var loggedInPerson: Person? = null

    private var regViaLink: Boolean = false

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.genderOptions = listOf(MessageIdOption(MessageID.female, context, Person.GENDER_FEMALE),
                MessageIdOption(MessageID.male, context, Person.GENDER_MALE),
                MessageIdOption(MessageID.other, context, Person.GENDER_OTHER))

        registrationModeFlags = arguments[PersonEditView.ARG_REGISTRATION_MODE]?.toInt() ?: PersonEditView.REGISTER_MODE_NONE

        regViaLink = arguments[PersonEditView.REGISTER_VIA_LINK]?.toBoolean()?:false

        serverUrl = if (arguments.containsKey(UstadView.ARG_SERVER_URL)) {
            arguments.getValue(UstadView.ARG_SERVER_URL)
        } else {
            impl.getAppConfigString(AppConfig.KEY_API_URL, "http://localhost", context) ?: ""
        }

        nextDestination = arguments[UstadView.ARG_NEXT] ?: impl.getAppConfigString(
                AppConfig.KEY_FIRST_DEST, ContentEntryListTabsView.VIEW_NAME, context)
                ?: ContentEntryListTabsView.VIEW_NAME

        view.registrationMode = registrationModeFlags
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): PersonWithAccount {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val person = withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.personDao?.findPersonAccountByUid(entityUid)
        } ?: PersonWithAccount().also {
            it.dateOfBirth = arguments[PersonEditView.ARG_DATE_OF_BIRTH]?.toLong() ?: 0L
        }

        view.personPicture = db.onDbThenRepoWithTimeout(2000) { dbToUse, _ ->
            dbToUse.takeIf { entityUid != 0L }?.personPictureDao?.findByPersonUidAsync(entityUid)
        } ?: PersonPicture()

        if(registrationModeFlags.hasFlag(REGISTER_MODE_MINOR)) {
            view.approvalPersonParentJoin = PersonParentJoin()
        }

        val loggedInPersonUid = accountManager.activeAccount.personUid
        loggedInPerson = withTimeoutOrNull(2000){
            db.personDao.findByUidAsync(loggedInPersonUid)
        }


        return person
    }

    override fun onLoadFromJson(bundle: Map<String, String>): PersonWithAccount {
        super.onLoadFromJson(bundle)
        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        return if(entityJsonStr != null) {
            safeParse(di, PersonWithAccount.serializer(), entityJsonStr)
        } else {
            PersonWithAccount()
        }
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    private fun PersonEditView.hasErrors(): Boolean =
            usernameError != null ||
            passwordError != null ||
            confirmError != null ||
            dateOfBirthError != null ||
            noMatchPasswordError != null ||
            firstNamesFieldError != null ||
            lastNameFieldError != null ||
            genderFieldError != null ||
                    firstNameError != null ||
                    lastNameError != null ||
                    emailError != null ||
                    parentContactError != null

    override fun handleClickSave(entity: PersonWithAccount) {
        view.loading = true
        view.fieldsEnabled = false

        presenterScope.launch {
            //reset all errors
            view.usernameError = null
            view.passwordError = null
            view.emailError = null
            view.confirmError = null
            view.dateOfBirthError = null
            view.noMatchPasswordError = null
            view.parentContactError = null
            view.firstNamesFieldError = null
            view.lastNameFieldError = null
            view.genderFieldError = null

            val requiredFieldMessage = impl.getString(MessageID.field_required_prompt, context)
            val formatError = impl.getString(MessageID.invalid_email, context)

            view.takeIf { entity.firstNames.isNullOrEmpty() }?.firstNamesFieldError = requiredFieldMessage
            view.takeIf { entity.lastName.isNullOrEmpty() }?.lastNameFieldError = requiredFieldMessage
            view.takeIf { entity.gender == Person.GENDER_UNSET }?.genderFieldError = requiredFieldMessage
            if(entity.gender != Person.GENDER_UNSET){
                view.genderFieldError = null
            }
            view.firstNameError = null
            view.lastNameError = null

            //Email validation
            val email = entity.emailAddr?:""
            if(email.isNotEmpty() && !EMAIL_VALIDATION_REGEX.matches(email)){
                view.emailError = formatError
            }


            view.takeIf { entity.firstNames.isNullOrEmpty() }?.firstNameError = requiredFieldMessage
            view.takeIf { entity.lastName.isNullOrEmpty() }?.lastNameError = requiredFieldMessage

            if(view.hasErrors()) {
                view.loading = false
                view.fieldsEnabled = true
                return@launch
            }

            if(registrationModeFlags.hasFlag(PersonEditView.REGISTER_MODE_ENABLED)) {
                view.takeIf { entity.username.isNullOrEmpty() }?.usernameError = requiredFieldMessage
                view.takeIf { entity.newPassword.isNullOrEmpty() }?.passwordError = requiredFieldMessage
                view.takeIf { entity.confirmedPassword.isNullOrEmpty() }?.confirmError = requiredFieldMessage

                val parentEmailError = when {
                    !registrationModeFlags.hasFlag(REGISTER_MODE_MINOR) -> 0
                    view.approvalPersonParentJoin?.ppjEmail.isNullOrBlank() -> MessageID.field_required_prompt
                    view.approvalPersonParentJoin?.ppjEmail?.let { EMAIL_VALIDATION_REGEX.matches(it) } != true ->
                        MessageID.invalid_email
                    else -> 0
                }

                view.takeIf { parentEmailError != 0 }?.parentContactError = systemImpl.getString(
                    parentEmailError, context)

                view.takeIf { entity.dateOfBirth == 0L }?.dateOfBirthError = requiredFieldMessage
                view.takeIf { entity.confirmedPassword != entity.newPassword }?.noMatchPasswordError =
                        impl.getString(MessageID.filed_password_no_match, context)

                if(view.hasErrors()) {
                    view.loading = false
                    view.fieldsEnabled = true
                    return@launch
                }

                try {
                    accountManager.register(entity, serverUrl, AccountRegisterOptions(
                        makeAccountActive = !registrationModeFlags.hasFlag(REGISTER_MODE_MINOR),
                        parentJoin = view.approvalPersonParentJoin
                    ))

                    val popUpToViewName = arguments[UstadView.ARG_POPUPTO_ON_FINISH] ?: UstadView.CURRENT_DEST

                    if(registrationModeFlags.hasFlag(REGISTER_MODE_MINOR)) {
                        val goOptions = UstadMobileSystemCommon.UstadGoOptions(
                            RegisterAgeRedirectView.VIEW_NAME, true)
                        nextDestination = "RegisterMinorWaitForParent"
                        val args = mutableMapOf<String, String>().also {
                            it[RegisterMinorWaitForParentView.ARG_USERNAME] = entity.username ?: ""
                            it[RegisterMinorWaitForParentView.ARG_PARENT_CONTACT] =
                                view.approvalPersonParentJoin?.ppjEmail ?: ""
                            it[RegisterMinorWaitForParentView.ARG_PASSWORD] = entity.newPassword ?: ""
                            it.putFromOtherMapIfPresent(arguments, UstadView.ARG_POPUPTO_ON_FINISH)
                        }

                        impl.go(RegisterMinorWaitForParentView.VIEW_NAME, args, context, goOptions)
                    }else {
                        val goOptions = UstadMobileSystemCommon.UstadGoOptions(
                            popUpToViewName, true)
                        impl.go(nextDestination, mapOf(), context, goOptions)
                    }
                } catch (e: Exception) {
                    if (e is IllegalStateException) {
                        view.usernameError = impl.getString(MessageID.person_exists, context)
                    } else {
                        view.showSnackBar(impl.getString(MessageID.login_network_error, context))
                    }

                    return@launch
                }finally {
                    view.loading = false
                    view.fieldsEnabled = true
                }
            } else {
                //Create/Update person group
                if(entity.personUid == 0L) {
                    val personWithGroup = repo.insertPersonAndGroup(entity)
                    entity.personGroupUid = personWithGroup.personGroupUid
                    entity.personUid = personWithGroup.personUid
                }else {
                    repo.personDao.updateAsync(entity)
                }

                val personPictureVal = view.personPicture
                if(personPictureVal != null) {
                    personPictureVal.personPicturePersonUid = entity.personUid

                    if(personPictureVal.personPictureUid == 0L) {
                        repo.personPictureDao.insertAsync(personPictureVal)
                    }else {
                        repo.personPictureDao.updateAsync(personPictureVal)
                    }
                }

                //Handle the following scenario: ClazzMemberList (user selects to add a student to enrol),
                // PersonList, PersonEdit, EnrolmentEdit
                if(arguments.containsKey(UstadView.ARG_GO_TO_COMPLETE)) {
                    systemImpl.go(arguments[UstadView.ARG_GO_TO_COMPLETE].toString(),
                            arguments.plus(UstadView.ARG_PERSON_UID to entity.personUid.toString()),
                            context)
                }else{
                    onFinish(PersonDetailView.VIEW_NAME, entity.personUid, entity)
                }
            }
        }
    }

    companion object {

        @Suppress("RegExpRedundantEscape")
        val EMAIL_VALIDATION_REGEX: Regex by lazy(LazyThreadSafetyMode.NONE) {
            Regex("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$")
        }
    }
}