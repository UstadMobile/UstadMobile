package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface ReportSELView extends UstadView {
    String VIEW_NAME = "ReportSELView";

    void finish();

    void generateCSVReport();

    void generateXLSReport();

    void updateTables(LinkedHashMap<String, LinkedHashMap<String, Map<Long, List<Long>>>> clazzMap,
                      HashMap<String, List<ClazzMemberWithPerson>> clazzToStudents);

}
