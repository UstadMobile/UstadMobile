package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentDetailProgressView
import com.ustadmobile.core.view.ClazzAssignmentListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics

/**
 *  Presenter for ClazzAssignmentList view
 **/
class ClazzAssignmentListPresenter(context: Any,
                                   arguments: Map<String, String>,
                                   view: ClazzAssignmentListView,
                                   val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                                   val repository: UmAppDatabase =
                                           UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<ClazzAssignmentListView>(context, arguments, view) {

    private var clazzAssignmentDao = repository.clazzAssignmentDao
    private var clazzUid : Long = 0L

    private lateinit var factory: DataSource.Factory<Int, ClazzAssignmentWithMetrics>

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            clazzUid = arguments[ARG_CLAZZ_UID]?.toLong() ?: 0L
            getAndSetProvider()

            //TODO: Figure out the visiblity of edit via permissions
            view.setEditVisibility(true)
        }
    }

    private fun getAndSetProvider() {
        factory = clazzAssignmentDao.findWithMetricsByClazzUid(clazzUid)
        view.setListProvider(factory)
    }

    fun handleClickAssignment(clazzAssignment: ClazzAssignmentWithMetrics){
        val args = mapOf(ARG_CLAZZ_ASSIGNMENT_UID to clazzAssignment.clazzAssignmentUid.toString())
        impl.go(ClazzAssignmentDetailProgressView.VIEW_NAME, args, view.viewContext)
    }

}
