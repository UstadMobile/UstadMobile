package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class ClazzAssignmentDetailStudentProgressPresenter(context: Any, arguments: Map<String, String>, view: ClazzAssignmentDetailStudentProgressView,
                                                    di: DI, lifecycleOwner: DoorLifecycleOwner,
                                                    private val clazzAssignmentItemListener: DefaultClazzAssignmentDetailStudentProgressItemListener = DefaultClazzAssignmentDetailStudentProgressItemListener(view, ListViewMode.BROWSER, di.direct.instance(), context))
    : UstadListPresenter<ClazzAssignmentDetailStudentProgressView, ClazzAssignment>(context, arguments, view, di, lifecycleOwner), ClazzAssignmentDetailStudentProgressItemListener by clazzAssignmentItemListener {

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        clazzAssignmentItemListener.listViewMode = mListMode
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false
    }

    private fun updateListOnView() {
        /* TODO: Update the list on the view from the appropriate DAO query, e.g.
        view.list = when(sortOrder) {
            SortOrder.ORDER_NAME_ASC -> repo.daoName.findAllActiveClazzesSortByNameAsc(
                    searchQuery, loggedInPersonUid)
            SortOrder.ORDER_NAME_DSC -> repo.daoName.findAllActiveClazzesSortByNameDesc(
                    searchQuery, loggedInPersonUid)
        }
        */
    }

    override fun handleClickCreateNewFab() {

    }

}