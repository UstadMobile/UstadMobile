package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ClazzEdit2Presenter
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.lib.db.entities.*

interface ClazzEdit2View: UstadEditView<ClazzWithHolidayCalendarAndSchoolAndTerminology> {

    var clazzSchedules: MutableLiveData<List<Schedule>>?

    var courseBlocks: MutableLiveData<List<CourseBlockWithEntity>>?

    var clazzEndDateError: String?

    var clazzStartDateError: String?

    var coursePicture: CoursePicture?

    var enrolmentPolicyOptions: List<ClazzEdit2Presenter.EnrolmentPolicyOptionsMessageIdOption>?

    companion object {

        const val VIEW_NAME = "CourseEditView"

    }

}