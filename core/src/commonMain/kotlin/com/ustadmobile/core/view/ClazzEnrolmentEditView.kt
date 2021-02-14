package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ClazzEnrolmentEditPresenter
import com.ustadmobile.lib.db.entities.ClazzEnrolment


interface ClazzEnrolmentEditView: UstadEditView<ClazzEnrolment> {

    var roleList: List<ClazzEnrolmentEditPresenter.RoleMessageIdOption>?
    var statusList: List<ClazzEnrolmentEditPresenter.StatusMessageIdOption>?

    var startDateError: String?

    companion object {

        const val VIEW_NAME = "ClazzEnrolmentEditView"

    }

}