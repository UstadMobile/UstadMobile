package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher

import io.ktor.client.features.json.defaultSerializer
import io.ktor.http.content.TextContent
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.*


class ReportDetailPresenter(context: Any,
                          arguments: Map<String, String>, view: ReportDetailView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadDetailPresenter<ReportDetailView, ReportWithFilters>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)



    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ReportWithFilters? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L


        val report = withTimeoutOrNull(2000) {
            db.reportDao.findByUid(entityUid)
        } ?: Report()

        val reportFilterList = withTimeout(2000) {
            db.reportFilterDao.findByReportUid(report.reportUid)
        }

        return ReportWithFilters(report, reportFilterList)
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ReportWithFilters? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[UstadEditView.ARG_ENTITY_JSON]
        val editEntity: ReportWithFilters?
        editEntity = if (entityJsonStr != null) {
            Json.parse(ReportWithFilters.serializer(), entityJsonStr)
        } else {
            ReportWithFilters()
        }

        return editEntity
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return true
    }

    companion object {



    }



}