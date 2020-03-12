package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ClazzEditView2
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.jvm.Volatile

class ClazzEditPresenter2(context: Any, arguments: Map<String, String>,
                          view: ClazzEditView2, val db: UmAppDatabase,
                          val repo: UmAppDatabase): UstadBaseController<ClazzEditView2>(context, arguments, view) {


    @Volatile
    private var clazz: Clazz? = null

    private val clazzSchedulesList = DoorMutableLiveData<List<Schedule>>()

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        GlobalScope.launch {
            val clazzUid = arguments[UstadView.ARG_CLAZZ_UID]?.toLong() ?: 0L
            clazz = repo.clazzDao.findByUid(clazzUid) ?: Clazz("")
            val schedules = repo.scheduleDao.findAllSchedulesByClazzUidAsList(clazzUid)
            clazzSchedulesList.sendValue(schedules)
            view.runOnUiThread(Runnable {
                view.clazz = clazz
            })
        }

        view.clazzSchedules = clazzSchedulesList
    }

    fun handleClickDone(clazz: Clazz) {

    }

    fun handleAddSchedule(schedule: Schedule) {

    }

    fun handleRemoveSchedule(schedule: Schedule) {

    }

}