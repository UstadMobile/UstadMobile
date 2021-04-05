package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.*
import org.kodein.di.DI

class ContentEntryDetailViewPagerPresenter(context: Any,
                                           arguments: Map<String, String>, view: ContentEntryDetailViewPagerView, di: DI,
                                           lifecycleOwner: DoorLifecycleOwner)
    : UstadDetailPresenter<ContentEntryDetailViewPagerView, ContentEntry>(context, arguments, view, di, lifecycleOwner) {


    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return false
    }

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override fun onLoadFromJson(bundle: Map<String, String>): ContentEntry? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[UstadEditView.ARG_ENTITY_JSON]
        var editEntity: ContentEntry? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di,  ContentEntry.serializer(), entityJsonStr)
        }else {
            editEntity = ContentEntry()
        }

        GlobalScope.launch {
            setupTabs()
        }

        return editEntity
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ContentEntry? {
        val entityUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L
        val entry = withContext(Dispatchers.Default) {
            withTimeoutOrNull(2000) { db.contentEntryDao.findByUidAsync(entityUid) }
        } ?: ContentEntry()

        setupTabs()

        return entry
    }


    private suspend fun setupTabs() {
        val entityUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L
        GlobalScope.launch(doorMainDispatcher()) {
            view.tabs = listOf("${ContentEntryDetailOverviewView.VIEW_NAME}?${UstadView.ARG_ENTITY_UID}=$entityUid",
            "${ContentEntryDetailAttemptsListView.VIEW_NAME}?${UstadView.ARG_ENTITY_UID}=$entityUid")
        }
    }




}


