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

    private var createAccount: Boolean = false

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        serverUrl = arguments[ARG_SERVER_URL]?:accountManager.activeAccount.endpointUrl
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Person? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: accountManager.activeAccount.personUid
        val person =  withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.personDao?.findByUid(entityUid)
        } ?: Person()
        createAccount = person.username == null
        view.fistPasswordFieldHint = impl.getString(if(createAccount) MessageID.password
        else MessageID.current_password,context)
        view.secondPasswordFieldHint = impl.getString(if(createAccount) MessageID.confirm_password
        else MessageID.new_password,context)

        return person
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
            if(!entity.admin && view.firstPassword.isNullOrEmpty() || view.secondPassword.isNullOrEmpty()
                    || entity.username.isNullOrEmpty()){

                view.usernameRequiredErrorVisible = entity.username.isNullOrEmpty()
                view.firstPasswordFieldRequiredErrorVisible = view.firstPassword.isNullOrEmpty() && !entity.admin
                view.secondPasswordFieldRequiredErrorVisible = view.secondPassword.isNullOrEmpty()
                return@launch
            }
            val firstPassword = view.firstPassword
            val secondPassword = view.secondPassword
            val username = entity.username
            try{
                if(createAccount && firstPassword != null && secondPassword != null){
                    if(firstPassword != secondPassword){
                        view.showPasswordDoNotMatchError()
                        return@launch
                    }
                    val umAccount = accountManager.register(entity,secondPassword, serverUrl)
                    if(umAccount.username != null){
                        repo.personDao.updateAsync(entity)
                    }
                }else{
                    if(firstPassword != null && secondPassword != null && username != null){
                        accountManager.changePassword(username,firstPassword,secondPassword, serverUrl)
                    }
                }
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