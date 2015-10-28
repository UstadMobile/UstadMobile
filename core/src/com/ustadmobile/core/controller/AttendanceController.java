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
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.AttendanceClass;
import com.ustadmobile.core.model.AttendanceClassStudent;
import com.ustadmobile.core.model.AttendanceRowModel;
import com.ustadmobile.core.omr.OMRRecognizer;
import com.ustadmobile.core.view.AttendanceView;
import com.ustadmobile.core.view.UstadView;
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
    
    public static final float OMR_AREA_OFFSET_X = (311f/AREA_WIDTH);
    
    public static final float OMR_AREA_OFFSET_Y = (37.5f/AREA_HEIGHT);
    
    public static final float OM_WIDTH = 26f/AREA_WIDTH;
    
    public static final float OM_HEIGHT = 20f/AREA_HEIGHT;
    
    public static final float OM_ROW_HEIGHT = 25.8f/AREA_HEIGHT;

    private AttendanceRowModel[] attendanceResult; 
    
    private AttendanceView view;
    
    private AttendanceClass[] teacherClasses;
    
    private int selectedClass;
    
    private AttendanceClassStudent[] classStudents;
    
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
        String[] classList = new String[teacherClasses.length];
        for(int i = 0; i < classList.length; i++) {
            classList[i] = teacherClasses[i].name;
        }
        
        view.showClassList(classList);
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

                boolean[][] marks = OMRRecognizer.getMarks(bitmapImg,
                    OMR_AREA_OFFSET_X, OMR_AREA_OFFSET_Y, 
                    OM_WIDTH, OM_HEIGHT, OM_ROW_HEIGHT, 
                    4, 33, null);    
                impl.getAppView(ctrl.getContext()).dismissProgressDialog();
                ctrl.handleResultsDecoded(marks);
            }catch(Exception e) {
                impl.getAppView(ctrl.getContext()).dismissProgressDialog();
                impl.getAppView(ctrl.getContext()).showAlertDialog("Decode failed", "Failed");
            }
        }
    }
    
    
}
