package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.ClazzAssignmentListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics

/**
 *  Presenter for MyWomenEntrepreneurs view
 **/
class ClazzAssignmentListPresenter(context: Any,
                                   arguments: Map<String, String>?,
                                   view: ClazzAssignmentListView,
                                   val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                                   val repository: UmAppDatabase =
                                           UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<ClazzAssignmentListView>(context, arguments!!, view) {

    private var clazzAssignmentDao = repository.clazzAssignmentDao
    private var clazzUid : Long = 0

    private lateinit var factory: DataSource.Factory<Int, ClazzAssignmentWithMetrics>

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            clazzUid = arguments[ARG_CLAZZ_UID]!!.toLong()
            getAndSetProvider()
        }
    }

    private fun getAndSetProvider() {
        factory = clazzAssignmentDao.findWithMetricsByClazzUid(clazzUid)
        view.setListProvider(factory)
    }

    fun handleClickAssignment(clazzAssignmentUid: Long){
        val args = mapOf(ARG_CLAZZ_ASSIGNMENT_UID to clazzAssignmentUid.toString())
        impl.go(ClazzAssignmentDetailView.VIEW_NAME, args, view.viewContext)
    }

}
