package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*

interface ClazzEdit2View: UstadEditView<ClazzWithHolidayCalendarAndSchoolAndTerminology> {

    var clazzSchedules: DoorMutableLiveData<List<Schedule>>?

    var courseBlocks: DoorMutableLiveData<List<CourseBlockWithEntity>>?

    var clazzEndDateError: String?

    var clazzStartDateError: String?

    var scopedGrants: DoorLiveData<List<ScopedGrantAndName>>?

    var coursePicturePath: String?

    var coursePicture: CoursePicture?

    companion object {

        const val VIEW_NAME = "ClassEditView"

    }

}