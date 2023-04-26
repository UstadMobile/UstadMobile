package com.ustadmobile.port.android.view

import com.ustadmobile.core.view.UstadView
import com.ustadmobile.port.android.presenter.StudentNoPasswordSignOnCourseListPresenter

interface StudentNoPasswordSignOnCourseListView: UstadView {

    var coursesLists: List<StudentNoPasswordSignOnCourseListPresenter.EndpointNoPasswordCourseList>?

    companion object {

        const val VIEW_NAME = "StudentNoPasswordSignOnCourseList"

    }

}
