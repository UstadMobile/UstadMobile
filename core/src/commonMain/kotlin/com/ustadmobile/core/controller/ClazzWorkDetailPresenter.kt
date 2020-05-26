package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzWorkDetailView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.withTimeoutOrNull


class ClazzWorkDetailPresenter(context: Any,
                          arguments: Map<String, String>, view: ClazzWorkDetailView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadDetailPresenter<ClazzWorkDetailView, ClazzWork>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzWork? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val clazzWork = withTimeoutOrNull(2000) {
            db.clazzWorkDao.findByUidAsync(entityUid)
        } ?: ClazzWork()

        view.title = clazzWork.clazzWorkTitle

        return clazzWork
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.studentRole = true
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        //TODO: this
        return true
    }

}