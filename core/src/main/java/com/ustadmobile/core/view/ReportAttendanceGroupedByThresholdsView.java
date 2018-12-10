package com.ustadmobile.core.view;


import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao.AttendanceResultGroupedByAgeAndThreshold;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * ReportNumberOfDaysClassesOpen Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ReportAttendanceGroupedByThresholdsView extends UstadView {

    String VIEW_NAME = "ReportAttendanceGroupedByThresholds";


    void updateTables(LinkedHashMap<String, List<AttendanceResultGroupedByAgeAndThreshold>> dataMaps);

    /**
     * For Android: closes the activity.
     */
    void finish();

}
