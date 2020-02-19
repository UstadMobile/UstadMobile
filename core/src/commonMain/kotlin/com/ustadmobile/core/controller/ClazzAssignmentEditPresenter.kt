package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.dao.ClazzAssignmentDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithMetrics
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 *  Presenter for ClazzAssignmentEdit view
 **/
class ClazzAssignmentEditPresenter(context: Any,
                                   arguments: Map<String, String>,
                                   view: ClazzAssignmentEditView,
                                   val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                                   val repository: UmAppDatabase =
                                           UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<ClazzAssignmentEditView>(context, arguments, view) {


    private var clazzAssignmentDao = repository.clazzAssignmentDao
    private var clazzAssignmentContentJoinDao  = repository.clazzAssignmentContentJoinDao
    private lateinit var clazzAssignment : ClazzAssignment
    private lateinit var factory: DataSource.Factory<Int, ContentEntryWithMetrics>


    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if(arguments.containsKey(UstadView.ARG_CLAZZ_ASSIGNMENT_UID)){
            GlobalScope.launch {
                val assignment = clazzAssignmentDao.findByUidAsync(
                        arguments[UstadView.ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0)
                if (assignment != null) {
                    clazzAssignment = assignment
                    view.runOnUiThread(Runnable {
                        view.setClazzAssignment(clazzAssignment)
                    })
                    getAndSetProvider()
                }
            }
        }
    }

    private fun getAndSetProvider() {
        factory = clazzAssignmentContentJoinDao.findContentByAssignmentUid(clazzAssignment.clazzAssignmentUid)
        view.runOnUiThread(Runnable {
            view.setListProvider(factory)
        })
    }


    fun handleSaveAssignment(assignment: ClazzAssignment){
        GlobalScope.launch {
            if(assignment.clazzAssignmentUid != 0L){
                clazzAssignmentDao.insertAsync(assignment)
            }else{
                clazzAssignmentDao.updateAsync(assignment)
            }
        }
    }

    /**
     * Handle what happens when you click add Content button
     */
    fun handleClickAddContent(){
        //TODO: Figure this
    }

}
