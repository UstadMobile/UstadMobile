package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ScopedGrantDetailView
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.ScopedGrantWithName
import org.kodein.di.DI


class ScopedGrantDetailPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: ScopedGrantDetailView,
    lifecycleOwner: LifecycleOwner,
    di: DI
): UstadDetailPresenter<ScopedGrantDetailView, ScopedGrantWithName>(
    context, arguments, view, di, lifecycleOwner
) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.LIVEDATA

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //TODO: Set any additional fields (e.g. joinlist) on the view
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return true
    }

    override fun onLoadLiveData(repo: UmAppDatabase): LiveData<ScopedGrantWithName?> {
        return repo.scopedGrantDao.findByUidLiveWithName(arguments[ARG_ENTITY_UID]?.toLong() ?: 0)
    }

    override fun handleClickEdit() {
        requireNavController().navigate(ScopedGrantEditView.VIEW_NAME,
            mapOf(ARG_ENTITY_UID to (entity?.sgUid?.toString() ?: "0")))
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}