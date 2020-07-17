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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance


class PersonAccountEditPresenter(context: Any,
                                 arguments: Map<String, String>, view: PersonAccountEditView, di: DI,
                                 lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<PersonAccountEditView, Person>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private lateinit var serverUrl: String

    private val impl: UstadMobileSystemImpl by instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        serverUrl = arguments[ARG_SERVER_URL]?:accountManager.activeAccount.endpointUrl
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Person? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: accountManager.activeAccount.personUid
        return withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.personDao?.findByUid(entityUid)
        } ?: Person()
    }

    override fun onLoadFromJson(bundle: Map<String, String>): Person? {
        super.onLoadFromJson(bundle)
        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        return  if(entityJsonStr != null) {
            Json.parse(Person.serializer(), entityJsonStr)
        }else {
            Person()
        }
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null, entityVal)
    }

    override fun handleClickSave(entity: Person) {
        GlobalScope.launch(doorMainDispatcher()) {
            if(!entity.admin && view.currentPassword.isNullOrEmpty() || view.newPassword.isNullOrEmpty()
                    || entity.username.isNullOrEmpty()){

                view.usernameRequiredErrorVisible = entity.username.isNullOrEmpty()
                view.currentPasswordRequiredErrorVisible = view.currentPassword.isNullOrEmpty() && !entity.admin
                view.newPasswordRequiredErrorVisible = view.newPassword.isNullOrEmpty()
                return@launch
            }
            val currentPassword = view.currentPassword
            val newPassword = view.newPassword
            val username = entity.username
            if(currentPassword != null && newPassword != null && username != null){
                try{
                    accountManager.changePassword(username,currentPassword,newPassword, serverUrl)
                    view.finishWithResult(listOf(entity))
                } catch (e: Exception){
                    view.errorMessage = impl.getString(if(e is UnauthorizedException)
                        MessageID.filed_password_no_match else
                        MessageID.login_network_error , context)
                    view.clearFields()
                }
            }
        }
    }

}