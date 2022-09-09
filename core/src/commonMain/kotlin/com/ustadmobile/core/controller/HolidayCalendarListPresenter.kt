package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.*
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI

class HolidayCalendarListPresenter(context: Any, arguments: Map<String, String>, view: HolidayCalendarListView,
        di: DI, lifecycleOwner: LifecycleOwner)
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
            ListViewMode.PICKER -> finishWithResult(safeStringify(di, ListSerializer(HolidayCalendar.serializer()),listOf(entry)))
            ListViewMode.BROWSER -> navigateForResult(
                NavigateForResultOptions(
                    this, null,
                    HolidayCalendarEditView.VIEW_NAME,
                    HolidayCalendar::class,
                    HolidayCalendar.serializer(),
                    RESULT_DEST_KEY,
                    arguments = mutableMapOf(UstadView.ARG_ENTITY_UID to entry.umCalendarUid.toString())
                    )
                )
        }
    }

    override fun handleClickCreateNewFab() {
        navigateForResult(
            NavigateForResultOptions(
                this, null,
                HolidayCalendarEditView.VIEW_NAME,
                HolidayCalendar::class,
                HolidayCalendar.serializer(),
                RESULT_DEST_KEY
            )
        )
    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {
        handleClickCreateNewFab()
    }



    override fun handleClickSortOrder(sortOption: IdOption) {
        //no sort options here
    }

    companion object {
        const val RESULT_DEST_KEY = "SchoolHolidayCalendar"
    }
}