package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.LoadControllerThread;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.sharedse.model.AttendanceClassStudent;
import com.ustadmobile.port.sharedse.view.PersonListView;

import java.util.Hashtable;

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
                    UstadMobileSystemImpl.PREFKEY_XAPISERVER, CoreBuildConfig.DEFAULT_XAPI_SERVER,
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
