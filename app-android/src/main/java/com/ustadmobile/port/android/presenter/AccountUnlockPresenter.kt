package com.ustadmobile.port.android.presenter

import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.port.android.view.StudentNoPasswordSignOnCourseListView
import org.kodein.di.DI

class AccountUnlockPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: StudentNoPasswordSignOnCourseListView,
    di: DI,
): UstadBaseController<StudentNoPasswordSignOnCourseListView>(context, arguments, view, di) {



    fun onClickUnlock(password: String) {

    }

}