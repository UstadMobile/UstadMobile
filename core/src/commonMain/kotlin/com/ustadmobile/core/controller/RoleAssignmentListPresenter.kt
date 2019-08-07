package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl



import com.ustadmobile.core.view.RoleAssignmentListView
import com.ustadmobile.core.view.RoleAssignmentDetailView

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.EntityRoleWithGroupName

import com.ustadmobile.core.db.dao.EntityRoleDao


import com.ustadmobile.core.view.RoleAssignmentDetailView.Companion.ENTITYROLE_UID

/**
 * Presenter for RoleAssignmentList view
 */
class RoleAssignmentListPresenter(context: Any, arguments: Map<String, String>?,
                                  view: RoleAssignmentListView,
                                  val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<RoleAssignmentListView>(context, arguments!!, view) {

    private var umProvider: UmProvider<EntityRoleWithGroupName>? = null
    internal var repository: UmAppDatabase
    private val providerDao: EntityRoleDao


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        providerDao = repository.entityRoleDao


    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //Get provider
        umProvider = providerDao.findAllActiveRoleAssignments()
        view.setListProvider(umProvider!!)

    }

    fun handleEditRoleAssignment(roleEntityUid: Long) {
        val args = HashMap<String, String>()
        args.put(ENTITYROLE_UID, roleEntityUid)
        impl.go(RoleAssignmentDetailView.VIEW_NAME, args, context)
    }

    fun handleDeleteRoleAssignment(roleEntityUid: Long) {
        providerDao.inavtivateEntityRoleAsync(roleEntityUid, null!!)
    }

    fun handleClickPrimaryActionButton() {

        val args = HashMap<String, String>()
        impl.go(RoleAssignmentDetailView.VIEW_NAME, args, context)
    }


}
