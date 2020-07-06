package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.TimeZoneEntityListView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.TimeZoneEntity
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class TimeZoneEntityListPresenter(context: Any, arguments: Map<String, String>, view: TimeZoneEntityListView,
                                  di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<TimeZoneEntityListView, TimeZoneEntity>(context, arguments, view, di, lifecycleOwner) {


    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class TimeZoneEntityListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { TimeZoneEntityListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?) = false

    private fun updateListOnView() {
        view.list = db.timeZoneEntityDao.findAllSortedByOffset()
        /* TODO: Update the list on the view from the appropriate DAO query, e.g.
        view.list = when(sortOrder) {
            SortOrder.ORDER_NAME_ASC -> repo.daoName.findAllActiveClazzesSortByNameAsc(
                    searchQuery, loggedInPersonUid)
            SortOrder.ORDER_NAME_DSC -> repo.daoName.findAllActiveClazzesSortByNameDesc(
                    searchQuery, loggedInPersonUid)
        }
        */
    }

    override fun handleClickEntry(entry: TimeZoneEntity) {
        if(mListMode == ListViewMode.PICKER) {
            view.finishWithResult(listOf(entry))
        }
    }

    override fun handleClickCreateNewFab() {
        //this is not supoprted for timezones
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? TimeZoneEntityListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }
}