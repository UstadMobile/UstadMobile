package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentDetailAssignmentView
import com.ustadmobile.core.view.ClazzAssignmentDetailProgressView
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 *  Presenter for ClazzAssignmentDetail view
 **/
class ClazzAssignmentDetailPresenter(context: Any,
                                     arguments: Map<String, String>,
                                     view: ClazzAssignmentDetailView,
                                     val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                                     val repository: UmAppDatabase =
                                             UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<ClazzAssignmentDetailView>(context, arguments, view) {


    private var clazzAssignmentDao = repository.clazzAssignmentDao
    private var clazzDao = repository.clazzDao
    private lateinit var clazzAssignment: ClazzAssignmentWithMetrics
    private var clazzUid = 0L

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        if(arguments.containsKey(UstadView.ARG_CLAZZ_UID)) {
            clazzUid = arguments[UstadView.ARG_CLAZZ_UID]?.toLong() ?: 0L
        }

        if(arguments.containsKey(ARG_CLAZZ_ASSIGNMENT_UID)){
            GlobalScope.launch {

                //Consider making this live data ?
                val loggedInPersonUid = UmAccountManager.getActivePersonUid(context)
                val assignmentEditPermission = clazzDao.personHasPermissionWithClazz(
                        loggedInPersonUid, clazzUid, Role.PERMISSION_CLAZZ_ASSIGNMENT_READ_WRITE)

                val assignment = clazzAssignmentDao.findWithMetricsByUid(
                                arguments[ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0)
                if (assignment != null) {
                    clazzAssignment = assignment
                    view.runOnUiThread(Runnable {
                        view.setClazzAssignment(clazzAssignment)
                        val tabs = mutableListOf<String>()
                        tabs.add(ClazzAssignmentDetailAssignmentView.VIEW_NAME)
                        if(assignmentEditPermission){
                            tabs.add(ClazzAssignmentDetailProgressView.VIEW_NAME)
                        }
                        view.setupTabs(tabs)
                    })
                }
            }
        }
    }
}
