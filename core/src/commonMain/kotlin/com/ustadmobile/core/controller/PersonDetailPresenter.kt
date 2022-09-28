package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.UstadView.Companion.CURRENT_DEST
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

class PersonDetailPresenter(context: Any,
                          arguments: Map<String, String>, view: PersonDetailView,
                            di : DI, lifecycleOwner: LifecycleOwner)
    : UstadDetailPresenter<PersonDetailView, PersonWithPersonParentJoin>(context, arguments, view,
        di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.LIVEDATA

    override fun onLoadLiveData(repo: UmAppDatabase): LiveData<PersonWithPersonParentJoin?>? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        view.clazzes = repo.clazzEnrolmentDao.findAllClazzesByPersonWithClazz(entityUid)
        val activePersonUid = accountManager.activeSession?.person?.personUid ?: -1

        GlobalScope.launch(doorMainDispatcher()) {
            val person = repo.onRepoWithFallbackToDb(5000) { dbToUse ->
                dbToUse.takeIf { entityUid != 0L }?.personDao?.findByUidAsync(entityUid)
            } ?: Person()

            //Reset password uses additional seeked permission
            val hasAuthPermission = repo.personDao.personHasPermissionAsync(
                activePersonUid,
                entityUid, Role.PERMISSION_RESET_PASSWORD
            )
            view.changePasswordVisible = person.username != null
                    && (activePersonUid == entityUid || hasAuthPermission)

            view.chatVisibility = person.personUid != activePersonUid

            view.showCreateAccountVisible =  person.username == null && hasAuthPermission
        }
        return repo.personDao.findByUidWithDisplayDetailsLive(entityUid, activePersonUid)
    }

    fun handleClickClazz(clazz: ClazzEnrolmentWithClazz) {
        systemImpl.go(ClazzDetailView.VIEW_NAME,
                mapOf(ARG_ENTITY_UID to clazz.clazzEnrolmentClazzUid.toString()), context)
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return repo.personDao.personHasPermissionAsync(
            account?.personUid ?: 0,
            arguments[ARG_ENTITY_UID]?.toLong() ?: 0L,
            Role.PERMISSION_PERSON_UPDATE
        )
    }

    override fun handleClickEdit() = navigateToEditScreen(PersonEditView.VIEW_NAME)

    fun handleChangePassword() = navigateToEditScreen()

    fun handleCreateAccount() = navigateToEditScreen()

    private fun navigateToEditScreen(destination: String = PersonAccountEditView.VIEW_NAME){
        val personUid = view.entity?.personUid ?: return
        navigateForResult(
            NavigateForResultOptions(this,
                null, destination,
                PersonWithAccount::class,
                PersonWithAccount.serializer(), SAVEDSTATE_KEY_PERSON,
                arguments = mutableMapOf(ARG_ENTITY_UID to personUid.toString())
            )
        )
    }

    fun handleClickChat(){
        systemImpl.go(
            ChatDetailView.VIEW_NAME,
            mapOf(
                ARG_PERSON_UID to (arguments[ARG_ENTITY_UID]?.toLong() ?: 0L).toString(),
            ),
            context)
    }

    fun handleClickManageParentalConsent() {
        val ppjUid = entityLiveData?.getValue()?.parentJoin?.ppjUid ?: 0L

        if(ppjUid != 0L) {
            systemImpl.go(ParentalConsentManagementView.VIEW_NAME,
                mapOf(ARG_ENTITY_UID to ppjUid.toString(),
                    ARG_NEXT to CURRENT_DEST), context)
        }else {
            view.showSnackBar(systemImpl.getString(MessageID.error, context))
        }
    }

    companion object {
        const val SAVEDSTATE_KEY_PERSON = "Person"
    }


}