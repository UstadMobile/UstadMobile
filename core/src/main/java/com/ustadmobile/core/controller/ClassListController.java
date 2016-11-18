package com.ustadmobile.core.controller;

import com.ustadmobile.core.model.AttendanceClass;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.core.view.ClassListView;
import com.ustadmobile.core.controller.AttendanceController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClassManagementView;

import java.util.Calendar;
import java.util.Hashtable;

/**
 * Created by varuna on 20/02/16.
 */
public class ClassListController extends UstadBaseController implements AsyncLoadableController{

    private AttendanceClass[] attendanceClasses;

    private ClassListView classListView;

    public ClassListController(Object context) {
        super(context);
        loadClasses();
    }

    protected void loadClasses(){
        attendanceClasses = AttendanceController.loadTeacherClassListFromPrefs(context);
        for(int i = 0; i < attendanceClasses.length; i++) {
            attendanceClasses[i].syncStatus = AttendanceController.getAttendanceStatusByClassId(context, attendanceClasses[i].id);
        }
    }

    @Override
    public UstadController loadController(Hashtable args, Object context) throws Exception {
        ClassListController controller = new ClassListController(context);
        return controller;
    }

    public static void makeControllerForView(ClassListView view, Hashtable args, ControllerReadyListener listener) {
        ClassListController ctrl = new ClassListController(view.getContext());
        ctrl.setView(view);
        listener.controllerReady(ctrl, 0);
    }
    public void setView(UstadView view) {
        super.setView(view);
        classListView = (ClassListView)view;
        classListView.setClassList(attendanceClasses);
    }


    public void setUIStrings() {
        
    }

    public void handleClassSelected(int index) {
        Hashtable args = new Hashtable();
        args.put(ClassManagementController.KEY_CLASSID, 
                attendanceClasses[index].id);
        UstadMobileSystemImpl.getInstance().go(ClassManagementView.class, args, 
                context);
    }


}
