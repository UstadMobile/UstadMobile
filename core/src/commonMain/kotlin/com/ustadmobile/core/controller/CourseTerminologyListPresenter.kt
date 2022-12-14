package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.CourseTerminologyEditView
import com.ustadmobile.core.view.CourseTerminologyListView
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI

class CourseTerminologyListPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: CourseTerminologyListView,
    di: DI,
    lifecycleOwner: LifecycleOwner)
    : UstadListPresenter<CourseTerminologyListView, CourseTerminology>(
        context,
        arguments,
        view,
        di,
        lifecycleOwner),
    CourseTerminologyListItemListener {


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    private fun updateListOnView() {
        presenterScope.launch {
            view.list = repo.courseTerminologyDao.findAllCourseTerminology()
        }
    }

    override fun handleClickCreateNewFab() {
        navigateForResult(
            NavigateForResultOptions(this,
                null,
                CourseTerminologyEditView.VIEW_NAME,
                CourseTerminology::class,
                CourseTerminology.serializer())
        )
    }

    override fun onClickCourseTerminology(courseTerminology: CourseTerminology) {
        finishWithResult(
            safeStringify(di,
            ListSerializer(CourseTerminology.serializer()), listOf(courseTerminology))
        )
    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {
        handleClickCreateNewFab()
    }

}