package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.*
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.*
import org.kodein.di.DI

class ContentEntryDetailPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: ContentEntryDetailView,
    di: DI,
    lifecycleOwner: LifecycleOwner
) : UstadDetailPresenter<ContentEntryDetailView, ContentEntry>(
    context, arguments, view, di, lifecycleOwner
) {


    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return false
    }

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override fun onLoadFromJson(bundle: Map<String, String>): ContentEntry? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[UstadEditView.ARG_ENTITY_JSON]
        var editEntity: ContentEntry? = null
        if (entityJsonStr != null) {
            editEntity = safeParse(di, ContentEntry.serializer(), entityJsonStr)
        } else {
            editEntity = ContentEntry()
        }

        setupTabs()

        return editEntity
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ContentEntry? {
        val entityUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L
        val entry = withContext(Dispatchers.Default) {
            db.onRepoWithFallbackToDb(2000) { it.contentEntryDao.findByUidAsync(entityUid) }
        }

        setupTabs()

        return entry
    }


    private fun setupTabs() {
        val entityUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L
        val commonArgs = mapOf(
            UstadView.ARG_NAV_CHILD to true.toString(),
            UstadView.ARG_ENTITY_UID to entityUid.toString(),
            UstadView.ARG_CLAZZUID to (arguments[UstadView.ARG_CLAZZUID] ?: "0")
        )

        view.tabs = listOf(
            ContentEntryDetailOverviewView.VIEW_NAME.appendQueryArgs(commonArgs),
            ContentEntryDetailAttemptsListView.VIEW_NAME.appendQueryArgs(commonArgs)
        )
    }


}


