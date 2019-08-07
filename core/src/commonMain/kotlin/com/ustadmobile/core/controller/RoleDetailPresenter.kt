package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.dao.RoleDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl



import com.ustadmobile.core.view.RoleDetailView
import com.ustadmobile.core.view.RoleListView
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UMCalendar

import com.ustadmobile.core.view.RoleListView.Companion.ROLE_UID


/**
 * Presenter for RoleDetail view
 */
class RoleDetailPresenter(context: Any, arguments: Map<String, String>?, view: RoleDetailView) :
        UstadBaseController<RoleDetailView>(context, arguments!!, view) {

    internal var repository: UmAppDatabase
    private var currentRoleUid: Long = 0
    private var currentRole: Role? = null
    private var updatedRole: Role? = null
    internal var roleDao: RoleDao

    var permissionField: Long = 0

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        roleDao = repository.roleDao

        if (arguments!!.containsKey(ROLE_UID)) {
            currentRoleUid = arguments!!.get(ROLE_UID)
        }

    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        if (currentRoleUid == 0L) {
            currentRole = Role()
            currentRole!!.roleName = ""
            currentRole!!.isRoleActive = false
            roleDao.insertAsync(currentRole, object : UmCallback<Long> {
                override fun onSuccess(result: Long?) {
                    initFromRole(result!!)
                }

                override fun onFailure(exception: Throwable?) {
                    print(exception!!.message)
                }
            })
        } else {
            initFromRole(currentRoleUid)
        }
    }

    fun handleRoleChanged(changedRole: Role) {

        //set the og person value
        if (currentRole == null)
            currentRole = changedRole

        if (updatedRole == null || updatedRole != changedRole) {
            //update class edit views
            view.updateRoleOnView(updatedRole!!)
            //Update the currently editing class object
            updatedRole = changedRole
        }
    }

    fun initFromRole(roleUid: Long) {
        this.currentRoleUid = roleUid

        val roleUmLiveData = roleDao.findByUidLive(currentRoleUid)
        //Observe the live data
        roleUmLiveData.observe(this@RoleDetailPresenter,
                UmObserver<Role> { this@RoleDetailPresenter.handleRoleChanged(it) })


        roleDao.findByUidAsync(roleUid, object : UmCallback<Role> {
            override fun onSuccess(result: Role?) {
                updatedRole = result
                view.updateRoleOnView(updatedRole!!)
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }

    fun updateRoleName(name: String) {
        updatedRole!!.roleName = name
    }


    fun handleClickDone() {

        updatedRole!!.isRoleActive = true
        updatedRole!!.rolePermissions = permissionField

        roleDao.updateAsync(updatedRole!!, object : UmCallback<Int> {
            override fun onSuccess(result: Int?) {
                view.finish()
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })

    }


}
