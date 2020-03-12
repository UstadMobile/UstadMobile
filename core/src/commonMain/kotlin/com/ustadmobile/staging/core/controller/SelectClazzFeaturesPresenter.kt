package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.SelectClazzFeaturesView
import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_ACTIVITY_ENABLED
import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_ATTENDANCE_ENABLED
import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_CLAZZUID
import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_SEL_ENABLED
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Clazz.Companion.CLAZZ_FEATURE_ATTENDANCE
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * Presenter for SelectClazzFeatures view
 */
class SelectClazzFeaturesPresenter(context: Any, arguments: Map<String, String>, view:
SelectClazzFeaturesView) : UstadBaseController<SelectClazzFeaturesView>(context, arguments,
        view) {

    internal var repository: UmAppDatabase = UmAccountManager.getRepositoryForActiveAccount(context)
    internal var clazzDao: ClazzDao
    private var currentClazzUid: Long = 0
    private var givenValues = false

    private var attendanceFeature: Boolean = false
    private var activityFeature: Boolean = false
    private var selFeature: Boolean = false

    var currentClazz: Clazz? = null

    init {

        clazzDao = repository.clazzDao

        if (arguments.containsKey(CLAZZ_FEATURE_CLAZZUID)) {
            currentClazzUid = arguments[CLAZZ_FEATURE_CLAZZUID]!!.toLong()
        }
        if (arguments.containsKey(CLAZZ_FEATURE_ATTENDANCE_ENABLED)) {
            if (arguments[CLAZZ_FEATURE_ATTENDANCE_ENABLED] == "yes") {
                attendanceFeature = true
                givenValues = true
            }
        }
        if (arguments.containsKey(CLAZZ_FEATURE_ACTIVITY_ENABLED)) {
            if (arguments[CLAZZ_FEATURE_ACTIVITY_ENABLED] == "yes") {
                activityFeature = true
                givenValues = true
            }
        }
        if (arguments.containsKey(CLAZZ_FEATURE_SEL_ENABLED)) {
            if (arguments[CLAZZ_FEATURE_SEL_ENABLED] == "yes") {
                selFeature = true
                givenValues = true
            }
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (currentClazzUid != 0L) {
            GlobalScope.launch {
                val result = clazzDao.findByUidAsync(currentClazzUid)
                currentClazz = result
                if (givenValues) {
                    view.updateFeaturesOnView(currentClazz!!)
                }
            }
        }
    }

    fun updateAttendanceFeature(enabled: Boolean) {
        currentClazz?.updateAttendanceFeature(enabled)
    }

    fun updateActivityFeature(enabled: Boolean) {
        currentClazz?.updateActivityFeature(enabled)
    }

    fun updateSELFeature(enabled: Boolean) {
        currentClazz?.updateSelFeature(enabled)
    }

    fun updateAssignmentFeature(enabled: Boolean){
        currentClazz?.updateAssignmentFeature(enabled)
    }

}
