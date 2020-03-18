package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.HolidayCalendarList2View
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.lib.db.entities.HolidayCalendar

//class HolidayCalendarList2Presenter(context: Any, args: Map<String, String>, view: HolidayCalendarList2View,
//        val db: UmAppDatabase, val repo: UmAppDatabase)
//    : UstadListPresenter<HolidayCalendarList2View, HolidayCalendar>(context, args, view) {
//
//    lateinit var listMode: ListViewMode
//
//    override fun onCreate(savedState: Map<String, String?>?) {
//        super.onCreate(savedState)
//        view.list = repo.holidayCalendarDao.findAllHolidaysWithEntriesCount()
//    }
//
//    override fun handleClickEntry(entry: HolidayCalendar) {
//        when(listMode) {
//            ListViewMode.PICKER -> view.finishWithResult(entry)
//            //TODO: go to edit view otherwise
//        }
//    }
//
//    override fun handleClickCreateNew() {
//
//    }
//}