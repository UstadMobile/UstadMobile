package com.ustadmobile.core.controller;

import com.ustadmobile.core.model.AttendanceClass;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.core.view.ClassListView;
import com.ustadmobile.core.controller.AttendanceController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClassManagementView;
import java.util.Hashtable;

/**
 * Created by varuna on 20/02/16.
 */
public class ClassListController extends UstadBaseController{

    private AttendanceClass[] attendanceClasses;

    private ClassListView classListView;

    public ClassListController(Object context) {
        super(context);
        attendanceClasses = AttendanceController.loadTeacherClassListFromPrefs(context);
    }


    public void setView(UstadView view) {
        super.setView(view);
        classListView = (ClassListView)view;
        String[] classNames = new String[attendanceClasses.length];
        for(int i = 0; i < classNames.length; i++){
            classNames[i] = attendanceClasses[i].name;
        }
        classListView.setClassList(classNames);
    }

    public static ClassListController makeControllerForView(ClassListView view) {
        ClassListController ctrl = new ClassListController(view.getContext());
        ctrl.setView(view);

        return ctrl;
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
