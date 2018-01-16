package com.ustadmobile.port.sharedse.model;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.ListableEntity;
import com.ustadmobile.lib.util.UMUtil;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsEndpoint;
import com.ustadmobile.nanolrs.core.manager.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatement;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.port.sharedse.controller.AttendanceController;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mike on 21/11/16.
 */

public class AttendanceListEntry implements ListableEntity {

    private XapiStatement hostedStatement;

    private int syncStatus = 0;

    private String classId;

    private Object context;

    //Integer of students in each attendance category e.g. use studentsByAttendanceStatus[ATTENDANCE_PRESENT] etc.
    private int[] studentsByAttendanceStatus = new int[]{0, 0, 0, 0};

    public AttendanceListEntry(String classId, XapiStatement hostedStatement, Object context) {
        this.classId = classId;
        this.hostedStatement = hostedStatement;
        this.context = context;
    }

    public void loadDetail() {
        XapiForwardingStatementManager forwardingMgr = PersistenceManager.getInstance().getManager(XapiForwardingStatementManager.class);
        if(forwardingMgr.findStatusByXapiStatement(context, hostedStatement) == XapiForwardingStatement.STATUS_SENT) {
            syncStatus = ListableEntity.STATUSICON_SENT;
        }else {
            syncStatus = ListableEntity.STATUSICON_SENDING;
        }

        if(hostedStatement.getContextRegistration() == null) {
            return;//old version that did not process the registration - nothing we can do here.
        }

        List<? extends XapiStatement> allStmtList = XapiStatementsEndpoint.getStatements(context,
                null, null, null, null, null, hostedStatement.getContextRegistration(), false,
                false, -1, -1, 0);
        Iterator<? extends XapiStatement> iterator = allStmtList.iterator();

        XapiStatement stmt;
        String verbId;
        while (iterator.hasNext()) {
            stmt = iterator.next();
            verbId = stmt.getVerb() != null ? stmt.getVerb().getVerbId() : null;

            if(verbId == null)
                continue;

            for(int i = 0; i < AttendanceController.VERB_IDS.length; i++) {
                if(verbId.equals(AttendanceController.VERB_IDS[i])) {
                    studentsByAttendanceStatus[i]++;
                    break;
                }
            }
        }

    }


    @Override
    public String getTitle() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(hostedStatement.getTimestamp());
        StringBuffer sb = new StringBuffer();
        sb.append(cal.get(Calendar.DAY_OF_MONTH)).append('/');
        sb.append(cal.get(Calendar.MONTH)+1).append('/');
        sb.append(cal.get(Calendar.YEAR)).append(' ');
        sb.append(cal.get(Calendar.HOUR_OF_DAY)).append(':');
        sb.append(UMUtil.pad0(cal.get(Calendar.MINUTE)));

        return sb.toString();
    }

    @Override
    public String getDetail() {
        return studentsByAttendanceStatus[AttendanceController.VERB_IDS_ATTENDED] + " Present, " +
                studentsByAttendanceStatus[AttendanceController.VERB_IDS_ABSENT] + " Absent";
    }

    @Override
    public String getId() {
        return hostedStatement.getUuid();
    }

    @Override
    public String getStatusText(Object context) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        return syncStatus == ListableEntity.STATUSICON_SENT ?
                impl.getString(MessageID.sent, context) : impl.getString(MessageID.sending, context);
    }

    @Override
    public int getStatusIconCode() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }
}
