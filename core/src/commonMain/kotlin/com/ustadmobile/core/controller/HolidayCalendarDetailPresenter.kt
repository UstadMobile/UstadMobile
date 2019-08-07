package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.dao.DateRangeDao
import com.ustadmobile.core.db.dao.UMCalendarDao
import com.ustadmobile.core.impl.UmAccountManager

import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl



import com.ustadmobile.core.view.AddDateRangeDialogView
import com.ustadmobile.core.view.HolidayCalendarDetailView

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.DateRange
import com.ustadmobile.lib.db.entities.UMCalendar

import com.ustadmobile.core.db.UmAppDatabase

import com.ustadmobile.core.view.AddDateRangeDialogView.Companion.DATERANGE_UID
import com.ustadmobile.core.view.HolidayCalendarDetailView.Companion.ARG_CALENDAR_UID

/**
 * Presenter for HolidayCalendarDetail view
 */
class HolidayCalendarDetailPresenter(context: Any, arguments: Map<String, String>?, view:
HolidayCalendarDetailView, val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) : UstadBaseController<HolidayCalendarDetailView>(context, arguments!!,
        view) {

    private var umProvider: UmProvider<DateRange>? = null
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
        umCalendarDao = repository.getUMCalendarDao()

        if (arguments!!.containsKey(HolidayCalendarDetailView.ARG_CALENDAR_UID)) {
            currentCalendarUid = arguments!!.get(ARG_CALENDAR_UID)
        }

    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        if (currentCalendarUid == 0L) {
            currentCalendar = UMCalendar()
            currentCalendar!!.isUmCalendarActive = false
            umCalendarDao.insertAsync(currentCalendar!!, object : UmCallback<Long> {
                override fun onSuccess(result: Long?) {
                    initFromCalendar(result!!)
                }

                override fun onFailure(exception: Throwable?) {

                }
            })
        } else {
            initFromCalendar(currentCalendarUid)
        }
    }

    fun updateRanges() {
        //Get provider
        umProvider = providerDao.findAllDatesInCalendar(currentCalendarUid)
        view.setListProvider(umProvider!!)
    }

    private fun initFromCalendar(calUid: Long) {
        this.currentCalendarUid = calUid
        //Handle Clazz info changed:
        //Get person live data and observe
        val calendarLiveData = umCalendarDao.findByUidLive(currentCalendarUid)
        //Observe the live data
        calendarLiveData.observe(this@HolidayCalendarDetailPresenter,
                UmObserver<UMCalendar> { this@HolidayCalendarDetailPresenter.handleCalendarValueChanged(it) })

        umCalendarDao.findByUidAsync(currentCalendarUid, object : UmCallback<UMCalendar> {
            override fun onSuccess(result: UMCalendar?) {
                updatedCalendar = result
                view.updateCalendarOnView(result!!)
                updateRanges()
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })

    }

    private fun handleCalendarValueChanged(calendar: UMCalendar) {
        //set the og person value
        if (currentCalendar == null)
            currentCalendar = calendar

        if (updatedCalendar == null || updatedCalendar != calendar) {
            //update class edit views
            view.updateCalendarOnView(updatedCalendar!!)
            //Update the currently editing class object
            updatedCalendar = calendar
        }
    }

    fun updateCalendarName(name: String) {
        updatedCalendar!!.umCalendarName = name
    }

    fun handleAddDateRange() {
        val args = HashMap<String, String>()
        args.put(HolidayCalendarDetailView.ARG_CALENDAR_UID, currentCalendarUid)
        impl.go(AddDateRangeDialogView.VIEW_NAME, args, context)
    }

    fun handleEditRange(rangeUid: Long) {
        val args = HashMap<String, String>()
        args.put(DATERANGE_UID, rangeUid)
        args.put(ARG_CALENDAR_UID, currentCalendarUid)
        impl.go(AddDateRangeDialogView.VIEW_NAME, args, context)
    }

    fun handleDeleteRange(rangeUid: Long) {
        repository.dateRangeDao.findByUidAsync(rangeUid, object : UmCallback<DateRange> {
            override fun onSuccess(result: DateRange?) {
                if (result != null) {
                    result.isDateRangeActive = false
                }
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }


    fun handleClickDone() {

        updatedCalendar!!.isUmCalendarActive = true
        updatedCalendar!!.umCalendarCategory = UMCalendar.CATEGORY_HOLIDAY
        repository.getUMCalendarDao().updateAsync(updatedCalendar, object : UmCallback<Int> {
            override fun onSuccess(result: Int?) {
                view.finish()
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })

    }
}
