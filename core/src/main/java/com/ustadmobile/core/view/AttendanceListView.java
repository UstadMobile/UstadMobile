package com.ustadmobile.core.view;

/**
 * Created by mike on 20/11/16.
 */

public interface AttendanceListView extends EntityListView {

    void updateStatus(String hostedStatementId, int status, String statusMessage);

}
