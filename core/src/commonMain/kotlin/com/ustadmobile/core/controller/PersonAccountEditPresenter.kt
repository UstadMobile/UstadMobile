package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.PersonAccountEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance


class PersonAccountEditPresenter(context: Any,
                                 arguments: Map<String, String>, view: PersonAccountEditView, di: DI,
                                 lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<PersonAccountEditView, PersonWithAccount>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private lateinit var serverUrl: String

    private val impl: UstadMobileSystemImpl by instance()

    private var createAccount: Boolean = false

    private var isActiveUserAdmin: Boolean = false

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        serverUrl = arguments[ARG_SERVER_URL]?:accountManager.activeAccount.endpointUrl
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): PersonWithAccount? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val activePersonUid = accountManager.activeAccount.personUid

        val activePerson = withTimeoutOrNull(2000) {
            db.personDao.findByUid(activePersonUid)
        } ?: Person()

        isActiveUserAdmin = activePerson.admin
        view.currentPasswordVisible = !isActiveUserAdmin

        val person = withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.personDao?.findPersonAccountByUid(entityUid)
        } ?: PersonWithAccount()
        createAccount = person.username.isNullOrEmpty()
        return person
    }

    override fun onLoadFromJson(bundle: Map<String, String>): PersonWithAccount? {
        super.onLoadFromJson(bundle)
        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        return  if(entityJsonStr != null) {
            Json.parse(PersonWithAccount.serializer(), entityJsonStr)
        }else {
            PersonWithAccount()
        }
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null, entityVal)
    }

    override fun handleClickSave(entity: PersonWithAccount) {
        GlobalScope.launch(doorMainDispatcher()) {
            val hasErrors = !isActiveUserAdmin && entity.currentPassword.isNullOrEmpty()
                    && !createAccount || entity.newPassword.isNullOrEmpty()
                    || entity.confirmedPassword.isNullOrEmpty() || entity.username.isNullOrEmpty()
                    || entity.confirmedPassword != entity.newPassword

            if(hasErrors){

                val requiredFieldMessage = impl.getString(MessageID.field_required_prompt, context)

                view.usernameError = if(entity.username.isNullOrEmpty())
                    requiredFieldMessage else null
                view.currentPasswordError = if(entity.currentPassword.isNullOrEmpty()
                        && !isActiveUserAdmin && !createAccount)
                    requiredFieldMessage else null
                view.newPasswordError = if(entity.newPassword.isNullOrEmpty())
                    requiredFieldMessage else null
                view.confirmedPasswordError = if(entity.confirmedPassword.isNullOrEmpty())
                    requiredFieldMessage else null
                view.noPasswordMatchError = if(entity.confirmedPassword != entity.newPassword)
                    impl.getString(MessageID.filed_password_no_match, context) else null
                return@launch
            }
            try{
                if(createAccount && !entity.newPassword.isNullOrEmpty()
                        && !entity.confirmedPassword.isNullOrEmpty()){
                    val umAccount = accountManager.register(entity,serverUrl, false)
                    if(umAccount.username != null){
                        repo.personDao.updateAsync(entity)
                    }
                }else{
                    val currentPassword = entity.currentPassword
                    val newPassword = entity.newPassword
                    val username = entity.username
                    if(((currentPassword != null && !isActiveUserAdmin) || isActiveUserAdmin)
                            && newPassword != null && username != null){
                        accountManager.changePassword(username, currentPassword,
                                newPassword, serverUrl)
                    }
                }
                view.finishWithResult(listOf(entity))
            } catch (e: Exception){
                when (e) {
                    is UnauthorizedException -> view.currentPasswordError =
                            impl.getString(MessageID.incorrect_current_password, context)
                    is IllegalStateException -> view.usernameError =
                            impl.getString(MessageID.person_exists, context)
                    else -> view.errorMessage = impl.getString(MessageID.login_network_error, context)
                }
            }
        }
    }

}