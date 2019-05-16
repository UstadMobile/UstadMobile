package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.AuditLog;
import com.ustadmobile.lib.db.entities.AuditLogWithNames;

import java.util.List;


/**
 * Core View. Screen is for AuditLogList's View
 */
public interface AuditLogListView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "AuditLogList";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();


    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    void setListProvider(UmProvider<AuditLogWithNames> listProvider);

    /**
     * Generate csv report from the View which has the table data
     */
    void generateCSVReport(List<String[]> data);


}

