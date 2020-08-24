package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.ext.setAttachmentDataFromUri
import com.ustadmobile.core.view.ContentEntryListTabsView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getSystemTimeInMillis
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

    private  lateinit var nextDestination: String

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private var registrationMode: Boolean = false

    private val clazzMemberJoinEditHelper =
            DefaultOneToManyJoinEditHelper(ClazzMemberWithClazz::clazzMemberUid,
            "state_ClazzMemberWithClazz_list", ClazzMemberWithClazz.serializer().list,
            ClazzMemberWithClazz.serializer().list, this) { clazzMemberUid = it }

    fun handleAddOrEditClazzMemberWithClazz(clazzMemberWithClazz: ClazzMemberWithClazz) {
        clazzMemberJoinEditHelper.onEditResult(clazzMemberWithClazz)
    }

    fun handleClickRemovePersonFromClazz(clazzMemberWithClazz: ClazzMemberWithClazz) {
        clazzMemberJoinEditHelper.onDeactivateEntity(clazzMemberWithClazz)
    }

    private val rolesAndPermissionEditHelper = DefaultOneToManyJoinEditHelper<EntityRoleWithNameAndRole>(
            EntityRoleWithNameAndRole::erUid,
            "state_EntityRoleWithNameAndRole_list", EntityRoleWithNameAndRole.serializer().list,
            EntityRoleWithNameAndRole.serializer().list, this) { erUid = it }

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

        serverUrl = if (arguments.containsKey(UstadView.ARG_SERVER_URL)) {
            arguments.getValue(UstadView.ARG_SERVER_URL)
        } else {
            impl.getAppConfigString(AppConfig.KEY_API_URL, "http://localhost", context)?:""
        }

        nextDestination = arguments[UstadView.ARG_NEXT] ?: impl.getAppConfigString(
                AppConfig.KEY_FIRST_DEST, ContentEntryListTabsView.VIEW_NAME, context) ?:
                ContentEntryListTabsView.VIEW_NAME

        view.registrationMode = registrationMode
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): PersonWithAccount? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val person = withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.personDao?.findPersonAccountByUid(entityUid)
        } ?: PersonWithAccount()

        val personPicture = withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.personPictureDao?.findByPersonUidAsync(entityUid)
        }

        if(personPicture != null){
            view.personPicturePath = repo.personPictureDao.getAttachmentPath(personPicture)
        }

        val clazzMemberWithClazzList = withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.clazzMemberDao?.findAllClazzesByPersonWithClazzAsList(entityUid, getSystemTimeInMillis())
        } ?: listOf()
        clazzMemberJoinEditHelper.liveList.sendValue(clazzMemberWithClazzList)


        val rolesAndPermissionList = withTimeoutOrNull(2000){
            db.takeIf{entityUid != 0L}?.entityRoleDao?.filterByPersonWithExtraAsList(
                    entity?.personGroupUid?:0L)
        }?:listOf()
        rolesAndPermissionEditHelper.liveList.sendValue(rolesAndPermissionList)


        return person
    }

    override fun onLoadFromJson(bundle: Map<String, String>): PersonWithAccount? {
        super.onLoadFromJson(bundle)
        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: Person? = null
        editEntity = if(entityJsonStr != null) {
            Json.parse(PersonWithAccount.serializer(), entityJsonStr)
        }else {
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

    override fun handleClickSave(entity: PersonWithAccount) {
        GlobalScope.launch(doorMainDispatcher()) {
            val noPasswordMatch = entity.newPassword != entity.confirmedPassword
                    && !entity.newPassword.isNullOrEmpty() &&  !entity.confirmedPassword.isNullOrEmpty()
            if(registrationMode && (entity.username.isNullOrEmpty()
                            || entity.newPassword.isNullOrEmpty() || entity.confirmedPassword.isNullOrEmpty()
                            || noPasswordMatch)){
                val requiredFieldMessage = impl.getString(MessageID.field_required_prompt, context)
                view.usernameError = if(entity.username.isNullOrEmpty()) requiredFieldMessage else null
                view.passwordError = if(entity.newPassword.isNullOrEmpty()) requiredFieldMessage else null
                view.confirmError = if(entity.confirmedPassword.isNullOrEmpty()) requiredFieldMessage else null
                view.noMatchPasswordError = if(noPasswordMatch)
                    impl.getString(MessageID.filed_password_no_match, context) else null
                return@launch
            }

            if(registrationMode){
                val password = entity.newPassword
                if(password != null){
                   try{
                       val umAccount = accountManager.register(entity, serverUrl)
                       accountManager.activeAccount = umAccount
                       view.navigateToNextDestination(umAccount,nextDestination)
                   }catch (e:Exception){
                       view.errorMessage = impl.getString(if(e is IllegalArgumentException) MessageID.person_exists
                       else MessageID.login_network_error , context)
                       return@launch
                   }
                }
            }else{
                if(entity.personUid == 0L) {

                    val personWithGroup = repo.insertPersonAndGroup(entity)
                    entity.personGroupUid = personWithGroup.personGroupUid
                    entity.personUid = personWithGroup.personUid

                }else {
                    repo.personDao.updateAsync(entity)
                }

                val loggedInPersonUid = accountManager.activeAccount.personUid
                val loggedInPerson = withTimeoutOrNull(2000){
                    db.personDao.findByUidAsync(loggedInPersonUid)
                }
                view.isAdmin = loggedInPerson?.admin?:false

                repo.entityRoleDao.insertListAsync(rolesAndPermissionEditHelper.entitiesToInsert.also { it.forEach {
                    it.erGroupUid = entity.personGroupUid
                }  })
                repo.entityRoleDao.updateListAsync(rolesAndPermissionEditHelper.entitiesToUpdate.also { it.forEach{
                    it.erGroupUid = entity.personGroupUid
                } })

                repo.entityRoleDao.deactivateByUids(rolesAndPermissionEditHelper.primaryKeysToDeactivate)



                clazzMemberJoinEditHelper.entitiesToInsert.forEach {
                    repo.enrolPersonIntoClazzAtLocalTimezone(entity, it.clazzMemberClazzUid,
                            it.clazzMemberRole)
                }
                repo.clazzMemberDao.updateDateLeft(clazzMemberJoinEditHelper.primaryKeysToDeactivate,
                        getSystemTimeInMillis())

                var personPicture = db.personPictureDao.findByPersonUidAsync(entity.personUid)
                val viewPicturePath = view.personPicturePath
                val currentPath = if(personPicture != null)
                    repo.personPictureDao.getAttachmentPath(personPicture) else null

                if(personPicture != null && viewPicturePath != null && currentPath != viewPicturePath) {
                    repo.personPictureDao.setAttachment(personPicture, viewPicturePath)
                    repo.personPictureDao.update(personPicture)
                }else if(viewPicturePath != null && currentPath != viewPicturePath) {
                    personPicture = PersonPicture().apply {
                        personPicturePersonUid = entity.personUid
                    }
                    personPicture.personPictureUid = repo.personPictureDao.insert(personPicture)
                    repo.personPictureDao.setAttachment(personPicture, viewPicturePath)
                }else if(personPicture != null && currentPath != null && viewPicturePath == null) {
                    //picture has been removed
                    personPicture.personPictureActive = false
                    repo.personPictureDao.setAttachmentDataFromUri(personPicture, null, context)
                    repo.personPictureDao.update(personPicture)
                }

                onFinish(PersonDetailView.VIEW_NAME, entity.personUid, entity)
            }
        }
    }
}