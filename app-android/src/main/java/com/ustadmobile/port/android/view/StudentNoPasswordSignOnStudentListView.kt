package com.ustadmobile.port.android.view

import com.ustadmobile.core.view.UstadView
import com.ustadmobile.port.android.presenter.StudentNoPasswordSignOnStudentListPresenter

interface StudentNoPasswordSignOnStudentListView: UstadView {

    var studentList: StudentNoPasswordSignOnStudentListPresenter.NoPasswordStudentList?

    companion object {

        const val VIEW_NAME = "StudentNoPasswordSignOnStudentList"

    }
}