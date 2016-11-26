package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.AttendanceClassStudent;
import com.ustadmobile.core.model.ListableEntity;
import com.ustadmobile.core.view.PersonListView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by mike on 20/11/16.
 */

public class PersonListController extends EntityListController implements Runnable{

    private boolean isRefreshing = false;

    private String classId;

    public PersonListController(Object context) {
        super(context);

    }

    @Override
    public void setUIStrings() {

    }

    public void loadStudentsByClassId(String classId) {
        AttendanceClassStudent[] students = AttendanceController.loadClassStudentListFromPrefs(
            classId, context);
        entityList.clear();
        for(int i = 0; i < students.length; i++) {
            entityList.add(students[i]);
        }
    }

    /**
     * Handle when the user pulls down to refresh
     */
    public void handleRefresh() {
        if(isRefreshing) {
            return;
        }

        isRefreshing = true;
        new Thread(this).start();
    }

    /**
     * Used to reload the class list from the server
     */
    public void run() {
        try {
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            LoginController.loadClassListToPrefs(classId, impl.getAppPref(
                    UstadMobileSystemImpl.PREFKEY_XAPISERVER, UstadMobileDefaults.DEFAULT_XAPI_SERVER,
                    context),context);
            loadStudentsByClassId(classId);
        }catch(Exception e) {
            e.printStackTrace();
        }finally{
            PersonListView view = (PersonListView)getView();
            if(view != null) {
                view.setRefreshing(false);
                view.setEntityList(entityList);
            }
            isRefreshing = false;
        }
    }


    @Override
    public UstadController loadController(Hashtable args, Object context) throws Exception {
        PersonListController controller = new PersonListController(context);
        controller.classId = args.get(ClassManagementController2.ARG_CLASSID).toString();
        controller.loadStudentsByClassId(controller.classId);
        return controller;
    }

    public static void makeControllerForView(PersonListView view, Hashtable args, ControllerReadyListener readyListener) {
        PersonListController controller = new PersonListController(view.getContext());
        new LoadControllerThread(args, controller, readyListener, view).start();
    }

}
