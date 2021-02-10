package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ClazzEnrollmentEditPresenter
import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzEnrollment


interface ClazzEnrollmentEditView: UstadEditView<ClazzEnrollment> {

    var roleList: List<ClazzEnrollmentEditPresenter.RoleMessageIdOption>?
    var statusList: List<ClazzEnrollmentEditPresenter.StatusMessageIdOption>?

    var startDateError: String?

    companion object {

        const val VIEW_NAME = "ClazzEnrollmentEditView"

    }

}