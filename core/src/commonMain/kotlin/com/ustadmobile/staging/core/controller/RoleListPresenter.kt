package com.ustadmobile.staging.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.RoleDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.RoleDetailView
import com.ustadmobile.staging.core.view.RoleListView
import com.ustadmobile.staging.core.view.RoleListView.Companion.ROLE_UID
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Presenter for RoleList view
 */
class RoleListPresenter(context: Any, arguments: Map<String, String>?, view: RoleListView,
                        val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<RoleListView>(context, arguments!!, view) {

    private var umProvider: DataSource.Factory<Int, Role>? = null
    internal var repository: UmAppDatabase
    private val providerDao: RoleDao


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        providerDao = repository.roleDao


    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //Get provider
        umProvider = providerDao.findAllActiveRoles()
        view.setListProvider(umProvider!!)

    }

    fun handleEditRole(roleUid: Long) {
        val args = HashMap<String, String>()
        args.put(ROLE_UID, roleUid.toString())
        impl.go(RoleDetailView.VIEW_NAME, args, context)
    }

    fun handleRoleDelete(roleUid: Long) {
        GlobalScope.launch {
            repository.roleDao.inactiveRoleAsync(roleUid)
        }
    }

    fun handleClickPrimaryActionButton() {

        val args = HashMap<String, String>()
        impl.go(RoleDetailView.VIEW_NAME, args, context)
    }


}
