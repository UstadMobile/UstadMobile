package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentDetailAssignmentView
import com.ustadmobile.core.view.ClazzAssignmentDetailProgressView
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.lib.db.entities.ClazzAssignment
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
    private lateinit var clazzAssignment: ClazzAssignment

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if(arguments.containsKey(ARG_CLAZZ_ASSIGNMENT_UID)){
            GlobalScope.launch {
                val assignment = clazzAssignmentDao.findByUidAsync(
                                arguments[ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0)
                if (assignment != null) {
                    clazzAssignment = assignment
                    view.runOnUiThread(Runnable {
                        view.setClazzAssignment(clazzAssignment)
                        view.setupTabs(listOf(ClazzAssignmentDetailAssignmentView.VIEW_NAME,
                                ClazzAssignmentDetailProgressView.VIEW_NAME))
                    })

                    //TODO : Get permission and check if logged in person has progress access/ etc

                }
            }
        }
    }

}
