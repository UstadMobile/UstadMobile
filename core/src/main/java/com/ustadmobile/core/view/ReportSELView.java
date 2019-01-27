package com.ustadmobile.core.view;

import java.util.List;

public interface ReportSELView extends UstadView {
    String VIEW_NAME = "ReportSELView";

    void finish();

    void generateCSVReport();

    void generateXLSReport();

    void updateTables(List items);

}
