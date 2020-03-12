package com.ustadmobile.core.controller


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.RoleDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.RoleDetailView
import com.ustadmobile.core.view.RoleListView.Companion.ROLE_UID
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


/**
 * Presenter for RoleDetail view
 */
class RoleDetailPresenter(context: Any, arguments: Map<String, String>, view: RoleDetailView) :
        UstadBaseController<RoleDetailView>(context, arguments, view) {

    internal var repository: UmAppDatabase
    private var currentRoleUid: Long = 0
    private var currentRole: Role? = null
    private var updatedRole: Role? = null
    internal var roleDao: RoleDao

    var permissionField: Long = 0

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        roleDao = repository.roleDao

        if (arguments.containsKey(ROLE_UID)) {
            currentRoleUid = arguments.get(ROLE_UID)!!.toLong()
        }

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (currentRoleUid == 0L) {
            currentRole = Role()
            currentRole!!.roleName = ""
            currentRole!!.roleActive = false
            GlobalScope.launch {
                val result = roleDao.insertAsync(currentRole!!)
                initFromRole(result)
            }
        } else {
            initFromRole(currentRoleUid)
        }
    }

    private fun handleRoleChanged(changedRole: Role?) {

        //set the og person value
        if (currentRole == null)
            currentRole = changedRole!!

        if (updatedRole == null || updatedRole != changedRole) {
            //update class edit views
            view.updateRoleOnView(updatedRole!!)
            //Update the currently editing class object
            updatedRole = changedRole
        }
    }

    private fun initFromRole(roleUid: Long) {
        this.currentRoleUid = roleUid

        val roleUmLiveData = roleDao.findByUidLive(currentRoleUid)
        //Observe the live data
        view.runOnUiThread(Runnable {
            roleUmLiveData.observeWithPresenter(this, this::handleRoleChanged)
        })

        GlobalScope.launch {
            val result = roleDao.findByUidAsync(roleUid)
            updatedRole = result
            view.updateRoleOnView(updatedRole!!)
        }
    }

    fun updateRoleName(name: String) {
        updatedRole!!.roleName = name
    }


    fun handleClickDone() {

        updatedRole!!.roleActive = true
        updatedRole!!.rolePermissions = permissionField
        GlobalScope.launch {
            roleDao.updateAsync(updatedRole!!)
            view.finish()
        }

    }


}
