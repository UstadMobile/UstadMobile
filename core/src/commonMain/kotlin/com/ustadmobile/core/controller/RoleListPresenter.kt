package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UmAccountManager

import com.ustadmobile.core.impl.UstadMobileSystemImpl



import com.ustadmobile.core.view.RoleListView
import com.ustadmobile.core.view.RoleDetailView

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.Role

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.RoleDao

import com.ustadmobile.core.view.RoleListView.Companion.ROLE_UID

/**
 * Presenter for RoleList view
 */
class RoleListPresenter(context: Any, arguments: Map<String, String>?, view: RoleListView,
                        val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<RoleListView>(context, arguments!!, view) {

    private var umProvider: UmProvider<Role>? = null
    internal var repository: UmAppDatabase
    private val providerDao: RoleDao


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        providerDao = repository.roleDao


    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //Get provider
        umProvider = providerDao.findAllActiveRoles()
        view.setListProvider(umProvider!!)

    }

    fun handleEditRole(roleUid: Long) {
        val args = HashMap<String, String>()
        args.put(ROLE_UID, roleUid)
        impl.go(RoleDetailView.VIEW_NAME, args, context)
    }

    fun handleRoleDelete(roleUid: Long) {
        repository.roleDao.inactiveRoleAsync(roleUid, null!!)
    }

    fun handleClickPrimaryActionButton() {

        val args = HashMap<String, String>()
        impl.go(RoleDetailView.VIEW_NAME, args, context)
    }


}
