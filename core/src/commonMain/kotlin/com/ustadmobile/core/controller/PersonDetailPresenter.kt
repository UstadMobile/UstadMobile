package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.CURRENT_DEST
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.*
import org.kodein.di.DI

class PersonDetailPresenter(context: Any,
                          arguments: Map<String, String>, view: PersonDetailView,
                            di : DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadDetailPresenter<PersonDetailView, PersonWithPersonParentJoin>(context, arguments, view,
        di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.LIVEDATA

    override fun onLoadLiveData(repo: UmAppDatabase): DoorLiveData<PersonWithPersonParentJoin?>? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        view.clazzes = repo.clazzEnrolmentDao.findAllClazzesByPersonWithClazz(entityUid)
        val activePersonUid = accountManager.activeAccount.personUid

        GlobalScope.launch(doorMainDispatcher()) {
            val person = repo.onRepoWithFallbackToDb(5000) { dbToUse ->
                dbToUse.takeIf { entityUid != 0L }?.personDao?.findByUid(entityUid)
            } ?: Person()

            //Reset password uses additional seeked permission
            val hasAuthPermission = repo.personDao.personHasPermissionAsync(
                activePersonUid,
                entityUid, Role.PERMISSION_RESET_PASSWORD
            )
            view.changePasswordVisible = person.username != null
                    && (activePersonUid == entityUid || hasAuthPermission)

            view.showCreateAccountVisible =  person.username == null && hasAuthPermission

            view.rolesAndPermissions = repo.entityRoleDao.filterByPersonWithExtra(
                person.personGroupUid?:0L)
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

    override fun handleClickEdit() {
        val personUid = view.entity?.personUid ?: return
        systemImpl.go(PersonEditView.VIEW_NAME,
                mapOf(ARG_ENTITY_UID to personUid.toString()), context)
    }

    fun handleChangePassword(){
        val personUid = view.entity?.personUid ?: return
        systemImpl.go(PersonAccountEditView.VIEW_NAME,
                mapOf(ARG_ENTITY_UID to personUid.toString()), context)
    }

    fun handleCreateAccount(){
        val personUid = view.entity?.personUid ?: return
        systemImpl.go(PersonAccountEditView.VIEW_NAME,
                mapOf(ARG_ENTITY_UID to personUid.toString()), context)
    }

    fun handleClickManageParentalConsent() {
        val ppjUid = entityLiveData?.getValue()?.parentJoin?.ppjUid ?: 0

        if(ppjUid != 0L) {
            systemImpl.go(ParentalConsentManagementView.VIEW_NAME,
                mapOf(ARG_ENTITY_UID to ppjUid.toString(),
                    ARG_NEXT to CURRENT_DEST), context)
        }else {
            view.showSnackBar(systemImpl.getString(MessageID.error, context))
        }
    }


}