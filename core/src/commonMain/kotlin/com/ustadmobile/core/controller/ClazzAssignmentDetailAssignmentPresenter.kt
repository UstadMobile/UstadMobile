package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentDetailAssignmentView
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntryWithMetrics
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 *  Presenter for ClazzAssignmentDetailAssignment view
 **/
class ClazzAssignmentDetailAssignmentPresenter(context: Any,
                                               arguments: Map<String, String>,
                                               view: ClazzAssignmentDetailAssignmentView,
                                               val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                                               val repository: UmAppDatabase =
                                                       UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<ClazzAssignmentDetailAssignmentView>(context, arguments, view) {


    private var clazzAssignmentDao = repository.clazzAssignmentDao
    private var clazzDao = repository.clazzDao
    private var clazzAssignmentContentJoinDao = repository.clazzAssignmentContentJoinDao
    private lateinit var clazzAssignment : ClazzAssignment
    private lateinit var factory: DataSource.Factory<Int, ContentEntryWithMetrics>
    private var clazzUid = 0L

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if(arguments.containsKey(UstadView.ARG_CLAZZ_UID)) {
            clazzUid = arguments[UstadView.ARG_CLAZZ_UID]?.toLong() ?: 0L
        }

        if(arguments.containsKey(UstadView.ARG_CLAZZ_ASSIGNMENT_UID)){
            val clazzAssignmentUid = arguments[UstadView.ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0

            GlobalScope.launch {
                val assignment = clazzAssignmentDao.findWithMetricByUidAsync(clazzAssignmentUid)

                getAndSetProvider(assignment)

                val loggedInPersonUid = UmAccountManager.getActivePersonUid(context)
                val assignmentEditPermission = clazzDao.personHasPermissionWithClazz(
                        loggedInPersonUid, clazzUid, Role.PERMISSION_CLAZZ_ASSIGNMENT_READ_WRITE)

                if(assignment != null){
                    clazzAssignment = assignment
                    view.runOnUiThread(Runnable {
                        view.setEditVisibility(assignmentEditPermission)
                        view.setClazzAssignment(clazzAssignment)
                    })
                }
            }
        }
    }

    private fun getAndSetProvider(assignment: ClazzAssignment?) {
        val loggedInPersonUid = UmAccountManager.getActivePersonUid(context)
        factory = clazzAssignmentContentJoinDao.findContentByAssignmentUid(
                assignment?.clazzAssignmentUid?:0, assignment?.clazzAssignmentStartDate?:0,
                assignment?.clazzAssignmentDueDate?:0, loggedInPersonUid)
        view.runOnUiThread(Runnable {
            view.setListProvider(factory)
        })
    }

    fun handleClickEdit(){
        val args = mapOf(UstadView.ARG_CLAZZ_ASSIGNMENT_UID to arguments[UstadView.ARG_CLAZZ_ASSIGNMENT_UID],
                UstadView.ARG_CLAZZ_UID to arguments[UstadView.ARG_CLAZZ_UID])
        impl.go(ClazzAssignmentEditView.VIEW_NAME, args, context)
    }


}
