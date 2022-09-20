package com.ustadmobile.core.controller

import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.PersonAccountEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on


class PersonAccountEditPresenter(context: Any,
                                 arguments: Map<String, String>, view: PersonAccountEditView, di: DI,
                                 lifecycleOwner: LifecycleOwner)
    : UstadEditPresenter<PersonAccountEditView, PersonWithAccount>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private lateinit var serverUrl: String

    private val impl: UstadMobileSystemImpl by instance()

    private val authManager: AuthManager by on(accountManager.activeEndpoint).instance()

    private var createAccount: Boolean = false

    private var activeUserHasPasswordResetPermission: Boolean = false

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        serverUrl = arguments[ARG_SERVER_URL]?:accountManager.activeAccount.endpointUrl
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): PersonWithAccount {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val activePersonUid = accountManager.activeAccount.personUid

        activeUserHasPasswordResetPermission = activePersonUid != entityUid &&
            (withTimeoutOrNull(2000) {
                db.personDao.personHasPermissionAsync(
                    activePersonUid, entityUid,
                    Role.PERMISSION_RESET_PASSWORD
                )
            } ?: false)

        view.currentPasswordVisible = !activeUserHasPasswordResetPermission

        val person = withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.personDao?.findPersonAccountByUid(entityUid)
        } ?: PersonWithAccount()
        createAccount = person.username.isNullOrEmpty()
        view.usernameVisible = person.username.isNullOrBlank()
        return person
    }

    override fun onLoadFromJson(bundle: Map<String, String>): PersonWithAccount {
        super.onLoadFromJson(bundle)
        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        return  if(entityJsonStr != null) {
            safeParse(di, PersonWithAccount.serializer(), entityJsonStr)
        }else {
            PersonWithAccount()
        }
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, json, PersonWithAccount.serializer(), entityVal)
    }

    override fun handleClickSave(entity: PersonWithAccount) {
        val hasErrors =
            !activeUserHasPasswordResetPermission && entity.currentPassword.isNullOrEmpty()
                && !createAccount
            || entity.newPassword.isNullOrEmpty()
            || entity.confirmedPassword.isNullOrEmpty() || entity.username.isNullOrEmpty()
            || entity.confirmedPassword != entity.newPassword

        if(hasErrors){
            val requiredFieldMessage = impl.getString(MessageID.field_required_prompt, context)

            view.usernameError = if(entity.username.isNullOrEmpty())
                requiredFieldMessage else null
            view.noPasswordMatchError = if(entity.confirmedPassword != entity.newPassword
                && !entity.confirmedPassword.isNullOrEmpty() && !entity.newPassword.isNullOrEmpty())
                impl.getString(MessageID.filed_password_no_match, context) else null

            view.currentPasswordError = if(entity.currentPassword.isNullOrEmpty()
                && !activeUserHasPasswordResetPermission && !createAccount)
                requiredFieldMessage else null
            view.newPasswordError = if(entity.newPassword.isNullOrEmpty())
                requiredFieldMessage else view.newPasswordError
            view.confirmedPasswordError = if(entity.confirmedPassword.isNullOrEmpty())
                requiredFieldMessage else view.confirmedPasswordError

            return
        }

        val newPassword = entity.newPassword
            ?: throw IllegalStateException("Not possible! hasErrors checked false")

        presenterScope.launch {
            val entityUsername = entity.username
            val currentPassword = entity.currentPassword

            //check if we need to validate the current password
            if(!activeUserHasPasswordResetPermission && !createAccount) {
                if(entityUsername == null || currentPassword == null)
                    throw IllegalStateException("Should have been an error")

                if(!authManager.authenticate(entityUsername, currentPassword).success) {
                    //current password is wrong
                    view.currentPasswordError = impl.getString(MessageID.incorrect_current_password,
                        context)
                    return@launch
                }
            }

            if(createAccount) {
                if(entityUsername == null)
                    throw IllegalStateException("Cannot create new user with null username")

                if(repo.personDao.findByUsernameCount(entityUsername) == 0) {
                    repo.personDao.updateAsync(entity)
                }else {
                    view.usernameError = impl.getString(MessageID.person_exists, context)
                    return@launch
                }
            }

            authManager.setAuth(entity.personUid, newPassword)
            finishWithResult(safeStringify(di, ListSerializer(PersonWithAccount.serializer()), listOf(entity)))
        }
    }

}