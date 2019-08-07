package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl



import com.ustadmobile.core.view.GroupListView
import com.ustadmobile.core.view.GroupDetailView

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.GroupWithMemberCount
import com.ustadmobile.lib.db.entities.PersonGroup

import com.ustadmobile.core.db.dao.PersonGroupDao

import com.ustadmobile.core.view.GroupDetailView.Companion.GROUP_UID

/**
 * Presenter for GroupList view
 */
class GroupListPresenter(context: Any, arguments: Map<String, String>?, view: GroupListView,
                         val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<GroupListView>(context, arguments!!, view) {

    private var umProvider: UmProvider<GroupWithMemberCount>? = null
    internal var repository: UmAppDatabase
    private val providerDao: PersonGroupDao


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        providerDao = repository.personGroupDao


    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //Get provider
        umProvider = providerDao.findAllActiveGroupsWithoutIndividualGroup()
        view.setListProvider(umProvider!!)

    }

    fun handleEditGroup(uid: Long) {
        val args = HashMap<String, String>()
        args.put(GROUP_UID, uid)
        impl.go(GroupDetailView.VIEW_NAME, args, context)
    }

    fun handleDeleteGroup(uid: Long) {
        providerDao.inactivateGroupAsync(uid, null!!)
    }

    fun handleClickPrimaryActionButton() {

        val args = HashMap<String, String>()
        impl.go(GroupDetailView.VIEW_NAME, args, context)
    }


}
