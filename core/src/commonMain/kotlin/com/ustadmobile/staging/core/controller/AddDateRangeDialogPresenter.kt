package com.ustadmobile.core.controller


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.DateRangeDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.AddDateRangeDialogView
import com.ustadmobile.core.view.AddDateRangeDialogView.Companion.DATERANGE_UID
import com.ustadmobile.core.view.HolidayCalendarDetailView.Companion.ARG_CALENDAR_UID
import com.ustadmobile.lib.db.entities.DateRange
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class AddDateRangeDialogPresenter(context: Any, arguments: Map<String, String>?,
                                  view: AddDateRangeDialogView)
    : UstadBaseController<AddDateRangeDialogView>(context, arguments!!, view) {

    private var currentDateRange: DateRange? = null

    private val dateRangeDao: DateRangeDao

    private val appDatabaseRepo: UmAppDatabase

    private var currentDateRangeUid: Long = 0
    private var currentCalendarUid: Long = 0

    init {

        appDatabaseRepo = UmAccountManager.getRepositoryForActiveAccount(context)
        dateRangeDao = appDatabaseRepo.dateRangeDao

        if (arguments!!.containsKey(ARG_CALENDAR_UID)) {
            currentCalendarUid = arguments.get(ARG_CALENDAR_UID)!!.toLong()
        }

        if (arguments.containsKey(DATERANGE_UID)) {
            currentDateRangeUid = arguments.get(DATERANGE_UID)!!.toLong()
        }

        if (currentDateRangeUid > 0) {
            GlobalScope.launch {
                val result = dateRangeDao.findByUidAsync(currentDateRangeUid)
                currentDateRange = result
                view.updateFields(result!!)
            }
        } else {
            currentDateRange = DateRange()
        }
    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        if (currentDateRange == null) {
            currentDateRange = DateRange()
        }

    }

    /**
     * Handles what happens when you click OK/Add in the DateRange dialog - Persists the date range
     * to that clazz.
     */
    fun handleAddDateRange() {

        currentDateRange!!.dateRangeActive = true
        currentDateRange!!.dateRangeUMCalendarUid = currentCalendarUid

        if (currentDateRange!!.dateRangeUid == 0L) { //Not persisted. Insert it.
            GlobalScope.launch {
                dateRangeDao.insertAsync(currentDateRange!!)
            }
        } else { //Update it.
            GlobalScope.launch {
                dateRangeDao.updateAsync(currentDateRange!!)
            }
        }
    }

    /**
     * Cancels the schedule dialog
     */
    fun handleCancelDateRange() {
        //Do nothing.
        currentDateRange = null
    }

    /**
     * Sets the picked "from" time from the dialog to the daterange object in the presenter. In ms
     * since the start of the day.
     *
     * @param time  The "from" time.
     */
    fun handleDateRangeFromTimeSelected(time: Long) {
        currentDateRange!!.dateRangeFromDate = time
    }

    /**
     * Sets the picked "to" time from the dialog to the daterange object in the presenter.
     *
     * @param time The "to" time
     */
    fun handleDateRangeToTimeSelected(time: Long) {
        currentDateRange!!.dateRangeToDate = time
    }

}
