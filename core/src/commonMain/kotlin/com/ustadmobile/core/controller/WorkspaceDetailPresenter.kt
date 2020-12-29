package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.WorkspaceDetailView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.WorkSpaceEditView
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.db.entities.WorkSpace
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI


class WorkspaceDetailPresenter(context: Any,
        arguments: Map<String, String>, view: WorkspaceDetailView,
        lifecycleOwner: DoorLifecycleOwner,
        di: DI)
    : UstadDetailPresenter<WorkspaceDetailView, WorkSpace>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return repo.personDao.takeIf { account != null }
                ?.personIsAdmin(account?.personUid ?: 0) ?: false
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): WorkSpace {
        val workspace = withTimeoutOrNull(5000) {
             db.workSpaceDao.getWorkspaceAsync()
        } ?: WorkSpace()

        if(db !is DoorDatabaseRepository) {
            view.workspaceTermsList = repo.workspaceTermsDao.findAllTermsAsFactory()
        }

        return workspace
    }

    override fun handleClickEdit() {
        systemImpl.go(WorkSpaceEditView.VIEW_NAME,
            mapOf(ARG_ENTITY_UID to entity?.uid?.toString()), context)
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}