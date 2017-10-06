/*
 * 
 */
package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.port.sharedse.model.AttendanceClass;
import com.ustadmobile.port.sharedse.model.AttendanceClassStudent;
import com.ustadmobile.port.sharedse.view.AttendanceView;
import com.ustadmobile.port.sharedse.view.ClassManagementView;
import com.ustadmobile.port.sharedse.view.EnrollStudentView;

import java.io.IOException;
import java.util.Hashtable;

/**
 *
 * @author varuna
 */
public class ClassManagementController extends UstadBaseController {

    /**
     * Hashtable key to pass the class id argument
     */
    public static final String KEY_CLASSID = "classid";
    
    public static final String KEY_UPDATE_STUDENT_LIST = "updatestudentlist";
    
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
        students = AttendanceController.loadClassStudentListFromPrefs(classId, context);
    }
    
    public ClassManagementController(Object context, String classID, boolean refreshStudent){
        super(context);
        mClass = getClassById(context, classID);
        if (refreshStudent){
            students = AttendanceController.loadClassStudentListFromNet(classID, context);
        }else{
            students = AttendanceController.loadClassStudentListFromPrefs(classID, context);
        }
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
        classView.setAttendanceLabel(impl.getString(MessageID.attendance, getContext()));
        classView.setReportsLabel(impl.getString(MessageID.reports, getContext()));
        classView.setExamsLabel(impl.getString(MessageID.exams, getContext()));
        classView.setClassName(mClass.name);
    }
    
    public static void loadClassStudentListFromNet(
            final String classID, final Object context, 
            final ClassManagementView view) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        Thread getStudentListThread = new Thread() {

            public void run() {
                String username = impl.getActiveUser(context);
                String password = impl.getActiveUserAuth(context);
                String classURL = UMFileUtil.resolveLink(
                    CoreBuildConfig.DEFAULT_XAPI_SERVER,
                    UstadMobileDefaults.DEFAULT_STUDENTLIST_ENDPOINT)
                        + classID;

                String studentListJSON = null;
                try {
                    studentListJSON = LoginController.getJSONArrayResult(
                            username, password, classURL);
                } catch (IOException ex) {
                    System.out.println("Something wrong with getting "
                            + "Student List: " + ex.toString());

                }
                if(studentListJSON != null) {
                    impl.setUserPref("studentlist."+classID,
                            studentListJSON, context);
                }
                AttendanceClassStudent[] students = 
                        AttendanceController.loadClassStudentListFromPrefs(classID, context);
                view.updateStudentList(students);
                
            }
        };
        getStudentListThread.start();
        
        
        //return AttendanceController.loadClassStudentListFromPrefs(classID, context);
    }
    
    /**
     * Create a new class management controller for the given class id
     * 
     * @param view The ClassManagementView we want to associate with
     * @param args Hashtable containing KEY_CLASSID
     * @return new ClassManagementController for this class
     */
    public static ClassManagementController makeControllerForView(ClassManagementView view, Hashtable args) {
       
       String classID = (String)args.get(KEY_CLASSID);
       ClassManagementController ctrl = new ClassManagementController(view.getContext(), 
            (String)args.get(KEY_CLASSID));
       if (args.containsKey(KEY_UPDATE_STUDENT_LIST)){
            //ctrl.students = loadClassStudentListFromNet(classID, view.getTargetContext(), view);
            loadClassStudentListFromNet(classID, view.getContext(), view);   
       }
       ctrl.setView(view);
       ctrl.setUIStrings();
       return ctrl;

    }
    
    public void handleShowEnrollForm(){
        Hashtable args = new Hashtable();
        args.put(AttendanceController.KEY_CLASSID, mClass.id);
        UstadMobileSystemImpl.getInstance().go(EnrollStudentView.VIEW_NAME, args,
                context);
    }
        
    public void handleClickAttendanceButton() {
        Hashtable args = new Hashtable();
        args.put(AttendanceController.KEY_CLASSID, mClass.id);
        UstadMobileSystemImpl.getInstance().go(AttendanceView.VIEW_NAME, args,
                context);
    }
    
}
