package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.ContentEntryListView
import com.ustadmobile.core.view.ContentEntryListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorMutableLiveData
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
    private var clazzUid: Long = 0L

    private var allContent = DoorMutableLiveData<MutableList<ContentEntry>>(mutableListOf())


    override fun onCreate(savedState: Map<String, String?>?) {
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
                view.runOnUiThread(Runnable {
                    view.setClazzAssignment(clazzAssignment)
                })
                getAndSetProvider()
            }
        }

        if(arguments.containsKey(UstadView.ARG_CLAZZ_UID)){
            clazzUid = arguments[UstadView.ARG_CLAZZ_UID]?.toLong()?:0
        }
    }

    private fun getAndSetProvider() {
        factory = clazzAssignmentContentJoinDao.findContentByAssignmentUid(
                clazzAssignment.clazzAssignmentUid)
        view.runOnUiThread(Runnable {
            view.setListProvider(factory)
        })
    }

    fun handleSaveAssignment(assignment: ClazzAssignment){
        assignment.clazzAssignmentClazzUid = clazzUid
        assignment.clazzAssignmentInactive = false
        assignment.clazzAssignmentUpdateDate = UMCalendarUtil.getDateInMilliPlusDays(0)

        GlobalScope.launch {
            if(assignment.clazzAssignmentUid != 0L){
                clazzAssignmentDao.updateAsync(assignment)
            }else{
                assignment.clazzAssignmentCreationDate = UMCalendarUtil.getDateInMilliPlusDays(0)
                clazzAssignmentDao.insertAsync(assignment)
            }
            view.finish()
        }
    }

    fun handleContentEntryAdded(contentEntry: ContentEntry){
        //TODO: Update RV for the selected.

        val currentList = allContent.getValue()
        val newList = currentList?.toMutableList().also {
            it?.add(contentEntry)
        }?: mutableListOf<ContentEntry>()

        allContent.sendValue(newList)

    }

}
