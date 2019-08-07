package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl

import com.ustadmobile.core.view.SelectClazzFeaturesView
import com.ustadmobile.lib.db.entities.Clazz

import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_ACTIVITY_ENABLED
import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_ATTENDANCE_ENABLED
import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_CLAZZUID
import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_SEL_ENABLED


/**
 * Presenter for SelectClazzFeatures view
 */
class SelectClazzFeaturesPresenter(context: Any, arguments: Map<String, String>?, view:
SelectClazzFeaturesView) : UstadBaseController<SelectClazzFeaturesView>(context, arguments!!,
        view) {

    internal var repository: UmAppDatabase
    internal var clazzDao: ClazzDao
    private var currentClazzUid: Long = 0
    private var givenValues = false

    private var attendanceFeature: Boolean = false
    private var activityFeature: Boolean = false
    private var selFeature: Boolean = false

    var currentClazz: Clazz? = null


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        clazzDao = repository.clazzDao

        if (arguments!!.containsKey(CLAZZ_FEATURE_CLAZZUID)) {
            currentClazzUid = arguments!!.get(CLAZZ_FEATURE_CLAZZUID)
        }
        if (arguments!!.containsKey(CLAZZ_FEATURE_ATTENDANCE_ENABLED)) {
            if (arguments!!.get(CLAZZ_FEATURE_ATTENDANCE_ENABLED) == "yes") {
                attendanceFeature = true
                givenValues = true
            }
        }
        if (arguments!!.containsKey(CLAZZ_FEATURE_ACTIVITY_ENABLED)) {
            if (arguments!!.get(CLAZZ_FEATURE_ACTIVITY_ENABLED) == "yes") {
                activityFeature = true
                givenValues = true
            }
        }
        if (arguments!!.containsKey(CLAZZ_FEATURE_SEL_ENABLED)) {
            if (arguments!!.get(CLAZZ_FEATURE_SEL_ENABLED) == "yes") {
                selFeature = true
                givenValues = true
            }
        }

    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        if (currentClazzUid != 0L) {
            clazzDao.findByUidAsync(currentClazzUid, object : UmCallback<Clazz> {
                override fun onSuccess(result: Clazz?) {
                    currentClazz = result
                    if (givenValues) {
                        view.updateFeaturesOnView(currentClazz!!)
                    }
                }

                override fun onFailure(exception: Throwable?) {
                    print(exception!!.message)
                }
            })
        }

    }

    fun updateAttendanceFeature(enabled: Boolean) {
        currentClazz!!.isAttendanceFeature = enabled
    }

    fun updateActivityFeature(enabled: Boolean) {
        currentClazz!!.isActivityFeature = enabled
    }

    fun updateSELFeature(enabled: Boolean) {
        currentClazz!!.isSelFeature = enabled
    }


}
