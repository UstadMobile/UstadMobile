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


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    private fun updateListOnView() {
        view.list = repo.holidayCalendarDao.findAllHolidaysWithEntriesCount()
    }

    override fun handleClickEntry(entry: HolidayCalendar) {
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
//            ListViewMode.BROWSER -> systemImpl.go(HolidayCalendarDetailView.VIEW_NAME,
//                mapOf(HolidayCalendarDetailView.ARG_ENTITY_UID to uid, context)
        }
    }

    override fun handleClickCreateNewFab() {
        //TODO: go to the view
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        //no sort options here
    }
}