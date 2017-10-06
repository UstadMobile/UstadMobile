package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.LoadControllerThread;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.ListableEntity;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsEndpoint;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingEndpoint;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingEvent;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingListener;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.port.sharedse.model.AttendanceListEntry;
import com.ustadmobile.port.sharedse.view.AttendanceListView;
import com.ustadmobile.port.sharedse.view.AttendanceView;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mike on 20/11/16.
 */

public class AttendanceListController extends EntityListController implements XapiStatementsForwardingListener{

    public static final int MAX_ATTENDANCE_ITEMS = 40;

    public AttendanceListController(Object context) {
        super(context);
        XapiStatementsForwardingEndpoint.addQueueStatusListener(this);
    }

    private String classId;

    public static void makeControllerForView(AttendanceListView view, Hashtable args, ControllerReadyListener listener) {
        AttendanceListController controller = new AttendanceListController(view.getContext());
        new LoadControllerThread(args, controller, listener, view).start();
    }

    @Override
    public UstadController loadController(Hashtable args, Object context) throws Exception {
        String classId = args.get(ClassManagementController2.ARG_CLASSID).toString();

        AttendanceListController controller = new AttendanceListController(context);
        controller.setClassId(classId);
        controller.loadAttendanceList();
        return controller;
    }

    public void loadAttendanceList() {
        String attendanceActivityId = UstadMobileConstants.PREFIX_ATTENDANCE_URL + classId;
        List<? extends XapiStatement> classHostedStmts = XapiStatementsEndpoint.getStatements(context,
                null, null, null, AttendanceController.XAPI_VERB_TEACHER_HOSTED,
                attendanceActivityId, null, false, false, -1, -1, MAX_ATTENDANCE_ITEMS);

        getList().clear();
        Iterator<? extends XapiStatement> iterator = classHostedStmts.iterator();
        while(iterator.hasNext()) {
            AttendanceListEntry entry = new AttendanceListEntry(classId, iterator.next(), context);
            entry.loadDetail();
            getList().add(entry);
        }
    }

    @Override
    public void handleViewDestroy() {
        XapiStatementsForwardingEndpoint.removeQueueStatusListener(this);
        super.handleViewDestroy();
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public void handleClickSnapSheet() {
        handleStartAttendanceEntry(AttendanceController.ENTRYMODE_SNAP_SHEET);
    }

    public void handleClickDirectEntry() {
        handleStartAttendanceEntry(AttendanceController.ENTRYMODE_DIRECT_ENTRY);
    }

    private void handleStartAttendanceEntry(int mode) {
        Hashtable args = new Hashtable();
        args.put(AttendanceController.KEY_CLASSID, classId);
        args.put(AttendanceController.ARG_ENTRYMODE, new Integer(mode));
        UstadMobileSystemImpl.getInstance().go(AttendanceView.VIEW_NAME, args, context);
    }


    @Override
    public void queueStatusUpdated(XapiStatementsForwardingEvent event) {

    }

    @Override
    public void queueStatementSent(XapiStatementsForwardingEvent event) {
        for(int i = 0; i < entityList.size(); i++) {
            if(entityList.get(i).getId().equals(event.getStatement().getUuid()) && getView() != null) {
                ((AttendanceListEntry)entityList.get(i)).setSyncStatus(ListableEntity.STATUSICON_SENT);
                ((AttendanceListView)getView()).updateStatus(event.getStatement().getUuid(),
                    AttendanceController.STATUS_ATTENDANCE_SENT,
                    UstadMobileSystemImpl.getInstance().getString(MessageID.sent, getContext()));
            }
        }
    }

    @Override
    public void statementQueued(XapiStatementsForwardingEvent event) {

    }

}
