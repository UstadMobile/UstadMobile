package com.ustadmobile.core.view


import com.ustadmobile.lib.db.entities.Clazz

/**
 * Core View. Screen is for SelectClazzFeatures's View
 */
interface SelectClazzFeaturesView : UstadView {

    fun updateFeaturesOnView(clazZ: Clazz)

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "SelectClazzFeatures"

        //Any argument keys:
        val CLAZZ_FEATURE_CLAZZUID = "ClazzFeatureClazzUid"
        val CLAZZ_FEATURE_ATTENDANCE_ENABLED = "ClazzFeatureAttendanceEnabled"
        val CLAZZ_FEATURE_ACTIVITY_ENABLED = "ClazzFeatureActivityEnabled"
        val CLAZZ_FEATURE_SEL_ENABLED = "ClazzFeatureSELEnabled"
    }


}

