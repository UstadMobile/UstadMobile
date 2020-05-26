package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.serialization.json.Json

class ClazzWorkListPresenter(context: Any, arguments: Map<String, String>, view: ClazzWorkListView,
                          lifecycleOwner: DoorLifecycleOwner, systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?>)
    : UstadListPresenter<ClazzWorkListView, ClazzWork>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {


    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class ClazzWorkListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { ClazzWorkListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        //TODO
        return true
    }

    private fun updateListOnView() {

        val clazzUid = arguments.get(UstadView.ARG_CLAZZ_UID)?.toLong()?:0L
        view.list = when (currentSortOrder) {
            SortOrder.ORDER_NAME_ASC -> repo.clazzWorkDao.findWithMetricsByClazzUidLiveAsc(clazzUid)
            SortOrder.ORDER_NAME_DSC -> repo.clazzWorkDao.findWithMetricsByClazzUidLiveDesc(clazzUid)
        }
    }

    override fun handleClickEntry(entry: ClazzWork) {
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(ClazzWorkDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.clazzWorkUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        val clazzUid = arguments.get(UstadView.ARG_CLAZZ_UID)?.toLong()?:0L

        val clazzWork: ClazzWork = ClazzWork().apply {
            clazzWorkClazzUid = clazzUid
        }
        val clazzWorkJson = Json.stringify(ClazzWork.serializer(), clazzWork)
        systemImpl.go(ClazzWorkEditView.VIEW_NAME,
                mapOf(UstadEditView.ARG_ENTITY_JSON to clazzWorkJson), context)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? ClazzWorkListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }
}