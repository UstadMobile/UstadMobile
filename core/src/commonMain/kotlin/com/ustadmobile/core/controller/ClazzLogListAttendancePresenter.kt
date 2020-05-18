package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzWithSchool
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ClazzLogListAttendancePresenter(context: Any, arguments: Map<String, String>, view: ClazzLogListAttendanceView,
                          lifecycleOwner: DoorLifecycleOwner, systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?>)
    : UstadListPresenter<ClazzLogListAttendanceView, ClazzLog>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {


    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    var clazzUidFilter: Long = 0

    private var clazzWithSchool: ClazzWithSchool? = null

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class ClazzLogListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        clazzUidFilter = arguments[UstadView.ARG_FILTER_BY_CLAZZUID]?.toLong() ?: 0
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { ClazzLogListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        //TODO: this would be based on whether or not the person has permission to record attendance
        return true
    }

    private fun updateListOnView() {
        GlobalScope.launch(doorMainDispatcher()) {
            clazzWithSchool = repo.clazzDao.getClazzWithSchool(clazzUidFilter)
            view.clazzTimeZone = clazzWithSchool?.clazzTimeZone ?: clazzWithSchool?.school?.schoolTimeZone ?: "UTC"
            view.list = repo.clazzLogDao.findByClazzUidAsFactory(clazzUidFilter)
        }

    }

    override fun handleClickEntry(entry: ClazzLog) {
        /* TODO: Add code to go to the appropriate detail view or make a selection
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(ClazzLogDetailView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to uid, context)
        }
        */
    }

    override fun handleClickCreateNewFab() {
        //in this instance we should open up the most recent clazzlog for clazz log detail
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? ClazzLogListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }
}