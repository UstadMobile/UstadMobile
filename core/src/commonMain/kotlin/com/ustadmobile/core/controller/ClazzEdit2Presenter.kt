package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.coroutines.*
import kotlin.jvm.Volatile

class ClazzEdit2Presenter(context: Any, arguments: Map<String, String>,
                          view: ClazzEdit2View, val db: UmAppDatabase,
                          val repo: UmAppDatabase): UstadBaseController<ClazzEdit2View>(context, arguments, view) {


    @Volatile
    private var clazz: Clazz? = null

    private val clazzSchedulesList = DoorMutableLiveData<List<Schedule>>()

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)


        GlobalScope.launch(doorMainDispatcher()) {
            val clazzUid = arguments[UstadView.ARG_CLAZZ_UID]?.toLong() ?: 0L

            view.clazzSchedules = clazzSchedulesList
            view.loading = true
            view.fieldsEnabled = false
            listOf(db, repo).forEach {
                val clazz = withTimeoutOrNull(2000) {
                    it.clazzDao.takeIf {clazzUid != 0L }?.findByUidAsync(clazzUid) ?: Clazz("Test Clazz").also {
                        it.isClazzActive = true
                    }
                }  ?: return@forEach

                val schedules = withTimeoutOrNull(2000) {
                    it.scheduleDao.findAllSchedulesByClazzUidAsync(clazzUid)
                } ?: listOf()

                clazzSchedulesList.sendValue(schedules)

                this@ClazzEdit2Presenter.clazz = clazz
                view.clazz = clazz
            }

            view.loading = false
            view.fieldsEnabled = true
        }
    }

    fun handleClickDone(clazz: Clazz) {
        GlobalScope.launch(doorMainDispatcher()) {
            if(clazz.clazzUid == 0L) {
                clazz.clazzUid = repo.clazzDao.insertAsync(clazz)
            }else {
                repo.clazzDao.updateAsync(clazz)
            }

            view.finish()
        }
    }

    fun handleAddSchedule(schedule: Schedule) {

    }

    fun handleRemoveSchedule(schedule: Schedule) {

    }

}