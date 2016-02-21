/*
 * 
 */
package com.ustadmobile.core.controller;

import com.ustadmobile.core.U;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.AttendanceClass;
import com.ustadmobile.core.model.AttendanceClassStudent;
import com.ustadmobile.core.view.AttendanceView;
import com.ustadmobile.core.view.ClassManagementView;
import com.ustadmobile.core.view.UstadView;
import java.util.Hashtable;

/**
 *
 * @author varuna
 */
public class ClassManagementController extends UstadBaseController{

    /**
     * Hashtable key to pass the class id argument
     */
    public static final String KEY_CLASSID = "classid";
    
    private AttendanceClass mClass; 
    
    private AttendanceClassStudent[] students;
    
    private ClassManagementView classView;
    
    /**
     * Create a new class management controller for the given class id
     * 
     * 
     * @param context
     * @param classId Class ID of the given class (must be a class for this teacher)
     */
    public ClassManagementController(Object context, String classId) {
        super(context);
        mClass = getClassById(context, classId);
        students = AttendanceController.loadClassStudentListFromPrefs(classId, 
                context);
    }
    
    /**
     * Get the AttendanceClass object from the JSON string stored in preferences
     * by class id
     * 
     * @param context
     * @param classId Class ID to look up
     * @return AttendanceClass object with id and name of the given class
     */
    public static AttendanceClass getClassById(Object context, String classId) {
        AttendanceClass[] teacherClasses = AttendanceController.loadTeacherClassListFromPrefs(context);
        for(int i = 0; i < teacherClasses.length; i++) {
            if(teacherClasses[i].id.equals(classId)) {
                return teacherClasses[i];
            }
        }
        
        return null;
    }
    
    public void setView(UstadView view) {
        super.setView(view);
        classView = (ClassManagementView)view;
        classView.setStudentList(students);
    }
    
    public void setUIStrings() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        classView.setAttendanceLabel(impl.getString(U.id.attendance));
        classView.setReportsLabel(impl.getString(U.id.reports));
        classView.setExamsLabel(impl.getString(U.id.exams));
        classView.setClassName(mClass.name);
    }
    
    /**
     * Create a new class management controller for the given class id
     * 
     * @param view The ClassManagementView we want to associate with
     * @param args Hashtable containing KEY_CLASSID
     * @return new ClassManagementController for this class
     */
    public static ClassManagementController makeControllerForView(ClassManagementView view, Hashtable args) {
       ClassManagementController ctrl = new ClassManagementController(view.getContext(), 
            (String)args.get(KEY_CLASSID));
       ctrl.setView(view);
       ctrl.setUIStrings();
       return ctrl;
    }
    
    public void handleClickAttendanceButton() {
        Hashtable args = new Hashtable();
        args.put(AttendanceController.KEY_CLASSID, mClass.id);
        UstadMobileSystemImpl.getInstance().go(AttendanceView.class, args, 
                context);
    }
    
}
