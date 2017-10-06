/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public `cense as published by
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
package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsEndpoint;
import com.ustadmobile.nanolrs.core.manager.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatement;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.port.sharedse.model.AttendanceClass;
import com.ustadmobile.port.sharedse.model.AttendanceClassStudent;
import com.ustadmobile.port.sharedse.model.AttendanceRowModel;
import com.ustadmobile.port.sharedse.omr.AttendanceSheetImage;
import com.ustadmobile.port.sharedse.view.AttendanceView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

import jp.sourceforge.qrcode.util.DebugCanvas;

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
/* $endif$ */


/**
 *
 * @author mike
 */
public class AttendanceController extends UstadBaseController {

    /**
     * Attendance for the given class has not yet been taken today
     */
    public static final int STATUS_ATTENDANCE_NOT_TAKEN = 0;

    /**
     * Attendance for the given class has been taken but not yet
     * sent to the server
     */
    public static final int STATUS_ATTENDANCE_TAKEN = 1;

    /**
     * Attendance for the given class has been sent successfully to the server
     */
    public static final int STATUS_ATTENDANCE_SENT = 2;

    public static final String XAPI_VERB_TEACHER_HOSTED = "http://activitystrea.ms/schema/1.0/host";



    public static final int[] ATTENDANCE_MESSAGES = new int[] {
            MessageID.not_taken, MessageID.sending, MessageID.sent
    };

    //areas in which optical marks are to be found on the paper
    
    
    public static final float AREA_WIDTH = 485f;
    
    public static final float AREA_HEIGHT = 722f;
    
    //First column's X and Y Offset
    
    //as per the calculation in points of the sheet generator itself. - used to be (197.2f/AREA_WIDTH);
    public static final float OMR_AREA_OFFSET_X_1 = 156f/AREA_WIDTH;

    public static final float OMR_AREA_OFFSET_Y_1 = (31.6f/AREA_HEIGHT);

    //Second column's X and Y offset
    public static final float OMR_AREA_OFFSET_X_2 = (479f/AREA_WIDTH);

    public static final float OMR_AREA_OFFSET_Y_2 = (39.5f/AREA_HEIGHT);

    //Width between each blobs
    public static final float OM_WIDTH = 20.8f/AREA_WIDTH;
    
    public static final float OM_HEIGHT = 16f/AREA_HEIGHT;
    
    public static final float OM_ROW_HEIGHT = 20.651441242f/AREA_HEIGHT;
    
    public static final int DEFAULT_GS_THRESHOLD = 128;

    public static final int ENTRYMODE_SNAP_SHEET = 0;

    public static final int ENTRYMODE_DIRECT_ENTRY = 1;
    
    
    /*
    public static final float AREA_WIDTH = 607f;
    
    public static final float AREA_HEIGHT = 902f;
    
    //First column's X and Y Offset
    
    //as per the calculation in points of the sheet generator itself. - used to be (197.2f/AREA_WIDTH);
    public static final float OMR_AREA_OFFSET_X_1 = (156f)/485f;

    public static final float OMR_AREA_OFFSET_Y_1 = (39.5f/AREA_HEIGHT);

    //Second column's X and Y offset
    public static final float OMR_AREA_OFFSET_X_2 = (479f/AREA_WIDTH);

    public static final float OMR_AREA_OFFSET_Y_2 = (39.5f/AREA_HEIGHT);

    //Width between each blobs
    public static final float OM_WIDTH = 26f/AREA_WIDTH;
    
    public static final float OM_HEIGHT = 20f/AREA_HEIGHT;
    
    public static final float OM_ROW_HEIGHT = 25.8f/AREA_HEIGHT;
    
    public static final int DEFAULT_GS_THRESHOLD = 128;
    */
    
    public AttendanceRowModel[] attendanceResult;
    
    private AttendanceView view;
        
    private AttendanceClass theClass;
    
    private AttendanceClassStudent[] classStudents;
    
    /**
     * Argument required for attendance class id
     */
    public static final String KEY_CLASSID = "classid";

    public static final String ARG_ENTRYMODE = "mode";
    

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

    public static final int VERB_IDS_ATTENDED = 0;

    public static final int VERB_IDS_LATE = 1;

    public static final int VERB_IDS_EXCUSED = 2;

    public static final int VERB_IDS_ABSENT = 3;
    
    public static final String[] VERB_DISPLAYS = new String[] {
        "Attended", "Late", "Absent - Excused", "Skipped"
    };

    private int entryMode = ENTRYMODE_SNAP_SHEET;
    
    public AttendanceController(Object context, String classId) {
        super(context);
        theClass = ClassManagementController.getClassById(context, classId);
    }
    
    public static AttendanceController makeControllerForView(AttendanceView view, Hashtable args) {
        AttendanceController ctrl = new AttendanceController(view.getContext(), 
            (String)args.get(KEY_CLASSID));

        if(args.containsKey(ARG_ENTRYMODE)) {
            ctrl.setEntryMode((Integer)args.get(ARG_ENTRYMODE));
        }

        ctrl.setView(view);

        return ctrl;
    }

    /**
     * Given the teacher class list stored in JSON in the preferences turn this into
     * an array of the AttendanceClass model
     *
     * @param context Context object to use to access preferences
     * @return Array of AttendanceClass that was decoded from json: null if preference is not set
     */
    public static AttendanceClass[] loadTeacherClassListFromPrefs(Object context) {
        //set the class list JSON
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String classListJSON = impl.getUserPref("teacherclasslist", context);
        AttendanceClass[] teacherClasses = null;

        if(classListJSON == null) {
            return null;//not a teacher or no classes assigned
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

        return teacherClasses;
    }

    /**
     *
     * @param context
     * @param classId
     * @return
     */
    public static int getAttendanceStatusByClassId(Object context, String classId) {
        //Determine the status
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        long timeMidnight = cal.getTimeInMillis();

        List<? extends XapiStatement> statementList = XapiStatementsEndpoint.getStatements(context, null, null, null,
                XAPI_VERB_TEACHER_HOSTED, UstadMobileConstants.PREFIX_ATTENDANCE_URL + classId,
                null, false, false, timeMidnight, -1, 0);

        if(statementList.isEmpty()) {
            return STATUS_ATTENDANCE_NOT_TAKEN;
        }

        boolean statementsPending = false;
        XapiForwardingStatementManager manager = PersistenceManager.getInstance().getManager(XapiForwardingStatementManager.class);
        for(int i = 0; i < statementList.size(); i++) {
            if(manager.findStatusByXapiStatement(context, statementList.get(i)) != XapiForwardingStatement.STATUS_SENT)
                return STATUS_ATTENDANCE_TAKEN;
        }

        return STATUS_ATTENDANCE_SENT;
    }

    
    
    public static AttendanceClassStudent[] loadClassStudentListFromNet(final String classID, final Object context) {
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
            }
        };
        getStudentListThread.start();
        
        
        return loadClassStudentListFromPrefs(classID, context);
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
                        studentObj.getString("username"),
                        studentObj.getString("full_name")                            );
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
        if(entryMode == ENTRYMODE_SNAP_SHEET) {
            view.showTakePicture();
        }else {
            //TODO: Remove these hard coded values
            boolean[][] marks = new boolean[66][4];
            handleResultsDecoded(marks);
        }
    }
    
    
    

    public void setView(UstadView view) {
        super.setView(view);
        this.view = (AttendanceView)view;
    }
    
    public void handleSetFocusWaitEnabled(boolean focusWaitEnabled) {

    }
    
    public void handleClickSnap() {
        view.showTakePicture();
    }
    
    public void handleSheetRecognized(final AttendanceSheetImage sheet, final DebugCanvas dc) {
        int numRows = 33; // number of rows of marks
        int numOffsetCols = 2;//number of columns 
        int numOMRsPerCol = 4;
        boolean[][] marks = new boolean[numRows * numOffsetCols][numOMRsPerCol];
        float[] offsetsX = new float[]{AttendanceSheetImage.DEFAULT_OMR_OFFSET_X_1,
            AttendanceSheetImage.DEFAULT_OMR_OFFSET_X_2};
        
        boolean[][] colMarks;
        for(int i = 0; i < offsetsX.length; i++) {
            colMarks = sheet.getOMRsByRow(sheet.getRecognizedImage(),
                0.25f, offsetsX[i],
                AttendanceSheetImage.DEFAULT_OMR_OFFSET_Y,
                AttendanceSheetImage.DEFAULT_OM_DISTANCE_X, OM_HEIGHT/2, 
                AttendanceSheetImage.DEFAULT_OM_DISTANCE_Y, 
                numOMRsPerCol, numRows, dc);
            System.arraycopy(colMarks, 0, marks, (i*numRows), numRows);
        }
        
        handleResultsDecoded(marks);

    }
    
    
   public void handleResultsDecoded(boolean[][] opticalMarks) {
        classStudents = loadClassStudentListFromPrefs(
            theClass.id, context);

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
                classStudents[i].username, attendanceVal,classStudents[i].full_name);
        }
        
        view.showResult(attendanceResult);
    }

    public void handleGoBack(){
        view.goBack();
    }
    
    public void handleClickSubmitResults() {
        Thread saveThread = new Thread(new Runnable() {
            public void run() {
                final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                String xAPIServer = impl.getAppPref(
                        UstadMobileSystemImpl.PREFKEY_XAPISERVER,
                        CoreBuildConfig.DEFAULT_XAPI_SERVER, context);
                String registrationUUID = UMTinCanUtil.generateUUID();


                JSONObject teacherStmt = makeAttendendedStmt(
                        theClass.id, theClass.name,
                        impl.getActiveUser(getContext()), xAPIServer,
                        AttendanceController.XAPI_VERB_TEACHER_HOSTED, "hosted", registrationUUID,
                        null, "Ustad Mobile - " + UstadMobileSystemImpl.getInstance().getVersion(context));
                UstadMobileSystemImpl.getInstance().queueTinCanStatement(teacherStmt, context);


                JSONObject studentStmt;
                JSONObject instructorActor = UMTinCanUtil.makeActorFromActiveUser(getContext());

                for(int i = 0; i < attendanceResult.length; i++) {
                    int attendanceStatus = attendanceResult[i].attendanceStatus;
                    if(attendanceStatus >= 0) {
                        studentStmt = makeAttendendedStmt(theClass.id,
                                theClass.name, attendanceResult[i].userId,
                                xAPIServer, VERB_IDS[attendanceStatus], VERB_DISPLAYS[attendanceStatus],
                                registrationUUID, instructorActor, null);
                        UstadMobileSystemImpl.getInstance().queueTinCanStatement(studentStmt, context);
                    }

                }
                impl.getAppView(context).dismissProgressDialog();
                view.finish();
            }
        });
        UstadMobileSystemImpl.getInstance().getAppView(getContext()).showProgressDialog("Saving...");
        saveThread.start();
    }

    
    
    public JSONObject makeAttendendedStmt(String classID, String className, String username, String xapiServer, String verbID, String verbDisplay, String registrationUUID, JSONObject instructor, String contextPlatform) {
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
            if(contextPlatform != null) {
                stmtContext.put("platform", contextPlatform);
            }

            stmt.put("context", stmtContext);
        }catch(JSONException j) {
            
        }
        
        return stmt;
    }

    /**
     * The entry mode : direct or by sheet snap - as per ENTRYMODE_ constants
     *
     * @return The entry mode as per ENTRYMODE_ constants
     */
    public int getEntryMode() {
        return entryMode;
    }

    /**
     * Set the entry mode : direct or by sheet snap .  Must be done before calling handleStartFlow()
     *
     * @param entryMode The entry mode as per ENTRYMODE_ constants
     */
    public void setEntryMode(int entryMode) {
        this.entryMode = entryMode;
    }
}
