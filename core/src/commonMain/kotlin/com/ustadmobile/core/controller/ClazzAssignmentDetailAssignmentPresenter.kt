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
    private var clazzAssignmentContentJoinDao = repository.clazzAssignmentContentJoinDao
    private lateinit var clazzAssignment : ClazzAssignment
    private lateinit var factory: DataSource.Factory<Int, ContentEntryWithMetrics>

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if(arguments.containsKey(UstadView.ARG_CLAZZ_ASSIGNMENT_UID)){
            val clazzAssignmentUid = arguments[UstadView.ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0
            GlobalScope.launch {
                val assignment = clazzAssignmentDao.findWithMetricByUidAsync(clazzAssignmentUid)
                if(assignment != null){
                    clazzAssignment = assignment
                    getAndSetProvider()
                    view.runOnUiThread(Runnable {
                        view.setClazzAssignment(clazzAssignment)
                    })

                    //TODO: Find if edit permission and view/show
                    view.runOnUiThread(Runnable {
                        view.setEditVisibility(true)
                    })
                }
            }
        }
    }


    private fun getAndSetProvider() {
        factory = clazzAssignmentContentJoinDao.findContentByAssignmentUid(
                clazzAssignment.clazzAssignmentUid)
        view.runOnUiThread(Runnable {
            view.setListProvider(factory)
        })
    }

    fun handleClickEdit(){
        val args = mapOf(UstadView.ARG_CLAZZ_ASSIGNMENT_UID
                to clazzAssignment.clazzAssignmentUid.toString())
        impl.go(ClazzAssignmentEditView.VIEW_NAME, args, context)
    }


}
