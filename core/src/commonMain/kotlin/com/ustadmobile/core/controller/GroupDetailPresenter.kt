package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.db.dao.PersonGroupDao
import com.ustadmobile.core.db.dao.PersonGroupMemberDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView
import com.ustadmobile.core.view.GroupDetailView
import com.ustadmobile.lib.db.entities.PersonGroup
import com.ustadmobile.lib.db.entities.PersonWithEnrollment



import com.ustadmobile.core.view.GroupDetailView.Companion.GROUP_UID

/**
 * Presenter for GroupDetail view
 */
class GroupDetailPresenter(context: Any, arguments: Map<String, String>?, view: GroupDetailView,
                           val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<GroupDetailView>(context, arguments!!, view) {

    private var umProvider: UmProvider<PersonWithEnrollment>? = null
    internal var repository: UmAppDatabase
    private val providerDao: PersonGroupMemberDao
    private var currentGroupUid: Long = 0
    private var currentGroup: PersonGroup? = null
    private var updatedGroup: PersonGroup? = null
    internal var groupDao: PersonGroupDao


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        providerDao = repository.personGroupMemberDao

        groupDao = repository.personGroupDao
        //Get or create group Uid

        if (arguments!!.containsKey(GROUP_UID)) {
            currentGroupUid = arguments!!.get(GROUP_UID)
        }


    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        if (currentGroupUid == 0L) {
            currentGroup = PersonGroup()
            currentGroup!!.groupName = ""
            currentGroup!!.setGroupActive(false)
            groupDao.insertAsync(currentGroup, object : UmCallback<Long> {
                override fun onSuccess(result: Long?) {
                    initFromGroup(result!!)
                }

                override fun onFailure(exception: Throwable?) {

                }
            })
        } else {
            initFromGroup(currentGroupUid)
        }


    }

    fun initFromGroup(groupUid: Long) {
        this.currentGroupUid = groupUid

        val groupUmLiveData = groupDao.findByUidLive(currentGroupUid)
        groupUmLiveData.observe(this@GroupDetailPresenter,
                UmObserver<PersonGroup> { this@GroupDetailPresenter.handleGroupChanged(it) })

        groupDao.findByUidAsync(groupUid, object : UmCallback<PersonGroup> {
            override fun onSuccess(result: PersonGroup?) {
                updatedGroup = result
                view.updateGroupOnView(updatedGroup!!)

                //Get provider
                umProvider = providerDao.findAllPersonWithEnrollmentWithGroupUid(currentGroupUid)
                view.setListProvider(umProvider!!)
            }

            override fun onFailure(exception: Throwable?) {

            }
        })

    }

    fun handleGroupChanged(changedGroup: PersonGroup?) {
        if (changedGroup == null) {
            currentGroup = changedGroup
        }

        if (updatedGroup == null || updatedGroup == changedGroup) {
            view.updateGroupOnView(updatedGroup!!)
            updatedGroup = changedGroup
        }
    }

    fun updateGroupName(name: String) {
        updatedGroup!!.groupName = name
    }

    fun handleClickDone() {

        updatedGroup!!.setGroupActive(true)
        groupDao.updateAsync(updatedGroup!!, object : UmCallback<Int> {
            override fun onSuccess(result: Int?) {
                view.finish()
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })

    }

    fun handleClickAddMember() {
        val args = HashMap<String, String>()
        args.put(GROUP_UID, currentGroupUid)
        impl.go(ClazzDetailEnrollStudentView.VIEW_NAME, args, context)

    }

    fun handleClickStudent(uid: Long) {
        //TODO:
    }

    fun handleDeleteMember(uid: Long) {
        providerDao.inactivateMemberFromGroupAsync(uid, currentGroupUid, null!!)
    }
}
