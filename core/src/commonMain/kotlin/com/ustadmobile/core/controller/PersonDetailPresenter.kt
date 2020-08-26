package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.PersonAccountEditView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI


class PersonDetailPresenter(context: Any,
                          arguments: Map<String, String>, view: PersonDetailView,
                            di : DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadDetailPresenter<PersonDetailView, PersonWithDisplayDetails>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.LIVEDATA

    private var person: Person? = null

    private var personPicture: PersonPicture? = null

    private var presenterFields: List<PresenterFieldRow>? = null

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        //TODO: Set any additional fields (e.g. joinlist) on the view
    }

    override fun onLoadLiveData(repo: UmAppDatabase): DoorLiveData<PersonWithDisplayDetails?>? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        view.clazzes = repo.clazzMemberDao.findAllClazzesByPersonWithClazz(entityUid, getSystemTimeInMillis())

        GlobalScope.launch(doorMainDispatcher()) {
            val activePersonUid = accountManager.activeAccount.personUid

            val activePerson = withTimeoutOrNull(2000) {
                db.personDao.findByUid(activePersonUid)
            } ?: Person()

            val person = withTimeoutOrNull(2000) {
                db.takeIf { entityUid != 0L }?.personDao?.findByUid(entityUid)
            } ?: PersonWithDisplayDetails()

            view.changePasswordVisible = person.username != null
                    && (activePersonUid == entityUid || activePerson.admin)

            view.showCreateAccountVisible =  person.username == null && activePerson.admin
        }
        return repo.personDao.findByUidWithDisplayDetailsLive(entityUid)
    }

    fun handleClickClazz(clazz: ClazzMemberWithClazz) {
        systemImpl.go(ClazzDetailView.VIEW_NAME,
                mapOf(ARG_ENTITY_UID to clazz.clazzMemberClazzUid.toString()), context)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return repo.personDao.personHasPermission(account?.personUid ?: 0,
                arguments[ARG_ENTITY_UID]?.toLong() ?: 0L,
                    Role.PERMISSION_PERSON_UPDATE)
    }

    override fun handleClickEdit() {
        val personUid = view.entity?.personUid ?: return
        systemImpl.go(PersonEditView.VIEW_NAME, mapOf(ARG_ENTITY_UID to personUid.toString()),
            context)
    }

    fun handleChangePassword(){
        val personUid = view.entity?.personUid ?: return
        systemImpl.go(PersonAccountEditView.VIEW_NAME, mapOf(ARG_ENTITY_UID to personUid.toString()),
                context)
    }

    fun handleCreateAccount(){
        val personUid = view.entity?.personUid ?: return
        systemImpl.go(PersonAccountEditView.VIEW_NAME, mapOf(ARG_ENTITY_UID to personUid.toString()), context)
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}