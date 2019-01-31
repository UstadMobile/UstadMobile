package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface ReportSELView extends UstadView {
    String VIEW_NAME = "ReportSELView";

    /**
     * Finishes the screen.
     */
    void finish();

    /**
     * Generates a CSV report from the sel report and table data in the view and starts the share
     * intent on the platform.
     */
    void generateCSVReport();

    /**
     * Generates an XLS report from the sel report and table data in the view and starts the share
     * intent on the platform.
     */
    void generateXLSReport();

    /**
     * Updates raw sel data and initiates creating the table sel report on the view.
     * @param clazzMap          The raw sel report data in a map grouped by clazz, further grouped
     *                          by questions and further by nominator -> nominee list
     * @param clazzToStudents   A map of every clazz and its clazz members for the view to construct
     *                          the report easily.
     */
    void updateTables(LinkedHashMap<String, LinkedHashMap<String, Map<Long, List<Long>>>> clazzMap,
                      HashMap<String, List<ClazzMemberWithPerson>> clazzToStudents);

}
