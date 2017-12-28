/*
 * 
 */
package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.port.sharedse.model.AttendanceClass;
import com.ustadmobile.port.sharedse.model.AttendanceClassStudent;
import com.ustadmobile.port.sharedse.view.ClassManagementView;
import com.ustadmobile.port.sharedse.view.EnrollStudentView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

/**
 *
 * @author varuna
 */
public class EnrollStudentController extends UstadBaseController {

    /**
     * Hashtable key to pass the class id argument
     */
    public static final String KEY_CLASSID = "classid";
    
    public static final String TEACHER_ENROLL_STUDENT_ENDPOINT = "teacher_enroll_student/";
    
    private AttendanceClass mClass; 
    
    private AttendanceClassStudent[] students;
        
    private EnrollStudentView enrollStudentView;
    
    public static final String REGISTER_COUNTRY = "country";
    
    public static final String REGISTER_PHONENUM = "phonenumber";
    
    public static final String REGISTER_NAME = "name";
    
    public static final String REGISTER_GENDER = "gender";
    
    public static final String REGISTER_USERNAME = "username";
    
    public static final String REGISTER_PASSWORD = "password";
    
    public static final String REGISTER_EMAIL = "email";
        
    /**
     * Create a new class management controller for the given class id
     * 
     * 
     * @param context
     * @param classId Class ID of the given class (must be a class for this teacher)
     */
    public EnrollStudentController(Object context, String classId) {
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
        enrollStudentView = (EnrollStudentView)view;
    }


    public void setUIStrings() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        enrollStudentView.setTitle(impl.getString(MessageID.login, getContext()));
        enrollStudentView.setButtonText(impl.getString(MessageID.login, getContext()));
        enrollStudentView.setUsernameHint(impl.getString(MessageID.username, getContext()));
        enrollStudentView.setPasswordHint(impl.getString(MessageID.password, getContext()));
        enrollStudentView.setRegisterButtonText(impl.getString(MessageID.register, getContext()));
        enrollStudentView.setRegisterNameHint(impl.getString(MessageID.name, getContext()));
        enrollStudentView.setRegisterPhoneNumberHint(impl.getString(MessageID.phone_number, getContext()));
        enrollStudentView.setRegisterGenderMaleLabel(impl.getString(MessageID.male, getContext()));
        enrollStudentView.setRegisterGenderFemaleLabel(impl.getString(MessageID.female, getContext()));
        String optSffx = " (" + impl.getString(MessageID.optional, getContext()) + ")";
        enrollStudentView.setRegisterUsernameHint(impl.getString(MessageID.username, getContext()) + optSffx);
        enrollStudentView.setRegisterPasswordHint(impl.getString(MessageID.password, getContext()) + optSffx);
        enrollStudentView.setRegisterEmailHint(impl.getString(MessageID.email, getContext()) + optSffx);
        enrollStudentView.setDirection(UstadMobileSystemImpl.getInstance().getDirection());
  
    }
    
    /**
     * Create a new class management controller for the given class id
     * 
     * @param view The EnrollStudentView we want to associate with
     * @param args Hashtable containing KEY_CLASSID
     * @return new ClassManagementController for this class
     */
 
    public static EnrollStudentController makeControllerForView(EnrollStudentView view, Hashtable args){
        EnrollStudentController ctrl = new EnrollStudentController(view.getContext(),
            (String)args.get(KEY_CLASSID));
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
    
    public void handleClickEnroll(final Hashtable userInfoParams) throws IOException{
//        System.out.println("Handling register button in the controller..");
//        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
//
//        Thread enrollThread = new Thread() {
//            public void run() {
//                String classId = mClass.id; // is the class
//                String teacher_username = impl.getActiveUser(context);
//                String teacher_password = impl.getActiveUserAuth(context);
//
//                String teacher_enroll_student_url = null;
//                String umCloudServer = UstadMobileSystemImpl.getInstance().getAppPref(
//                            UstadMobileSystemImpl.PREFKEY_XAPISERVER,
//                            CoreBuildConfig.DEFAULT_XAPI_SERVER, context);
//                teacher_enroll_student_url = umCloudServer + "/" +
//                        TEACHER_ENROLL_STUDENT_ENDPOINT;
//
//                Hashtable headers = new Hashtable();
//                headers.put("UM-In-App-Registration-Version", "1.0.1");
//                //headers.put("X-Experience-API-Version", "1.0.1");
//                headers.put("Authorization", LoginController.encodeBasicAuth(
//                        teacher_username, teacher_password));
//                userInfoParams.put("class_id", classId);
//
//                HTTPResult registrationResult = null;
//                try {
//                    registrationResult = impl.makeRequest(
//                            teacher_enroll_student_url, headers, userInfoParams, "POST");
//                } catch (IOException ex) {
//                    System.out.println("EXCEPTION in making Request: " + ex.toString());
//                }
//
//                String serverSays = null;
//                try {
//                    serverSays = new String(registrationResult.getResponse(), "UTF-8");
//                } catch (UnsupportedEncodingException ex) {
//                    System.out.println("EXCEPTION: " + ex.toString());
//                }
//                if(registrationResult.getStatus() != 200) {
//                    try {
//                        throw new IOException("Registration error: code "
//                                + registrationResult.getStatus());
//                    } catch (IOException ex) {
//                        System.out.println("EXCEPTION: " + ex.toString());
//                    }
//                }else{
//                    System.out.println("Added OK");
//
//                }
//
//
//
//                impl.getAppView(context).dismissProgressDialog();
//                Hashtable args = new Hashtable();
//                args.put(ClassManagementController.KEY_CLASSID,
//                        classId);
//                args.put(ClassManagementController.KEY_UPDATE_STUDENT_LIST, "true");
//                UstadMobileSystemImpl.getInstance().go(ClassManagementView.VIEW_NAME, args,
//                        context);
//                //return serverSays;
//
//            }
//        };
//        impl.getAppView(context).showProgressDialog(impl.getString(MessageID.processing, getContext()));
//        String serverSays;
//        //return serverSays;
//        enrollThread.start();
    }
    
}
