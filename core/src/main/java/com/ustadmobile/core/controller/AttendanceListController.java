package com.ustadmobile.core.controller;

import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.AttendanceListEntry;
import com.ustadmobile.core.model.ListableEntity;
import com.ustadmobile.core.view.AttendanceListView;
import com.ustadmobile.core.view.AttendanceView;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsEndpoint;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingEndpoint;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingEvent;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingListener;
import com.ustadmobile.nanolrs.core.model.XapiStatementProxy;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mike on 20/11/16.
 */

public class AttendanceListController extends EntityListController implements XapiStatementsForwardingListener{

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
        List<? extends XapiStatementProxy> classHostedStmts = XapiStatementsEndpoint.getStatements(context,
                null, null, null, AttendanceController.XAPI_VERB_TEACHER_HOSTED,
                attendanceActivityId, null, false, false, -1, -1, 0);

        getList().clear();
        Iterator<? extends XapiStatementProxy> iterator = classHostedStmts.iterator();
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
        Hashtable args = new Hashtable();
        args.put(AttendanceController.KEY_CLASSID, classId);
        UstadMobileSystemImpl.getInstance().go(AttendanceView.class, args, context);
    }


    @Override
    public void queueStatusUpdated(XapiStatementsForwardingEvent event) {

    }

    @Override
    public void queueStatementSent(XapiStatementsForwardingEvent event) {
        for(int i = 0; i < entityList.size(); i++) {
            if(entityList.get(i).getId().equals(event.getStatement().getId()) && getView() != null) {
                ((AttendanceListEntry)entityList.get(i)).setSyncStatus(ListableEntity.STATUSICON_SENT);
                ((AttendanceListView)getView()).updateStatus(event.getStatement().getId(),
                    AttendanceController.STATUS_ATTENDANCE_SENT,
                    UstadMobileSystemImpl.getInstance().getString(MessageIDConstants.sent));
            }
        }
    }

    @Override
    public void statementQueued(XapiStatementsForwardingEvent event) {

    }

}
