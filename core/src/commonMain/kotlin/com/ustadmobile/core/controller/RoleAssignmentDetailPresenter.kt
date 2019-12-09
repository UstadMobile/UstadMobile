package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.RoleAssignmentDetailView
import com.ustadmobile.core.view.RoleAssignmentDetailView.Companion.ENTITYROLE_UID
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


/**
 * Presenter for RoleAssignmentDetail view
 */
class RoleAssignmentDetailPresenter(context: Any, arguments: Map<String, String>?, view:
RoleAssignmentDetailView) : UstadBaseController<RoleAssignmentDetailView>(context, arguments!!,
        view) {

    internal var repository: UmAppDatabase

    private var currentEntityRoleUid: Long = 0
    private var originalEntityRole: EntityRole? = null
    private var updatedEntityRole: EntityRole? = null

    private var groupUmLiveData: DoorLiveData<List<PersonGroup>>? = null
    private var roleUmLiveData: DoorLiveData<List<Role>>? = null
    private var locationUmLiveData: DoorLiveData<List<Location>>? = null
    private var clazzUmLiveData: DoorLiveData<List<Clazz>>? = null
    private var personUmLiveData: DoorLiveData<List<Person>>? = null

    private var groupIdToPosition: HashMap<Long, Int>? = null
    private val groupPositionToId: HashMap<Int, Long>
    private var roleIdToPosition: HashMap<Long, Int>? = null
    private val rolePositionToId: HashMap<Int, Long>
    private var locationIdToPosition: HashMap<Long, Int>? = null
    private val locationPositionToId: HashMap<Int, Long>
    private var clazzIdToPosition: HashMap<Long, Int>? = null
    private val clazzPositionToId: HashMap<Int, Long>
    private var peopleIdToPosition: HashMap<Long, Int>? = null
    private val peoplePositionToId: HashMap<Int, Long>

    private val entityRoleDao: EntityRoleDao

    private val groupDao: PersonGroupDao
    private val roleDao: RoleDao
    private val clazzDao: ClazzDao
    private val locationDao: LocationDao
    private val personDao: PersonDao

    private var currentTableId: Int = 0

    private var assigneePresets: Array<String>? = null
    private var groupPresets: Array<String>? = null
    private var rolePresets: Array<String>? = null
    private var scopePresets: Array<String?>? = null

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        entityRoleDao = repository.entityRoleDao
        groupDao = repository.personGroupDao
        roleDao = repository.roleDao
        clazzDao = repository.clazzDao
        locationDao = repository.locationDao
        personDao = repository.personDao
        groupIdToPosition = HashMap()
        groupPositionToId = HashMap()
        roleIdToPosition = HashMap()
        rolePositionToId = HashMap()
        locationIdToPosition = HashMap()
        locationPositionToId = HashMap()
        clazzIdToPosition = HashMap()
        clazzPositionToId = HashMap()
        peopleIdToPosition = HashMap()
        peoplePositionToId = HashMap()

        if (arguments!!.containsKey(ENTITYROLE_UID)) {
            currentEntityRoleUid = arguments!!.get(ENTITYROLE_UID)!!.toLong()
        }
    }

    fun updateGroupList(individual: Boolean) {

        if (individual) {
            //Update group
            groupUmLiveData = groupDao.findAllActivePersonPersonGroupLive()
            view.runOnUiThread(Runnable {
                groupUmLiveData!!.observe(this, this::handleAllGroupsChanged)
            })
        } else {
            //Update group
            groupUmLiveData = groupDao.findAllActiveGroupPersonGroupsLive()
            view.runOnUiThread(Runnable {
                groupUmLiveData!!.observe(this, this::handleAllGroupsChanged)
            })
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (currentEntityRoleUid == 0L) {
            val newEntityRole = EntityRole()
            newEntityRole.erActive = false
            GlobalScope.launch {
                val result = entityRoleDao.insertAsync(newEntityRole)
                initFromEntityRole(result!!)
            }

        } else {
            initFromEntityRole(currentEntityRoleUid)
        }

    }

    fun updateAssigneePresets(tableId: Int) {

        when (tableId) {
            Clazz.TABLE_ID -> {
                clazzUmLiveData = clazzDao.findAllLive()
                clazzUmLiveData!!.observe(this, this::handleAllClazzChanged)
            }
            Location.TABLE_ID -> {
                locationUmLiveData = locationDao.findAllActiveLocationsLive()
                locationUmLiveData!!.observe(this, this::handleAllLocationsChanged)
            }
            Person.TABLE_ID -> {
                personUmLiveData = personDao.findAllActiveLive()
                personUmLiveData!!.observe(this, this::handleAllPersonChanged)
            }
        }

    }

    private fun initFromEntityRole(uid: Long) {
        this.currentEntityRoleUid = uid

        val entityRoleLiveData = entityRoleDao.findByUidLive(currentEntityRoleUid)
        var thisP = this
        view.runOnUiThread(Runnable {
            entityRoleLiveData.observe(this, this::handleEntityRoleChanged)
        })

        GlobalScope.launch {
            val result = entityRoleDao.findByUidAsync(uid)
            updatedEntityRole = result

            //Update roles
            roleUmLiveData = roleDao.findAllActiveRolesLive()

            view.runOnUiThread(Runnable {
                roleUmLiveData!!.observe(thisP, thisP::handleAllRolesChanged)
            })

            val groupUid = updatedEntityRole!!.erGroupUid
            val result2 = groupDao.findByUidAsync(groupUid)
            if (result2 != null && result2.groupPersonUid != 0L) {
                view.individualClicked()
                updateGroupList(true)
            } else {
                view.groupClicked()
                updateGroupList(false)
            }

            val roleUid = updatedEntityRole!!.erRoleUid
            var groupSelected = 0
            var roleSelected = 0
            if (groupUid != 0L && roleUid != 0L && groupIdToPosition != null
                    && roleIdToPosition != null) {
                if (groupIdToPosition!!.containsKey(groupUid))
                    groupSelected = groupIdToPosition!![groupUid]!!
                if (roleIdToPosition!!.containsKey(roleUid))
                    roleSelected = roleIdToPosition!![roleUid]!!
            }
            //Update scope and assignee
            view.updateRoleAssignmentOnView(updatedEntityRole!!, groupSelected,
                    roleSelected)
        }
    }


    private fun handleAllGroupsChanged(groups: List<PersonGroup>?) {
        var selectedPosition = 0

        val entityList = ArrayList<String>()
        groupIdToPosition = HashMap()
        var posIter = 0
        for (everyEntity in groups!!) {
            entityList.add(everyEntity.groupName!!)
            groupIdToPosition!![everyEntity.groupUid] = posIter
            groupPositionToId[posIter] = everyEntity.groupUid
            posIter++
        }
        groupPresets = entityList.toTypedArray()

        if (originalEntityRole == null) {
            originalEntityRole = EntityRole()
        }
        if (originalEntityRole!!.erGroupUid != 0L) {
            val groupUid = originalEntityRole!!.erGroupUid
            if (groupIdToPosition!!.containsKey(groupUid)) {
                selectedPosition = groupIdToPosition!![groupUid]!!
            }
        }

        view.setGroupPresets(groupPresets!!, selectedPosition)
    }

    private fun handleAllRolesChanged(roles: List<Role>?) {
        var selectedPosition = 0

        val entityList = ArrayList<String>()
        roleIdToPosition = HashMap()
        var posIter = 0
        for (everyEntity in roles!!) {
            entityList.add(everyEntity.roleName!!)
            roleIdToPosition!![everyEntity.roleUid] = posIter
            rolePositionToId[posIter] = everyEntity.roleUid
            posIter++
        }
        //rolePresets = arrayOfNulls(entityList.size)
        rolePresets = entityList.toTypedArray()

        if (originalEntityRole == null) {
            originalEntityRole = EntityRole()
        }
        if (originalEntityRole!!.erRoleUid != 0L) {
            val entityUid = originalEntityRole!!.erRoleUid
            if (roleIdToPosition!!.containsKey(entityUid)) {
                selectedPosition = roleIdToPosition!![entityUid]!!
            }
        }

        view.setRolePresets(rolePresets!!, selectedPosition)
    }

    fun setScopePresets(scopePresets: Array<String?>) {
        this.scopePresets = scopePresets
    }

    private fun handleAllLocationsChanged(locations: List<Location>?) {
        var selectedPosition = 0

        val entityList = ArrayList<String>()
        locationIdToPosition = HashMap()
        var posIter = 0
        for (everyEntity in locations!!) {
            entityList.add(everyEntity.title!!)
            locationIdToPosition!![everyEntity.locationUid] = posIter
            locationPositionToId[posIter] = everyEntity.locationUid
            posIter++
        }
        //assigneePresets = arrayOfNulls(entityList.size)
        assigneePresets = entityList.toTypedArray()
        if (originalEntityRole == null) {
            originalEntityRole = EntityRole()
        }
        if (originalEntityRole!!.erEntityUid != 0L) {
            val entityUid = originalEntityRole!!.erEntityUid
            if (locationIdToPosition!!.containsKey(entityUid)) {
                selectedPosition = locationIdToPosition!![entityUid]!!
            }
        }

        view.setAssigneePresets(assigneePresets!!, selectedPosition)
    }

    private fun handleAllClazzChanged(clazzes: List<Clazz>?) {
        var selectedPosition = 0

        val entityList = ArrayList<String>()
        clazzIdToPosition = HashMap()
        var posIter = 0
        for (everyEntity in clazzes!!) {
            entityList.add(everyEntity.clazzName!!)
            clazzIdToPosition!![everyEntity.clazzUid] = posIter
            clazzPositionToId[posIter] = everyEntity.clazzUid
            posIter++
        }
        //assigneePresets = arrayOfNulls(entityList.size)
        assigneePresets = entityList.toTypedArray()

        if (originalEntityRole == null) {
            originalEntityRole = EntityRole()
        }
        if (originalEntityRole!!.erEntityUid != 0L) {
            val entityUid = originalEntityRole!!.erEntityUid
            if (clazzIdToPosition!!.containsKey(entityUid)) {
                selectedPosition = clazzIdToPosition!![entityUid]!!
            }
        }

        view.setAssigneePresets(assigneePresets!!, selectedPosition)
    }

    private fun handleAllPersonChanged(people: List<Person>?) {
        var selectedPosition = 0

        val entityList = ArrayList<String>()
        peopleIdToPosition = HashMap()
        var posIter = 0
        for (everyEntity in people!!) {
            entityList.add(everyEntity.firstNames + " " + everyEntity.lastName)
            peopleIdToPosition!![everyEntity.personUid] = posIter
            peoplePositionToId[posIter] = everyEntity.personUid
            posIter++
        }
        //assigneePresets = arrayOfNulls(entityList.size)
        assigneePresets = entityList.toTypedArray()

        if (originalEntityRole == null) {
            originalEntityRole = EntityRole()
        }
        if (originalEntityRole!!.erEntityUid != 0L) {
            val entityUid = originalEntityRole!!.erEntityUid
            if (peopleIdToPosition!!.containsKey(entityUid)) {
                selectedPosition = peopleIdToPosition!![entityUid]!!
            }
        }

        view.setAssigneePresets(assigneePresets!!, selectedPosition)
    }


    fun updateGroup(position: Int) {

        if (groupPositionToId.containsKey(position))
            updatedEntityRole!!.erGroupUid = groupPositionToId[position]!!
    }

    fun updateRole(position: Int) {
        if (rolePositionToId.containsKey(position))
            updatedEntityRole!!.erRoleUid = rolePositionToId[position]!!
    }

    fun updateScope(position: Int) {
        currentTableId = 0
        when (position) {
            0 -> {
                currentTableId = Clazz.TABLE_ID
                view.setScopeAndAssigneeSelected(currentTableId)
            }
            1 -> {
                currentTableId = Person.TABLE_ID
                view.setScopeAndAssigneeSelected(currentTableId)
            }
            2 -> {
                currentTableId = Location.TABLE_ID
                view.setScopeAndAssigneeSelected(currentTableId)
            }
        }
        updatedEntityRole!!.erTableId = currentTableId
    }

    fun updateAssignee(position: Int) {
        var entityUid: Long = 0
        when (currentTableId) {
            Clazz.TABLE_ID -> if (clazzPositionToId.containsKey(position))
                entityUid = clazzPositionToId[position]!!
            Person.TABLE_ID -> if (peoplePositionToId.containsKey(position))
                entityUid = peoplePositionToId[position]!!
            Location.TABLE_ID -> if (locationPositionToId.containsKey(position))
                entityUid = locationPositionToId[position]!!
        }
        if (entityUid != 0L) {
            updatedEntityRole!!.erEntityUid = entityUid
        }
    }

    private fun handleEntityRoleChanged(changedEntityRole: EntityRole?) {
        //set the og person value
        if (originalEntityRole == null)
            originalEntityRole = changedEntityRole

        if (updatedEntityRole == null || updatedEntityRole != changedEntityRole) {
            //update class edit views
            //Update the currently editing class object
            updatedEntityRole = changedEntityRole

            view.updateRoleAssignmentOnView(updatedEntityRole!!, 0, 0)
        }
    }

    fun handleClickDone() {
        updatedEntityRole!!.erActive = true
        GlobalScope.launch {
            entityRoleDao.updateAsync(updatedEntityRole!!)
            view.finish()
        }
    }
}
