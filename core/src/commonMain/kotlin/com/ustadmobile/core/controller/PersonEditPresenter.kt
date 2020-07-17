package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.ext.setAttachmentDataFromUri
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazz
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.WorkSpace
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
    : UstadEditPresenter<PersonEditView, Person>(context, arguments, view, di, lifecycleOwner) {

    private lateinit var serverUrl: String

    private val impl: UstadMobileSystemImpl by instance()

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private var workSpace: WorkSpace = WorkSpace().apply {
        registrationAllowed = false
    }

    private val clazzMemberJoinEditHelper = DefaultOneToManyJoinEditHelper(ClazzMemberWithClazz::clazzMemberUid,
            "state_ClazzMemberWithClazz_list", ClazzMemberWithClazz.serializer().list,
            ClazzMemberWithClazz.serializer().list, this) { clazzMemberUid = it }

    fun handleAddOrEditClazzMemberWithClazz(clazzMemberWithClazz: ClazzMemberWithClazz) {
        clazzMemberJoinEditHelper.onEditResult(clazzMemberWithClazz)
    }

    fun handleClickRemovePersonFromClazz(clazzMemberWithClazz: ClazzMemberWithClazz) {
        clazzMemberJoinEditHelper.onDeactivateEntity(clazzMemberWithClazz)
    }


    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.genderOptions = listOf(MessageIdOption(MessageID.female, context, Person.GENDER_FEMALE),
                MessageIdOption(MessageID.male, context, Person.GENDER_MALE),
                MessageIdOption(MessageID.other, context, Person.GENDER_OTHER))
        view.clazzList = clazzMemberJoinEditHelper.liveList
        val mWorkSpace = arguments[UstadView.ARG_WORKSPACE]
        if(mWorkSpace != null){
            workSpace = Json.parse(WorkSpace.serializer(), mWorkSpace)
        }

        serverUrl = if (arguments.containsKey(UstadView.ARG_SERVER_URL)) {
            arguments.getValue(UstadView.ARG_SERVER_URL)
        } else {
            impl.getAppConfigString(AppConfig.KEY_API_URL, "http://localhost", context)?:""
        }
        view.classVisible = workSpace.registrationAllowed
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Person? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val person = withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.personDao?.findByUid(entityUid)
        } ?: Person()

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


        return person
    }

    override fun onLoadFromJson(bundle: Map<String, String>): Person? {
        super.onLoadFromJson(bundle)
        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: Person? = null
        editEntity = if(entityJsonStr != null) {
            Json.parse(Person.serializer(), entityJsonStr)
        }else {
            Person()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: Person) {
        GlobalScope.launch(doorMainDispatcher()) {
            val noPasswordMatch = view.password != view.confirmedPassword
                    && !view.password.isNullOrEmpty() &&  !view.confirmedPassword.isNullOrEmpty()
            if(workSpace.registrationAllowed && (entity.username.isNullOrEmpty()
                            || view.password.isNullOrEmpty() || view.confirmedPassword.isNullOrEmpty()
                            || noPasswordMatch)){

                view.usernameRequiredErrorVisible = entity.username.isNullOrEmpty()
                view.passwordRequiredErrorVisible = view.password.isNullOrEmpty()
                view.confirmPasswordErrorVisible = view.confirmedPassword.isNullOrEmpty()
                view.noMatchPasswordErrorVisible = noPasswordMatch
                return@launch
            }

            if(workSpace.registrationAllowed){
                val password = view.password
                if(password != null){
                   try{
                       val umAccount = accountManager.register(entity, password, serverUrl)
                   }catch (e:Exception){
                       view.errorMessage = impl.getString(if(e is IllegalArgumentException) MessageID.person_exists
                       else MessageID.login_network_error , context)
                       return@launch
                   }
                }
            }else{
                if(entity.personUid == 0L) {
                    entity.personUid = repo.personDao.insertAsync(entity)
                }else {
                    repo.personDao.updateAsync(entity)
                }

                clazzMemberJoinEditHelper.entitiesToInsert.forEach {
                    repo.enrolPersonIntoClazzAtLocalTimezone(entity, it.clazzMemberClazzUid,
                            it.clazzMemberRole)
                }
                repo.clazzMemberDao.updateDateLeft(clazzMemberJoinEditHelper.primaryKeysToDeactivate,
                        getSystemTimeInMillis())

                var personPicture = db.personPictureDao.findByPersonUidAsync(entity.personUid)
                val viewPicturePath = view.personPicturePath
                val currentPath = if(personPicture != null) repo.personPictureDao.getAttachmentPath(personPicture) else null

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
            }

            view.finishWithResult(listOf(entity))
        }
    }

}