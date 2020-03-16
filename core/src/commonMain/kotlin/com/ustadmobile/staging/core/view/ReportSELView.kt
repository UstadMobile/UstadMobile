package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson

interface ReportSELView : UstadView {

    /**
     * Finishes the screen.
     */
    fun finish()

    /**
     * Generates a CSV report from the sel report and table data in the view and starts the share
     * intent on the platform.
     */
    fun generateCSVReport()

    /**
     * Generates an XLS report from the sel report and table data in the view and starts the share
     * intent on the platform.
     * @param reportPath    The report file xlsx path
     */
    fun generateXLSReport(reportPath: String)


    /**
     * Updates raw sel data and initiates creating the table sel report on the view.
     * @param clazzMap          The raw sel report data in a map grouped by clazz, further grouped
     * by questions and further by nominator -> nominee list
     * @param clazzToStudents   A map of every clazz and its clazz members for the view to construct
     * the report easily.
     */
    fun createTables(clazzMap: LinkedHashMap<String, LinkedHashMap<String, HashMap<Long,
            ArrayList<Long>>>>, clazzToStudents: HashMap<String, List<ClazzMemberWithPerson>>)

    companion object {
        val VIEW_NAME = "ReportSELView"
    }

}
