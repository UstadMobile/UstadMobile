package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonGroupDao
import com.ustadmobile.core.db.dao.PersonGroupMemberDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.GroupDetailView
import com.ustadmobile.core.view.GroupDetailView.Companion.GROUP_UID
import com.ustadmobile.lib.db.entities.PersonGroup
import com.ustadmobile.lib.db.entities.PersonWithEnrollment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * Presenter for GroupDetail view
 */
class GroupDetailPresenter(context: Any, arguments: Map<String, String>?, view: GroupDetailView,
                           val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<GroupDetailView>(context, arguments!!, view) {

    private var umProvider: DataSource.Factory<Int, PersonWithEnrollment>? = null
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
            currentGroupUid = arguments!!.get(GROUP_UID)!!.toLong()
        }

    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        if (currentGroupUid == 0L) {
            currentGroup = PersonGroup()
            currentGroup!!.groupName = ""
            currentGroup!!.groupActive = (false)
            GlobalScope.launch {
                val result = groupDao.insertAsync(currentGroup!!)
                initFromGroup(result)
            }
        } else {
            initFromGroup(currentGroupUid)
        }


    }

    fun initFromGroup(groupUid: Long) {
        this.currentGroupUid = groupUid

        val groupUmLiveData = groupDao.findByUidLive(currentGroupUid)
        view.runOnUiThread(Runnable {
            groupUmLiveData.observeWithPresenter(this, this::handleGroupChanged)
        })

        GlobalScope.launch {
            val result = groupDao.findByUidAsync(groupUid)
            updatedGroup = result
            view.updateGroupOnView(updatedGroup!!)

            //Get provider
            umProvider = providerDao.findAllPersonWithEnrollmentWithGroupUid(currentGroupUid)
            view.runOnUiThread(Runnable {
                view.setListProvider(umProvider!!)
            })
        }

    }

    fun handleGroupChanged(changedGroup: PersonGroup?) {
        if (changedGroup == null) {
            currentGroup = changedGroup
        }

        if (updatedGroup == null || updatedGroup == changedGroup) {
            updatedGroup = changedGroup
            view.runOnUiThread(Runnable {
                view.updateGroupOnView(updatedGroup!!)
            })
        }
    }

    fun updateGroupName(name: String) {
        updatedGroup!!.groupName = name
    }

    fun handleClickDone() {

        updatedGroup!!.groupActive = (true)
        GlobalScope.launch {
            groupDao.updateAsync(updatedGroup!!)
            view.finish()
        }


    }

    fun handleClickAddMember() {
        val args = HashMap<String, String>()
        args.put(GROUP_UID, currentGroupUid.toString())
        //impl.go(ClazzDetailEnrollStudentView.VIEW_NAME, args, context)

    }

    fun handleClickStudent(uid: Long) {
        //TODO:
    }

    fun handleDeleteMember(uid: Long) {
        GlobalScope.launch {
            providerDao.inactivateMemberFromGroupAsync(uid, currentGroupUid)
        }
    }
}
