package com.ustadmobile.core.view;


import com.ustadmobile.lib.db.entities.Clazz;

/**
 * Core View. Screen is for SelectClazzFeatures's View
 */
public interface SelectClazzFeaturesView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "SelectClazzFeatures";

    //Any argument keys:
    String CLAZZ_FEATURE_CLAZZUID = "ClazzFeatureClazzUid";
    String CLAZZ_FEATURE_ATTENDANCE_ENABLED = "ClazzFeatureAttendanceEnabled";
    String CLAZZ_FEATURE_ACTIVITY_ENABLED = "ClazzFeatureActivityEnabled";
    String CLAZZ_FEATURE_SEL_ENABLED = "ClazzFeatureSELEnabled";

    void updateFeaturesOnView(Clazz clazZ);

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

