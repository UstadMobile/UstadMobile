package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.db.dao.DateRangeDao
import com.ustadmobile.core.db.dao.UMCalendarDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.HolidayCalendarDetailView
import com.ustadmobile.core.view.HolidayCalendarListView
import com.ustadmobile.lib.db.entities.UMCalendar
import com.ustadmobile.lib.db.entities.UMCalendarWithNumEntries



import com.ustadmobile.core.view.HolidayCalendarDetailView.Companion.ARG_CALENDAR_UID

/**
 * Presenter for HolidayCalendarList view
 */
class HolidayCalendarListPresenter(context: Any, arguments: Map<String, String>?,
                                   view: HolidayCalendarListView,
                                   val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<HolidayCalendarListView>(context, arguments!!, view) {

    private var umProvider: UmProvider<UMCalendarWithNumEntries>? = null
    internal var repository: UmAppDatabase
    private val providerDao: UMCalendarDao


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        providerDao = repository.getUMCalendarDao()


    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //Get provider
        umProvider = providerDao.findAllHolidaysWithEntriesCount()
        view.setListProvider(umProvider!!)

    }

    fun handleClickPrimaryActionButton() {
        val args = HashMap<String, String>()
        impl.go(HolidayCalendarDetailView.VIEW_NAME, args, context)
    }

    fun handleEditCalendar(calendarUid: Long) {
        val args = HashMap<String, String>()
        args.put(ARG_CALENDAR_UID, calendarUid)
        impl.go(HolidayCalendarDetailView.VIEW_NAME, args, context)
    }

    fun handleDeleteCalendar(calendarUid: Long) {
        repository.getUMCalendarDao().inactivateCalendarAsync(calendarUid, null)
    }

}
