package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonGroupDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.GroupDetailView
import com.ustadmobile.core.view.GroupDetailView.Companion.GROUP_UID
import com.ustadmobile.core.view.GroupListView
import com.ustadmobile.lib.db.entities.GroupWithMemberCount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Presenter for GroupList view
 */
class GroupListPresenter(context: Any, arguments: Map<String, String>?, view: GroupListView,
                         val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<GroupListView>(context, arguments!!, view) {

    private var umProvider: DataSource.Factory<Int, GroupWithMemberCount>? = null
    internal var repository: UmAppDatabase
    private val providerDao: PersonGroupDao


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        providerDao = repository.personGroupDao


    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        //Get provider
        umProvider = providerDao.findAllActiveGroupsWithoutIndividualGroup()
        view.setListProvider(umProvider!!)

    }

    fun handleEditGroup(uid: Long) {
        val args = HashMap<String, String>()
        args.put(GROUP_UID, uid.toString())
        impl.go(GroupDetailView.VIEW_NAME, args, context)
    }

    fun handleDeleteGroup(uid: Long) {
        GlobalScope.launch {
            providerDao.inactivateGroupAsync(uid)
        }
    }

    fun handleClickPrimaryActionButton() {

        val args = HashMap<String, String>()
        impl.go(GroupDetailView.VIEW_NAME, args, context)
    }


}
