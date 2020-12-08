package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson


interface ClazzLogEditAttendanceView: UstadEditView<ClazzLog> {

    var clazzLogAttendanceRecordList: DoorMutableLiveData<List<ClazzLogAttendanceRecordWithPerson>>?

    /**
     * The timezone MUST always be set first
     */
    var clazzLogTimezone: String?

    var clazzLogsList: List<ClazzLog>?

    companion object {

        const val VIEW_NAME = "ClazzLogEditAttendanceEditView"

        /**
         * When a new clazzlog is provided as an argument, it will be added to the list of available
         * clazzlogs from which the user can select. This
         */
        const val ARG_NEW_CLAZZLOG = "newclazzlog"

    }

}