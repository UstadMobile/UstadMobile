package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.SelectClazzFeaturesView;
import com.ustadmobile.lib.db.entities.Clazz;

import static com.ustadmobile.core.view.SelectClazzFeaturesView.CLAZZ_FEATURE_ACTIVITY_ENABLED;
import static com.ustadmobile.core.view.SelectClazzFeaturesView.CLAZZ_FEATURE_ATTENDANCE_ENABLED;
import static com.ustadmobile.core.view.SelectClazzFeaturesView.CLAZZ_FEATURE_CLAZZUID;
import static com.ustadmobile.core.view.SelectClazzFeaturesView.CLAZZ_FEATURE_SEL_ENABLED;


/**
 * Presenter for SelectClazzFeatures view
 **/
public class SelectClazzFeaturesPresenter extends UstadBaseController<SelectClazzFeaturesView> {

    UmAppDatabase repository;
    ClazzDao clazzDao;
    private long currentClazzUid = 0;
    private boolean givenValues = false;

    private boolean attendanceFeature, activityFeature, selFeature;

    private Clazz currentClazz;


    public SelectClazzFeaturesPresenter(Object context, Hashtable arguments, SelectClazzFeaturesView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        clazzDao = repository.getClazzDao();

        if(arguments.containsKey(CLAZZ_FEATURE_CLAZZUID)){
            currentClazzUid = (long) arguments.get(CLAZZ_FEATURE_CLAZZUID);
        }
        if(arguments.containsKey(CLAZZ_FEATURE_ATTENDANCE_ENABLED)){
            if(arguments.get(CLAZZ_FEATURE_ATTENDANCE_ENABLED).equals("yes")){
                attendanceFeature = true;
                givenValues = true;
            }
        }
        if(arguments.containsKey(CLAZZ_FEATURE_ACTIVITY_ENABLED)){
            if(arguments.get(CLAZZ_FEATURE_ACTIVITY_ENABLED).equals("yes")){
                activityFeature = true;
                givenValues = true;
            }
        }
        if(arguments.containsKey(CLAZZ_FEATURE_SEL_ENABLED)){
            if(arguments.get(CLAZZ_FEATURE_SEL_ENABLED).equals("yes")){
                selFeature = true;
                givenValues = true;
            }
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if(currentClazzUid != 0){
            clazzDao.findByUidAsync(currentClazzUid, new UmCallback<Clazz>() {
                @Override
                public void onSuccess(Clazz result) {
                    currentClazz = result;
                    if(givenValues) {
                        view.updateFeaturesOnView(currentClazz);
                    }
                }

                @Override
                public void onFailure(Throwable exception) {exception.printStackTrace();}
            });
        }

    }

    public void updateAttendanceFeature(boolean enabled){
        currentClazz.setAttendanceFeature(enabled);
    }
    public void updateActivityFeature(boolean enabled){
        currentClazz.setActivityFeature(enabled);
    }
    public void updateSELFeature(boolean enabled){
        currentClazz.setSelFeature(enabled);
    }


    public Clazz getCurrentClazz() {
        return currentClazz;
    }

    public void setCurrentClazz(Clazz currentClazz) {
        this.currentClazz = currentClazz;
    }



}
