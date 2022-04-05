package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.view.CourseGroupSetDetailView
import com.ustadmobile.core.view.CourseGroupSetEditView
import com.ustadmobile.core.view.CourseGroupSetListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class CourseGroupSetListPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: CourseGroupSetListView,
    di: DI,
    lifecycleOwner: DoorLifecycleOwner
) : UstadListPresenter<CourseGroupSetListView, CourseGroupSet>(
    context,
    arguments,
    view,
    di,
    lifecycleOwner
), CourseGroupSetListItemListener {


    private var clazzUidFilter: Long = 0

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        clazzUidFilter = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: 0
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return repo.clazzDao.personHasPermissionWithClazz(
            account?.personUid?: 0, clazzUidFilter, Role.PERMISSION_CLAZZ_INSERT)
    }

    private fun updateListOnView() {
        view.list = repo.courseGroupSetDao.findAllCourseGroupSetForClazz(clazzUidFilter)
    }

    override fun handleClickCreateNewFab() {
        navigateForResult(
            NavigateForResultOptions(this,
                null,
                CourseGroupSetEditView.VIEW_NAME,
                CourseGroupSet::class,
                CourseGroupSet.serializer(),
                arguments = mutableMapOf(
                    UstadView.ARG_CLAZZUID to clazzUidFilter.toString()))
        )
    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {
        handleClickCreateNewFab()
    }

    override fun onClickCourseGroupSet(courseGroupSet: CourseGroupSet) {
        systemImpl.go(
            CourseGroupSetDetailView.VIEW_NAME,
            mapOf(
                UstadView.ARG_ENTITY_UID to courseGroupSet.cgsUid.toString(),
                UstadView.ARG_CLAZZUID to courseGroupSet.cgsClazzUid.toString()
            ), context
        )
    }

}