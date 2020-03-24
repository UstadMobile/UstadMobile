package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.UmAccount

class HolidayCalendarListPresenter(context: Any, arguments: Map<String, String>, view: HolidayCalendarListView,
                          lifecycleOwner: DoorLifecycleOwner, systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?>)
    : UstadListPresenter<HolidayCalendarListView, HolidayCalendar>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {


    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class HolidayCalendarListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { HolidayCalendarListSortOption(it, context) }
        view.list = repo.holidayCalendarDao.findAllHolidaysWithEntriesCount()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    private fun updateListOnView() {
        /* TODO: Update the list on the view from the appropriate DAO query, e.g.
        view.list = when(sortOrder) {
            SortOrder.ORDER_NAME_ASC -> repo.daoName.findAllActiveClazzesSortByNameAsc(
                    searchQuery, loggedInPersonUid)
            SortOrder.ORDER_NAME_DSC -> repo.daoName.findAllActiveClazzesSortByNameDesc(
                    searchQuery, loggedInPersonUid)
        }
        */
    }

    override fun handleClickEntry(entry: HolidayCalendar) {
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
//            ListViewMode.BROWSER -> systemImpl.go(HolidayCalendarDetailView.VIEW_NAME,
//                mapOf(HolidayCalendarDetailView.ARG_ENTITY_UID to uid, context)
        }

    }

    override fun handleClickCreateNew() {
        /* TODO: Add code to go to the edit view
        systemImpl.go(HolidayCalendarEditView.VIEW_NAME, mapOf(), context)
         */
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? HolidayCalendarListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }
}