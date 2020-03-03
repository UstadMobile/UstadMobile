package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentDetailProgressView
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.PersonWithAssignmentMetrics
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 *  Presenter for ClazzAssignmentDetail view
 **/
class ClazzAssignmentDetailProgressPresenter(context: Any,
                                             arguments: Map<String, String>,
                                             view: ClazzAssignmentDetailProgressView,
                                             val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                                             val repository: UmAppDatabase =
                                             UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<ClazzAssignmentDetailProgressView>(context, arguments, view) {


    private var clazzAssignmentDao = repository.clazzAssignmentDao
    private lateinit var clazzAssignment : ClazzAssignmentWithMetrics
    private lateinit var factory: DataSource.Factory<Int, PersonWithAssignmentMetrics>

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        if(arguments.containsKey(ARG_CLAZZ_ASSIGNMENT_UID)){
            val clazzAssignmentUid = arguments[ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0
            GlobalScope.launch {
                val assignment = clazzAssignmentDao.findWithMetricByUidAsync(clazzAssignmentUid)
                if(assignment != null){
                    clazzAssignment = assignment
                    getAndSetProvider()
                }
            }
        }
    }

    private fun getAndSetProvider() {
        factory = clazzAssignmentDao.findAllStudentsInAssignmentWithMetrics(
                clazzAssignment.clazzAssignmentUid)
        view.runOnUiThread(Runnable {
            view.setListProvider(factory)
        })
    }

}
