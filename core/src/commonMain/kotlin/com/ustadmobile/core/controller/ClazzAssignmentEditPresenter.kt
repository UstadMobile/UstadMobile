package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzAssignmentContentEntryJoinWithContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithMetrics
import com.ustadmobile.lib.util.getSystemTimeInMillis
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
    private var clazzUid: Long = 0L

    private var allContent = DoorMutableLiveData<List<
            ClazzAssignmentContentEntryJoinWithContentEntry>>(mutableListOf())


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        if(arguments.containsKey(UstadView.ARG_CLAZZ_ASSIGNMENT_UID)){
            GlobalScope.launch {
                val assignment = clazzAssignmentDao.findByUidAsync(
                        arguments[UstadView.ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0)
                if (assignment != null) {
                    clazzAssignment = assignment
                }else{
                    clazzAssignment = ClazzAssignment()
                }

                val contentEntries = clazzAssignmentContentJoinDao.findContentEntryJoinWithContentByAssignment(
                        clazzAssignment.clazzAssignmentUid)
                allContent.sendValue(contentEntries.toMutableList())

                view.runOnUiThread(Runnable {
                    view.setClazzAssignment(clazzAssignment)
                    view.contentEntryList = allContent
                })
            }
        }

        if(arguments.containsKey(UstadView.ARG_CLAZZ_UID)){
            clazzUid = arguments[UstadView.ARG_CLAZZ_UID]?.toLong()?:0
        }
    }

    fun handleSaveAssignment(assignment: ClazzAssignment){
        assignment.clazzAssignmentClazzUid = clazzUid
        assignment.clazzAssignmentInactive = false
        assignment.clazzAssignmentUpdateDate = getSystemTimeInMillis()


        val contentEntryJoins = allContent.getValue()

        GlobalScope.launch {

            if(assignment.clazzAssignmentUid != 0L){
                clazzAssignmentDao.updateAsync(assignment)
            }else{
                assignment.clazzAssignmentCreationDate = getSystemTimeInMillis()
                assignment.clazzAssignmentUid = clazzAssignmentDao.insertAsync(assignment)
            }

            contentEntryJoins?.forEach {
                it.clazzAssignmentContentJoinClazzAssignmentUid = assignment.clazzAssignmentUid
            }

            if(contentEntryJoins != null) {
                val (updates, inserts) = contentEntryJoins.partition { it.clazzAssignmentContentJoinUid != 0L }
                clazzAssignmentContentJoinDao.insertList(inserts)
                clazzAssignmentContentJoinDao.updateList(updates)
            }

            view.finish()
        }
    }

    fun handleContentEntryAdded(contentEntry: ContentEntry){

        val clazzAssignmentCEJoinWithCE = ClazzAssignmentContentEntryJoinWithContentEntry().also {
            it.contentEntry = contentEntry
            it.clazzAssignmentContentJoinContentUid = contentEntry.contentEntryUid
        }

        val currentList = allContent.getValue()
        val newList = currentList?.toMutableList().also {
            it?.add(clazzAssignmentCEJoinWithCE)
        }?: mutableListOf<ClazzAssignmentContentEntryJoinWithContentEntry>()

        allContent.sendValue(newList)
    }



}
