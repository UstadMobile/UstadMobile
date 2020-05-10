package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*


class PersonDetailPresenter(context: Any,
                          arguments: Map<String, String>, view: PersonDetailView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadDetailPresenter<PersonDetailView, PersonWithDisplayDetails>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

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
        view.clazzes = repo.clazzMemberDao.findAllClazzesByPersonWithClazz(entityUid)
        return repo.personDao.findByUidWithDisplayDetailsLive(entityUid)
    }

    fun handleClickClazz(clazz: ClazzMemberWithClazz) {
        systemImpl.go(ClazzDetailView.VIEW_NAME,
                mapOf(ARG_ENTITY_UID to clazz.clazzMemberClazzUid.toString()), context)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): PersonWithDisplayDetails? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        //Dummy value - not really used
        return PersonWithDisplayDetails()
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return true
    }

    override fun handleClickEdit() {
        val personUid = view.entity?.personUid ?: return
        systemImpl.go(PersonEditView.VIEW_NAME, mapOf(ARG_ENTITY_UID to personUid.toString()),
            context)
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}