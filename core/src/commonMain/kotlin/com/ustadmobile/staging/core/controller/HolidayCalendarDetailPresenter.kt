package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.DateRangeDao
import com.ustadmobile.core.db.dao.UMCalendarDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.AddDateRangeDialogView
import com.ustadmobile.core.view.AddDateRangeDialogView.Companion.DATERANGE_UID
import com.ustadmobile.core.view.HolidayCalendarDetailView
import com.ustadmobile.core.view.HolidayCalendarDetailView.Companion.ARG_CALENDAR_UID
import com.ustadmobile.lib.db.entities.DateRange
import com.ustadmobile.lib.db.entities.UMCalendar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * Presenter for HolidayCalendarDetail view
 */
class HolidayCalendarDetailPresenter(context: Any, arguments: Map<String, String>?, view:
HolidayCalendarDetailView, val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<HolidayCalendarDetailView>(context, arguments!!,
        view) {

    private var umProvider: DataSource.Factory<Int, DateRange>? = null
    internal var repository: UmAppDatabase
    private val providerDao: DateRangeDao
    private var currentCalendarUid: Long = 0
    private var currentCalendar: UMCalendar? = null
    private var updatedCalendar: UMCalendar? = null
    internal var umCalendarDao: UMCalendarDao

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        providerDao = repository.dateRangeDao
        umCalendarDao = repository.umCalendarDao

        if (arguments!!.containsKey(HolidayCalendarDetailView.ARG_CALENDAR_UID)) {
            currentCalendarUid = arguments!!.get(ARG_CALENDAR_UID)!!.toLong()
        }

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (currentCalendarUid == 0L) {
            currentCalendar = UMCalendar()
            currentCalendar!!.umCalendarActive = false
            GlobalScope.launch {
                val result = umCalendarDao.insertAsync(currentCalendar!!)
                initFromCalendar(result!!)
            }
        } else {
            initFromCalendar(currentCalendarUid)
        }
    }

    fun updateRanges() {
        //Get provider
        umProvider = providerDao.findAllDatesInCalendar(currentCalendarUid)
        view.runOnUiThread(Runnable {
            view.setListProvider(umProvider!!)
        })
    }

    private fun initFromCalendar(calUid: Long) {
        this.currentCalendarUid = calUid
        //Handle Clazz info changed:
        //Get person live data and observe
        val calendarLiveData = umCalendarDao.findByUidLive(currentCalendarUid)
        //Observe the live data
        view.runOnUiThread(Runnable {
            calendarLiveData.observeWithPresenter(this, this::handleCalendarValueChanged)
        })

        GlobalScope.launch {
            val result = umCalendarDao.findByUidAsync(currentCalendarUid)
            updatedCalendar = result
            view.runOnUiThread(Runnable {
                view.updateCalendarOnView(result!!)
            })
            updateRanges()
        }

    }

    private fun handleCalendarValueChanged(calendar: UMCalendar?) {
        //set the og person value
        if (currentCalendar == null)
            currentCalendar = calendar

        if (updatedCalendar == null || updatedCalendar != calendar) {
            //Update the currently editing class object
            updatedCalendar = calendar
            //update class edit views
            view.runOnUiThread(Runnable {
                view.updateCalendarOnView(updatedCalendar!!)
            })
        }
    }

    fun updateCalendarName(name: String) {
        updatedCalendar!!.umCalendarName = name
    }

    fun handleAddDateRange() {
        val args = HashMap<String, String>()
        args.put(HolidayCalendarDetailView.ARG_CALENDAR_UID, currentCalendarUid.toString())
        impl.go(AddDateRangeDialogView.VIEW_NAME, args, context)
    }

    fun handleEditRange(rangeUid: Long) {
        val args = HashMap<String, String>()
        args.put(DATERANGE_UID, rangeUid.toString())
        args.put(ARG_CALENDAR_UID, currentCalendarUid.toString())
        impl.go(AddDateRangeDialogView.VIEW_NAME, args, context)
    }

    fun handleDeleteRange(rangeUid: Long) {
        GlobalScope.launch {
            val result = repository.dateRangeDao.findByUidAsync(rangeUid)
            if (result != null) {
                result.dateRangeActive = false
            }
        }
    }

    fun handleClickDone() {
        updatedCalendar!!.umCalendarActive = true
        updatedCalendar!!.umCalendarCategory = UMCalendar.CATEGORY_HOLIDAY
        GlobalScope.launch {
            repository.umCalendarDao.updateAsync(updatedCalendar!!)
            view.finish()
        }
    }



}
