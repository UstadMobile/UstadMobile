package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntry2DetailView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithMostRecentContainer
import com.ustadmobile.lib.db.entities.UmAccount


class ContentEntry2DetailPresenter(context: Any,
                          arguments: Map<String, String>, view: ContentEntry2DetailView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadDetailPresenter<ContentEntry2DetailView, ContentEntryWithMostRecentContainer>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = TODO("PERSISTENCE_MODE.DB OR PERSISTENCE_MODE.JSON")

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //TODO: Set any additional fields (e.g. joinlist) on the view
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ContentEntryWithMostRecentContainer? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        //TODO: Load the list for any one to many join helper here
        /* e.g.
         val contentEntry = withTimeoutOrNull {
             db.contentEntry.findByUid(entityUid)
         } ?: ContentEntry()
         return contentEntry
         */
        return TODO("Implement load from Database or return null if using PERSISTENCE_MODE.JSON")
    }


    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        TODO("Not yet implemented")
    }

}