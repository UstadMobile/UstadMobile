package com.ustadmobile.core.controller

import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class HolidayCalendarListPresenter(context: Any, arguments: Map<String, String>, view: HolidayCalendarListView,
        di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<HolidayCalendarListView, HolidayCalendar>(context, arguments, view, di, lifecycleOwner) {


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.selectionOptions = listOf(SelectionOption.EDIT, SelectionOption.DELETE)
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
            ListViewMode.BROWSER -> systemImpl.go(HolidayCalendarEditView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to entry.umCalendarUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(HolidayCalendarEditView.VIEW_NAME, mapOf(), context)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        //no sort options here
    }
}