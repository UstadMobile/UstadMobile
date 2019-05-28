package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.util.LinkedHashMap;
import java.util.List;

public interface ReportAtRiskStudentsView extends UstadView {

    /**
     *  The view name
     */
    String VIEW_NAME="ReportAtRiskStudentsView";

    /**
     * finishes the activity.
     */
    void finish();

    /**
     * Reporting: to export to CSV
     */
    void generateCSVReport();

    /**
     * Starts the process of report generation and renders it to the view with the raw data
     *  supplied to its argument.
     * @param dataMaps  The raw data (usually from the database via the presenter.
     */
    void updateTables(LinkedHashMap<String, List<PersonWithEnrollment>> dataMaps);

    void setTableTextData(List<String[]> tableTextData);

    /**
     * Sets report provider to view.
     * @param provider
     */
    void setReportProvider(UmProvider<PersonWithEnrollment> provider);


    void generateXLSXReport(String xlsxReportPath);
}
