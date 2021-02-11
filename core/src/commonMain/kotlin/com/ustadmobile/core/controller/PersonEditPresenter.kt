package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.soywiz.klock.until
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.schedule.age
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.ContentEntryListTabsView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.onDbThenRepoWithTimeout
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getSystemTimeInMillis
import io.ktor.client.features.json.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance


class PersonEditPresenter(context: Any,
                          arguments: Map<String, String>, view: PersonEditView, di: DI,
                          lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<PersonEditView, PersonWithAccount>(context, arguments, view, di, lifecycleOwner) {

    private lateinit var serverUrl: String

    private val impl: UstadMobileSystemImpl by instance()

    private lateinit var nextDestination: String

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private var registrationMode: Boolean = false

    private var loggedInPerson: Person? = null

    private var regViaLink: Boolean = false

    private val clazzMemberJoinEditHelper =
            DefaultOneToManyJoinEditHelper(ClazzMemberWithClazz::clazzMemberUid,
            "state_ClazzMemberWithClazz_list", ClazzMemberWithClazz.serializer().list,
            ClazzMemberWithClazz.serializer().list, this, ClazzMemberWithClazz::class) { clazzMemberUid = it }

    fun handleAddOrEditClazzMemberWithClazz(clazzMemberWithClazz: ClazzMemberWithClazz) {
        clazzMemberJoinEditHelper.onEditResult(clazzMemberWithClazz)
    }

    fun handleClickRemovePersonFromClazz(clazzMemberWithClazz: ClazzMemberWithClazz) {
        clazzMemberJoinEditHelper.onDeactivateEntity(clazzMemberWithClazz)
    }

    private val rolesAndPermissionEditHelper = DefaultOneToManyJoinEditHelper<EntityRoleWithNameAndRole>(
            EntityRoleWithNameAndRole::erUid,
            "state_EntityRoleWithNameAndRole_list", EntityRoleWithNameAndRole.serializer().list,
            EntityRoleWithNameAndRole.serializer().list, this, EntityRoleWithNameAndRole::class) { erUid = it }

    fun handleAddOrEditRoleAndPermission(entityRoleWithNameAndRole: EntityRoleWithNameAndRole) {
        rolesAndPermissionEditHelper.onEditResult(entityRoleWithNameAndRole)
    }

    fun handleRemoveRoleAndPermission(entityRoleWithNameAndRole: EntityRoleWithNameAndRole) {
        rolesAndPermissionEditHelper.onDeactivateEntity(entityRoleWithNameAndRole)
    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.genderOptions = listOf(MessageIdOption(MessageID.female, context, Person.GENDER_FEMALE),
                MessageIdOption(MessageID.male, context, Person.GENDER_MALE),
                MessageIdOption(MessageID.other, context, Person.GENDER_OTHER))
        view.clazzList = clazzMemberJoinEditHelper.liveList

        view.rolesAndPermissionsList = rolesAndPermissionEditHelper.liveList

        registrationMode = arguments[PersonEditView.ARG_REGISTRATION_MODE]?.toBoolean()?:false
        regViaLink = arguments[PersonEditView.REGISTER_VIA_LINK]?.toBoolean()?:false

        serverUrl = if (arguments.containsKey(UstadView.ARG_SERVER_URL)) {
            arguments.getValue(UstadView.ARG_SERVER_URL)
        } else {
            impl.getAppConfigString(AppConfig.KEY_API_URL, "http://localhost", context) ?: ""
        }

        nextDestination = arguments[UstadView.ARG_NEXT] ?: impl.getAppConfigString(
                AppConfig.KEY_FIRST_DEST, ContentEntryListTabsView.VIEW_NAME, context)
                ?: ContentEntryListTabsView.VIEW_NAME

        view.registrationMode = registrationMode

    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): PersonWithAccount? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val person = withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.personDao?.findPersonAccountByUid(entityUid)
        } ?: PersonWithAccount()

        view.personPicture = db.onDbThenRepoWithTimeout(2000) { dbToUse, _ ->
            dbToUse.takeIf { entityUid != 0L }?.personPictureDao?.findByPersonUidAsync(entityUid)
        } ?: PersonPicture()

        val clazzMemberWithClazzList = withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.clazzMemberDao?.findAllClazzesByPersonWithClazzAsListAsync(entityUid, getSystemTimeInMillis())
        } ?: listOf()
        clazzMemberJoinEditHelper.liveList.sendValue(clazzMemberWithClazzList)


        val rolesAndPermissionList = withTimeoutOrNull(2000){
            db.takeIf{entityUid != 0L}?.entityRoleDao?.filterByPersonWithExtraAsList(
                    entity?.personGroupUid?:0L)
        }?:listOf()
        rolesAndPermissionEditHelper.liveList.sendValue(rolesAndPermissionList)

        val loggedInPersonUid = accountManager.activeAccount.personUid
        loggedInPerson = withTimeoutOrNull(2000){
            db.personDao.findByUidAsync(loggedInPersonUid)
        }

        val canDelegate = if(loggedInPersonUid != 0L) {
            repo.personDao.personHasPermissionAsync(loggedInPersonUid?: 0,
                    arguments[ARG_ENTITY_UID]?.toLong() ?: 0L,
                    Role.PERMISSION_PERSON_DELEGATE, checkPermissionForSelf = 1)
        }else {
            false
        }

        if(loggedInPerson != null && loggedInPerson?.admin == false){
            view.canDelegatePermissions = canDelegate
        }else {
            view.canDelegatePermissions = loggedInPerson != null && loggedInPerson?.admin == true
        }

        return person
    }

    override fun onLoadFromJson(bundle: Map<String, String>): PersonWithAccount? {
        super.onLoadFromJson(bundle)
        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: Person? = null
        editEntity = if (entityJsonStr != null) {
            safeParse(di, PersonWithAccount.serializer(), entityJsonStr)
        } else {
            PersonWithAccount()
        }

        return editEntity
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
            noMatchPasswordError != null

    override fun handleClickSave(entity: PersonWithAccount) {
        view.loading = true
        view.fieldsEnabled = false
        GlobalScope.launch(doorMainDispatcher()) {
            //reset all errors
            view.usernameError = null
            view.passwordError = null
            view.confirmError = null
            view.dateOfBirthError = null
            view.noMatchPasswordError = null

            if(registrationMode) {
                val requiredFieldMessage = impl.getString(MessageID.field_required_prompt, context)

                view.takeIf { entity.username.isNullOrEmpty() }?.usernameError = requiredFieldMessage
                view.takeIf { entity.newPassword.isNullOrEmpty() }?.passwordError = requiredFieldMessage
                view.takeIf { entity.confirmedPassword.isNullOrEmpty() }?.confirmError = requiredFieldMessage

                view.takeIf { entity.dateOfBirth == 0L }?.dateOfBirthError = requiredFieldMessage
                view.takeIf { !regViaLink && DateTime(entity.dateOfBirth).age() < 13 }?.dateOfBirthError =
                        impl.getString(MessageID.underRegistrationAgeError, context)
                view.takeIf { entity.confirmedPassword != entity.newPassword }?.noMatchPasswordError =
                        impl.getString(MessageID.filed_password_no_match, context)

                if(view.hasErrors()) {
                    view.loading = false
                    view.fieldsEnabled = true
                    return@launch
                }

                try {
                    val umAccount = accountManager.register(entity, serverUrl)
                    accountManager.activeAccount = umAccount
                    val goOptions = UstadMobileSystemCommon.UstadGoOptions(
                            arguments[UstadView.ARG_POPUPTO_ON_FINISH] ?: UstadView.CURRENT_DEST,
                            true)
                    impl.go(nextDestination, mapOf(), context, goOptions)
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
                    val personWithGroup = repo.insertPersonAndGroup(entity, loggedInPerson)
                    entity.personGroupUid = personWithGroup.personGroupUid
                    entity.personUid = personWithGroup.personUid
                }else {
                    repo.personDao.updateAsync(entity)
                }

                //Insert any roles and permissions
                repo.entityRoleDao.insertListAsync(
                        rolesAndPermissionEditHelper.entitiesToInsert.also {
                            it.forEach {
                                it.erUid = 0
                                it.erGroupUid = entity.personGroupUid
                                it.erActive = true
                            }
                        }
                )
                //Update any roles and permissions
                repo.entityRoleDao.updateListAsync(
                        rolesAndPermissionEditHelper.entitiesToUpdate.also {
                            it.forEach{
                                it.erGroupUid = entity.personGroupUid
                            }
                        }
                )

                //Remove any roles and permissions
                repo.entityRoleDao.deactivateByUids(
                        rolesAndPermissionEditHelper.primaryKeysToDeactivate)

                //Insert any Clazz enrollments
                clazzMemberJoinEditHelper.entitiesToInsert.forEach {
                    repo.enrolPersonIntoClazzAtLocalTimezone(entity, it.clazzMemberClazzUid,
                            it.clazzMemberRole)
                }
                //Update any clazz enrollments
                repo.clazzMemberDao.updateDateLeft(clazzMemberJoinEditHelper.primaryKeysToDeactivate,
                        getSystemTimeInMillis())


                val personPictureVal = view.personPicture
                if(personPictureVal != null) {
                    personPictureVal.personPicturePersonUid = entity.personUid

                    if(personPictureVal.personPictureUid == 0L) {
                        repo.personPictureDao.insertAsync(personPictureVal)
                    }else {
                        repo.personPictureDao.updateAsync(personPictureVal)
                    }
                }

                onFinish(PersonDetailView.VIEW_NAME, entity.personUid, entity)
            }
        }
    }
}