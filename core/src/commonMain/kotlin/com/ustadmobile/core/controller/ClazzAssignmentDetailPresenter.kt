package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentDetailView
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
class ClazzAssignmentDetailPresenter(context: Any,
                                     arguments: Map<String, String>?,
                                     view: ClazzAssignmentDetailView,
                                     val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                                     val repository: UmAppDatabase =
                                             UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<ClazzAssignmentDetailView>(context, arguments!!, view) {


    private var clazzAssignmentDao = repository.clazzAssignmentDao
    private var clazzAssignment : ClazzAssignmentWithMetrics ? = null
    private lateinit var factory: DataSource.Factory<Int, PersonWithAssignmentMetrics>

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        if(arguments.containsKey(ARG_CLAZZ_ASSIGNMENT_UID)){
            val clazzAssignmentUid = arguments[ARG_CLAZZ_ASSIGNMENT_UID]!!.toLong()
            GlobalScope.launch {
                clazzAssignment = clazzAssignmentDao.findWithMetricByUidAsync(clazzAssignmentUid)
                if(clazzAssignment != null){
                    getAndSetProvider()
                    view.runOnUiThread(Runnable {
                        view.setClazzAssignment(clazzAssignment!!)
                    })
                }
            }
        }
    }

    private fun getAndSetProvider() {
        factory = clazzAssignmentDao.findAllStudentsInAssignmentWithMetrics(
                clazzAssignment!!.clazzAssignmentUid)
        view.runOnUiThread(Runnable {
            view.setListProvider(factory)
        })
    }

    /**
     * Handle what happens when the edit button is clicked  - should go to assignment edit
     */
    fun handleClickEdit(){
        val args = mapOf(ARG_CLAZZ_ASSIGNMENT_UID to
                clazzAssignment!!.clazzAssignmentUid.toString())
        impl.go(ClazzAssignmentEditView.VIEW_NAME, args, context)
    }

}
