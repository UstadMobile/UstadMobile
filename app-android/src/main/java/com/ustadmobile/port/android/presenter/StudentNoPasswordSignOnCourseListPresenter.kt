package com.ustadmobile.port.android.presenter

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.port.android.view.StudentNoPasswordSignOnCourseListView
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.port.android.view.StudentNoPasswordSignOnStudentListView

class StudentNoPasswordSignOnCourseListPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: StudentNoPasswordSignOnCourseListView,
    di: DI,
): UstadBaseController<StudentNoPasswordSignOnCourseListView>(context, arguments, view, di) {

    data class EndpointNoPasswordCourseList(
        val endpoint: Endpoint,
        val courseList: DataSourceFactory<Int, Clazz>
    )

    override fun onCreate(savedState: Map<String, String>?) {
        val activeEndpoints = EndpointScope.Default.activeEndpointSet
        val timeNow = systemTimeInMillis()
        view.coursesLists = activeEndpoints.map { endpoint ->
            EndpointNoPasswordCourseList(
                endpoint = endpoint,
                courseList = di.on(endpoint).direct.instance<UmAppDatabase>(
                    tag = DoorTag.TAG_DB
                ).clazzDao.findStudentNoPasswordSignOnClazzes(timeNow)
            )
        }.toList()
    }

    fun onClickCourse(endpoint: Endpoint, course: Clazz) {
        requireNavController().navigate(
            StudentNoPasswordSignOnStudentListView.VIEW_NAME,
            mapOf(UstadView.ARG_ACCOUNT_ENDPOINT to endpoint.url,
                UstadView.ARG_CLAZZUID to course.clazzUid.toString()),
        )
    }

}