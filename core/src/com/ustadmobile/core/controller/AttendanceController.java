/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.core.controller;

import com.ustadmobile.core.U;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.AttendanceClass;
import com.ustadmobile.core.model.AttendanceClassStudent;
import com.ustadmobile.core.model.AttendanceRowModel;
import com.ustadmobile.core.omr.OMRRecognizer;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.AttendanceView;
import com.ustadmobile.core.view.UstadView;
import java.io.IOException;
import java.util.Hashtable;
import jp.sourceforge.qrcode.QRCodeDecoder;
import jp.sourceforge.qrcode.data.QRCodeImage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author mike
 */
public class AttendanceController extends UstadBaseController{

    //areas in which optical marks are to be found on the paper
    public static final float AREA_WIDTH = 607f;
    
    public static final float AREA_HEIGHT = 902f;
    
    //public static final float OMR_AREA_OFFSET_X = (311f/AREA_WIDTH);
    
    //public static final float OMR_AREA_OFFSET_Y = (37.5f/AREA_HEIGHT);

    public static final float OMR_AREA_OFFSET_X_1 = (196.2f/AREA_WIDTH);

    public static final float OMR_AREA_OFFSET_Y_1 = (41.1f/AREA_HEIGHT);

    public static final float OMR_AREA_OFFSET_X_2 = (478f/AREA_WIDTH);

    public static final float OMR_AREA_OFFSET_Y_2 = (41f/AREA_HEIGHT);
    
    public static final float OM_WIDTH = 26f/AREA_WIDTH;
    
    public static final float OM_HEIGHT = 20f/AREA_HEIGHT;
    
    public static final float OM_ROW_HEIGHT = 25.8f/AREA_HEIGHT;

    private AttendanceRowModel[] attendanceResult; 
    
    private AttendanceView view;
    
    private AttendanceClass[] teacherClasses;
    
    private int selectedClass;
    
    private AttendanceClassStudent[] classStudents;
    
    /**
     * Mapping of standard tincan verbs to IDS as per 
     * AttendanceRowModel.STATUS_
     */
    public static final String[] VERB_IDS = new String[] {
        "http://adlnet.gov/expapi/verbs/attended",
        "http://www.ustadmobile.com/xapi/verb/late",
        "http://www.ustadmobile.com/xapi/verb/absent-excused",
        "http://id.tincanapi.com/verb/skipped"
    };
    
    public static final String[] VERB_DISPLAYS = new String[] {
        "Attended", "Late", "Absent - Excused", "Skipped"
    };
    
    public AttendanceController(Object context) {
        super(context);
    }
    
    public static AttendanceController makeControllerForView(AttendanceView view) {
        AttendanceController ctrl = new AttendanceController(view.getContext());
        ctrl.setView(view);
        ctrl.loadTeacherClassList();
        
        return ctrl;
    }

    /**
     * Index of the class selected for taking attendance
     * 
     * @return 
     */
    public int getSelectedClass() {
        return selectedClass;
    }

    /**
     * Set the index of the class selected for taking attendance
     * 
     * @param selectedClass 
     */
    public void setSelectedClass(int selectedClass) {
        this.selectedClass = selectedClass;
    }
    
    public void handleClassSelected(int selectedClass) {
        setSelectedClass(selectedClass);
        view.showTakePicture();
    }
    
    private void loadTeacherClassList() {
        //set the class list JSON
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String classListJSON = impl.getUserPref("teacherclasslist", getContext());
        
        if(classListJSON == null) {
            return;//not a teacher or no classes assigned
        }
        
        try {
            JSONArray jsonArr = new JSONArray(classListJSON);
            teacherClasses = new AttendanceClass[jsonArr.length()];
            JSONObject classObj;
            for(int i = 0; i < teacherClasses.length; i++) {
                classObj = jsonArr.getJSONObject(i);
                teacherClasses[i] = new AttendanceClass(classObj.getString("id"),
                    classObj.getString("name"));
            }
        }catch(JSONException j) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 189, classListJSON, j);
        }
    }
    
    public static AttendanceClassStudent[] loadClassStudentListFromPrefs(String classID, Object context) {
        AttendanceClassStudent[] result = null;
        String prefKey = "studentlist." + classID;
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String classJSONStr = impl.getUserPref(prefKey, context);
        if(classJSONStr != null) {
            try {
                JSONArray arr = new JSONArray(classJSONStr);
                result = new AttendanceClassStudent[arr.length()];
                JSONObject studentObj;
                for(int i = 0; i < result.length; i++) {
                    studentObj = arr.getJSONObject(i);
                    result[i] = new AttendanceClassStudent(
                        studentObj.getString("username"), 
                        studentObj.getString("username"));
                }
            }catch(JSONException e) {
                
            }
        }else {
            
        }
        
        return result;
    }
    
    public void setUIStrings() {
        
    }
    
    /**
     * the view should call this to get the workflow started
     */
    public void handleStartFlow() {
        if (teacherClasses == null || teacherClasses.length == 0 ){
            UstadMobileSystemImpl.getInstance().getAppView(context).showNotification(
                    "You are not a teacher or no students assigned", AppView.LENGTH_LONG);
            view.finish();
        }else {
            String[] classList = new String[teacherClasses.length];
            for (int i = 0; i < classList.length; i++) {
                classList[i] = teacherClasses[i].name;
            }

            view.showClassList(classList);
        }
    }
    
    
    

    public void setView(UstadView view) {
        super.setView(view); 
        this.view = (AttendanceView)view;
    }
    
    
    
    public void handleClickSnap() {
        view.showTakePicture();
    }
    
    /**
     * Handle when the image has been taken by the underlying system - start
     * processing the image in a new threads
     * 
     * @param sysImage - the image captured by the system - in a system dependent
     * format e.g. Bitmap on Android, J2ME Image etc.
     */
    public void handlePictureAcquired(Object sysImage) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.getAppView(getContext()).showProgressDialog(impl.getString(
            U.id.processing));
        new ProcessAttendancePictureThread(sysImage, this).start();
    }
    
    
    public void handleResultsDecoded(boolean[][] opticalMarks) {
        classStudents = loadClassStudentListFromPrefs(
                teacherClasses[selectedClass].id, context);

        attendanceResult = new AttendanceRowModel[Math.min(opticalMarks.length, 
            classStudents.length)];
        int i;
        int j;
        int attendanceVal;
        
        for(i = 0; i < attendanceResult.length; i++) {
            attendanceVal = -1;
            for(j = 0; j < opticalMarks[i].length; j++) {
                if(opticalMarks[i][j]) {
                    attendanceVal = j;
                    break;
                }
            }
            
            attendanceResult[i] = new AttendanceRowModel(i, classStudents[i].name, 
                classStudents[i].username, attendanceVal);
        }
        
        view.showResult(attendanceResult);
    }
    
    public void handleClickSubmitResults() {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String xAPIServer = UstadMobileSystemImpl.getInstance().getAppPref(
                    UstadMobileSystemImpl.PREFKEY_XAPISERVER,
                    UstadMobileDefaults.DEFAULT_XAPI_SERVER, context);
        JSONArray stmtArr = new JSONArray();
        String registrationUUID = impl.generateUUID();
        
        JSONObject teacherStmt = makeAttendendedStmt(
            teacherClasses[selectedClass].id, teacherClasses[selectedClass].name,
            impl.getActiveUser(getContext()), xAPIServer, 
            "http://activitystrea.ms/schema/1.0/host", "hosted", registrationUUID, 
            null);
        stmtArr.put(teacherStmt);
        
        JSONObject studentStmt;
        JSONObject instructorActor = UMTinCanUtil.makeActorFromActiveUser(getContext());
        
        for(int i = 0; i < attendanceResult.length; i++) {
            int attendanceStatus = attendanceResult[i].attendanceStatus;
            if(attendanceStatus >= 0) {
                studentStmt = makeAttendendedStmt(teacherClasses[selectedClass].id, 
                    teacherClasses[selectedClass].name, attendanceResult[i].userId, 
                    xAPIServer, VERB_IDS[attendanceStatus], VERB_DISPLAYS[attendanceStatus], 
                    registrationUUID, instructorActor);
                stmtArr.put(studentStmt);
            }
            
        }
        sendToXAPIServer(stmtArr, xAPIServer, impl.getActiveUser(getContext()), 
            impl.getActiveUserAuth(getContext()));
    }
    
    public void handleResultsSubmitted() {
        
    }
    
    public void sendToXAPIServer(final JSONArray stmtArr, final String xapiServer, final String username, final String pasword) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        final String stmtURL = UMFileUtil.joinPaths(new String[] {
            xapiServer, "statements"});
        final String stmtStr = stmtArr.toString();
        final Hashtable headers = LoginController.makeAuthHeaders(username, 
            pasword);
        headers.put("X-Experience-API-Version", "1.0.1");
        final Object ctx = getContext();
        
        Thread sendThread = new Thread(new Runnable() {
            public void run() {
                try {
                    impl.makeRequest(stmtURL, headers, null, "POST", 
                        stmtArr.toString().getBytes("UTF-8"));
                    impl.getAppView(ctx).dismissProgressDialog();
                    view.finish();
                }catch(IOException e) {
                    impl.getAppView(ctx).dismissProgressDialog();
                    impl.getAppView(ctx).showAlertDialog("Error", "Error sending result - please try again");
                    e.printStackTrace();
                }
                
            }
        });
        impl.getAppView(getContext()).showProgressDialog("Sending...");
        sendThread.start();
    }
    
    
    public JSONObject makeAttendendedStmt(String classID, String className, String username, String xapiServer, String verbID, String verbDisplay, String registrationUUID, JSONObject instructor) {
        JSONObject stmt = new JSONObject();
        try {
            stmt.put("actor", UMTinCanUtil.makeActorFromUserAccount(username, 
                xapiServer));
            JSONObject verbObj = new JSONObject();
            
            verbObj.put("id",verbID);
            verbObj.put("display", 
                UMTinCanUtil.makeLangMapVal("en-US", verbDisplay));
            stmt.put("verb", verbObj);
            
            JSONObject object = new JSONObject();
            object.put("id", UstadMobileConstants.PREFIX_ATTENDANCE_URL + classID);
            object.put("objectType", "Activity");
            JSONObject definition = new JSONObject();
            definition.put("name", 
                UMTinCanUtil.makeLangMapVal("en-US", className));
            object.put("definition", definition);
            
            stmt.put("object", object);
            
            JSONObject stmtContext = new JSONObject();
            stmtContext.put("registration", registrationUUID);
            if(instructor != null) {
                stmtContext.put("instructor", instructor);
            }
            stmt.put("context", stmtContext);
        }catch(JSONException j) {
            
        }
        
        return stmt;
    }

    
    /**
     * Forks a thread to handle processing the image here.
     */
    public class ProcessAttendancePictureThread extends Thread {
        
        Object sysImage;
        AttendanceController ctrl;
        
        public ProcessAttendancePictureThread(Object sysImage, AttendanceController ctrl) {
            this.sysImage = sysImage;
            this.ctrl = ctrl;
        }
        
        public void run() {
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            try {
                QRCodeDecoder decoder = new QRCodeDecoder();
                QRCodeImage img = UstadMobileSystemImpl.getInstance().getQRCodeImage(
                    sysImage);
                boolean[][] bitmapImg = OMRRecognizer.convertImgToBitmap(img);
                img = null;

                boolean[][] marks_1 = OMRRecognizer.getMarks(bitmapImg,
                    OMR_AREA_OFFSET_X_1, OMR_AREA_OFFSET_Y_1,
                    OM_WIDTH, OM_HEIGHT, OM_ROW_HEIGHT, 
                    4, 33, null);


                boolean[][] marks_2 = OMRRecognizer.getMarks(bitmapImg,
                        OMR_AREA_OFFSET_X_2, OMR_AREA_OFFSET_Y_2,
                        OM_WIDTH, OM_HEIGHT, OM_ROW_HEIGHT,
                        4, 33, null);

                boolean[][] marks = new boolean[marks_1.length + marks_2.length][];

                System.arraycopy(marks_1, 0, marks, 0, marks_1.length);
                System.arraycopy(marks_2, 0, marks, marks_1.length, marks_2.length);

                //boolean[][] marks = marks_1 + marks_2;

                impl.getAppView(ctrl.getContext()).dismissProgressDialog();
                ctrl.handleResultsDecoded(marks);
            }catch(Exception e) {
                impl.getAppView(ctrl.getContext()).dismissProgressDialog();
                impl.getAppView(ctrl.getContext()).showAlertDialog("Decode failed", "Failed");
            }
        }
    }
    
    
}
