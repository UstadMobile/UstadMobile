package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingEndpoint;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingEvent;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingListener;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.port.sharedse.model.AttendanceClass;
import com.ustadmobile.port.sharedse.view.ClassListView;
import com.ustadmobile.port.sharedse.view.ClassManagementView2;

import java.util.Hashtable;

/**
 * Created by varuna on 20/02/16.
 */
@Deprecated
public class ClassListController extends UstadBaseController implements  XapiStatementsForwardingListener{

    private AttendanceClass[] attendanceClasses;

    private ClassListView classListView;

    public ClassListController(Object context) {
        super(context);
        loadClasses();
        XapiStatementsForwardingEndpoint.addQueueStatusListener(this);
    }

    protected void loadClasses(){
        attendanceClasses = AttendanceController.loadTeacherClassListFromPrefs(context);
        for(int i = 0; i < attendanceClasses.length; i++) {
            attendanceClasses[i].syncStatus = AttendanceController.getAttendanceStatusByClassId(context, attendanceClasses[i].id);
        }
    }

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
        args.put(ClassManagementController2.ARG_CLASS_NAME, attendanceClasses[index].getTitle());
        UstadMobileSystemImpl.getInstance().go(ClassManagementView2.VIEW_NAME, args,
                context);
    }

    @Override
    public void handleViewDestroy() {
        XapiStatementsForwardingEndpoint.removeQueueStatusListener(this);
        super.handleViewDestroy();
    }

    @Override
    public void queueStatusUpdated(XapiStatementsForwardingEvent event) {

    }

    @Override
    public void queueStatementSent(XapiStatementsForwardingEvent event) {
        updateViewAttendanceStatus(event, AttendanceController.STATUS_ATTENDANCE_SENT);
    }



    @Override
    public void statementQueued(XapiStatementsForwardingEvent event) {
        updateViewAttendanceStatus(event, AttendanceController.STATUS_ATTENDANCE_TAKEN);
    }


    protected void updateViewAttendanceStatus(XapiStatementsForwardingEvent event, int statusId) {
        XapiStatement stmt = event.getStatement();
        if(stmt.getVerb().getVerbId().equals(AttendanceController.XAPI_VERB_TEACHER_HOSTED)) {
            String activityId = stmt.getActivity().getActivityId();
            for(int i = 0; i < attendanceClasses.length; i++) {
                if(activityId.equals(UstadMobileConstants.PREFIX_ATTENDANCE_URL + attendanceClasses[i].id)) {
                    attendanceClasses[i].syncStatus = statusId;
                    classListView.setClassStatus(attendanceClasses[i].id, statusId,
                            attendanceClasses[i].getStatusText(getContext()));
                }
            }
        }
    }

}
