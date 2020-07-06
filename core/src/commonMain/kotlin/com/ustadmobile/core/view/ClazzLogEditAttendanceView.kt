package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson


interface ClazzLogEditAttendanceView: UstadEditView<ClazzLog> {

    var clazzLogAttendanceRecordList: DoorMutableLiveData<List<ClazzLogAttendanceRecordWithPerson>>?

    /**
     * The timezone MUST always be set first
     */
    var clazzLogTimezone: String?

    var clazzLogsList: DoorLiveData<List<ClazzLog>>?

    companion object {

        const val VIEW_NAME = "ClazzLogEditAttendanceEditView"

    }

}