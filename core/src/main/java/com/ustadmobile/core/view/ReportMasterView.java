package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.ReportMasterItem;

import java.util.List;

public interface ReportMasterView extends UstadView {
    String VIEW_NAME="ReportMasterView";
    void finish();
    void generateCSVReport();
    void updateTables(List<ReportMasterItem> items);
}
