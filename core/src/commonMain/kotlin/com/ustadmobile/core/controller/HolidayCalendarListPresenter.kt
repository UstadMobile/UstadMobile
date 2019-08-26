package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.UMCalendarDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.HolidayCalendarDetailView
import com.ustadmobile.core.view.HolidayCalendarDetailView.Companion.ARG_CALENDAR_UID
import com.ustadmobile.core.view.HolidayCalendarListView
import com.ustadmobile.lib.db.entities.UMCalendarWithNumEntries
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Presenter for HolidayCalendarList view
 */
class HolidayCalendarListPresenter(context: Any, arguments: Map<String, String>?,
                                   view: HolidayCalendarListView,
                                   val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<HolidayCalendarListView>(context, arguments!!, view) {

    private var umProvider: DataSource.Factory<Int, UMCalendarWithNumEntries>? = null
    internal var repository: UmAppDatabase
    private val providerDao: UMCalendarDao


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        providerDao = repository.umCalendarDao


    }

    override fun onCreate(savedState: Map<String, String?>?) {
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
        args.put(ARG_CALENDAR_UID, calendarUid.toString())
        impl.go(HolidayCalendarDetailView.VIEW_NAME, args, context)
    }

    fun handleDeleteCalendar(calendarUid: Long) {
        GlobalScope.launch {
            repository.umCalendarDao.inactivateCalendarAsync(calendarUid)
        }
    }

}
