package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.ClazzAssignmentListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

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
    private var clazzDao = repository.clazzDao
    private var clazzUid : Long = 0L

    private lateinit var factory: DataSource.Factory<Int, ClazzAssignmentWithMetrics>

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            clazzUid = arguments[ARG_CLAZZ_UID]?.toLong() ?: 0L
            getAndSetProvider()

            //Consider making this live data ?
            val loggedInPersonUid = UmAccountManager.getActivePersonUid(context)
            GlobalScope.launch {
                val assignmentEditPermission = clazzDao.personHasPermissionWithClazz(
                        loggedInPersonUid, clazzUid, Role.PERMISSION_CLAZZ_ASSIGNMENT_READ_WRITE)
                view.runOnUiThread(Runnable {
                    view.setEditVisibility(assignmentEditPermission)
                })
            }
        }
    }

    private fun getAndSetProvider() {
        factory = clazzAssignmentDao.findWithMetricsByClazzUid(clazzUid)
        view.setListProvider(factory)
    }

    fun handleClickAssignment(clazzAssignment: ClazzAssignmentWithMetrics){
        val args = mapOf(ARG_CLAZZ_ASSIGNMENT_UID to clazzAssignment.clazzAssignmentUid.toString(),
                ARG_CLAZZ_UID to clazzUid.toString())
        impl.go(ClazzAssignmentDetailView.VIEW_NAME, args, view.viewContext)
    }

    fun handleClickNewAssignment(){
        val args = mapOf(ARG_CLAZZ_ASSIGNMENT_UID to "0",
                ARG_CLAZZ_UID to clazzUid.toString())
        impl.go(ClazzAssignmentEditView.VIEW_NAME, args , view.viewContext)
    }

}
