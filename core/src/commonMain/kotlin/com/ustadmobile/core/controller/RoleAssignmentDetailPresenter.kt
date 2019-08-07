package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.db.dao.EntityRoleDao
import com.ustadmobile.core.db.dao.LocationDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.PersonGroupDao
import com.ustadmobile.core.db.dao.RoleDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.view.RoleAssignmentDetailView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.EntityRole
import com.ustadmobile.lib.db.entities.Location
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonGroup
import com.ustadmobile.lib.db.entities.Role

import com.ustadmobile.core.view.RoleAssignmentDetailView.Companion.ENTITYROLE_UID


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

    private var groupUmLiveData: UmLiveData<List<PersonGroup>>? = null
    private var roleUmLiveData: UmLiveData<List<Role>>? = null
    private var locationUmLiveData: UmLiveData<List<Location>>? = null
    private var clazzUmLiveData: UmLiveData<List<Clazz>>? = null
    private var personUmLiveData: UmLiveData<List<Person>>? = null

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
    private var scopePresets: Array<String>? = null

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

        if (arguments.containsKey(ENTITYROLE_UID)) {
            currentEntityRoleUid = arguments!!.get(ENTITYROLE_UID)
        }
    }

    fun updateGroupList(individual: Boolean) {

        if (individual) {
            //Update group
            groupUmLiveData = groupDao.findAllActivePersonPersonGroupLive()
            groupUmLiveData!!.observe(this@RoleAssignmentDetailPresenter,
                    UmObserver<List<PersonGroup>> { this@RoleAssignmentDetailPresenter.handleAllGroupsChanged(it) })
        } else {
            //Update group
            groupUmLiveData = groupDao.findAllActiveGroupPersonGroupsLive()
            groupUmLiveData!!.observe(this@RoleAssignmentDetailPresenter,
                    UmObserver<List<PersonGroup>> { this@RoleAssignmentDetailPresenter.handleAllGroupsChanged(it) })
        }
    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        if (currentEntityRoleUid == 0L) {
            val newEntityRole = EntityRole()
            newEntityRole.erActive = false
            entityRoleDao.insertAsync(newEntityRole, object : UmCallback<Long> {
                override fun onSuccess(result: Long?) {
                    initFromEntityRole(result!!)
                }

                override fun onFailure(exception: Throwable?) {
                    print(exception!!.message)
                }
            })
        } else {
            initFromEntityRole(currentEntityRoleUid)
        }

    }

    fun updateAssigneePresets(tableId: Int) {

        when (tableId) {
            Clazz.TABLE_ID -> {
                clazzUmLiveData = clazzDao.findAllLive()
                clazzUmLiveData!!.observe(this@RoleAssignmentDetailPresenter,
                        UmObserver<List<Clazz>> { this@RoleAssignmentDetailPresenter.handleAllClazzChanged(it) })
            }
            Location.TABLE_ID -> {
                locationUmLiveData = locationDao.findAllActiveLocationsLive()
                locationUmLiveData!!.observe(this@RoleAssignmentDetailPresenter,
                        UmObserver<List<Location>> { this@RoleAssignmentDetailPresenter.handleAllLocationsChanged(it) })
            }
            Person.TABLE_ID -> {
                personUmLiveData = personDao.findAllActiveLive()
                personUmLiveData!!.observe(this@RoleAssignmentDetailPresenter,
                        UmObserver<List<Person>> { this@RoleAssignmentDetailPresenter.handleAllPersonChanged(it) })
            }
        }

    }

    private fun initFromEntityRole(uid: Long) {
        this.currentEntityRoleUid = uid

        val entityRoleLiveData = entityRoleDao.findByUidLive(currentEntityRoleUid)
        entityRoleLiveData.observe(this@RoleAssignmentDetailPresenter,
                UmObserver<EntityRole> { this@RoleAssignmentDetailPresenter.handleEntityRoleChanged(it) })

        entityRoleDao.findByUidAsync(uid, object : UmCallback<EntityRole> {
            override fun onSuccess(result: EntityRole?) {
                updatedEntityRole = result

                //Update roles
                roleUmLiveData = roleDao.findAllActiveRolesLive()
                roleUmLiveData!!.observe(this@RoleAssignmentDetailPresenter,
                        UmObserver<List<Role>> { this@RoleAssignmentDetailPresenter.handleAllRolesChanged(it) })

                val groupUid = updatedEntityRole!!.erGroupUid
                groupDao.findByUidAsync(groupUid, object : UmCallback<PersonGroup> {
                    override fun onSuccess(result: PersonGroup?) {
                        if (result != null && result.groupPersonUid != 0L) {
                            view.individualClicked()
                            updateGroupList(true)
                        } else {
                            view.groupClicked()
                            updateGroupList(false)
                        }
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
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

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }


    private fun handleAllGroupsChanged(groups: List<PersonGroup>) {
        var selectedPosition = 0

        val entityList = ArrayList<String>()
        groupIdToPosition = HashMap()
        var posIter = 0
        for (everyEntity in groups) {
            entityList.add(everyEntity.groupName)
            groupIdToPosition!![everyEntity.groupUid] = posIter
            groupPositionToId[posIter] = everyEntity.groupUid
            posIter++
        }
        groupPresets = arrayOfNulls(entityList.size)
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

    private fun handleAllRolesChanged(roles: List<Role>) {
        var selectedPosition = 0

        val entityList = ArrayList<String>()
        roleIdToPosition = HashMap()
        var posIter = 0
        for (everyEntity in roles) {
            entityList.add(everyEntity.roleName)
            roleIdToPosition!![everyEntity.roleUid] = posIter
            rolePositionToId[posIter] = everyEntity.roleUid
            posIter++
        }
        rolePresets = arrayOfNulls(entityList.size)
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

    fun setScopePresets(scopePresets: Array<String>) {
        this.scopePresets = scopePresets
    }

    private fun handleAllLocationsChanged(locations: List<Location>) {
        var selectedPosition = 0

        val entityList = ArrayList<String>()
        locationIdToPosition = HashMap()
        var posIter = 0
        for (everyEntity in locations) {
            entityList.add(everyEntity.title)
            locationIdToPosition!![everyEntity.locationUid] = posIter
            locationPositionToId[posIter] = everyEntity.locationUid
            posIter++
        }
        assigneePresets = arrayOfNulls(entityList.size)
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

    private fun handleAllClazzChanged(clazzes: List<Clazz>) {
        var selectedPosition = 0

        val entityList = ArrayList<String>()
        clazzIdToPosition = HashMap()
        var posIter = 0
        for (everyEntity in clazzes) {
            entityList.add(everyEntity.clazzName)
            clazzIdToPosition!![everyEntity.clazzUid] = posIter
            clazzPositionToId[posIter] = everyEntity.clazzUid
            posIter++
        }
        assigneePresets = arrayOfNulls(entityList.size)
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

    private fun handleAllPersonChanged(people: List<Person>) {
        var selectedPosition = 0

        val entityList = ArrayList<String>()
        peopleIdToPosition = HashMap()
        var posIter = 0
        for (everyEntity in people) {
            entityList.add(everyEntity.firstNames + " " + everyEntity.lastName)
            peopleIdToPosition!![everyEntity.personUid] = posIter
            peoplePositionToId[posIter] = everyEntity.personUid
            posIter++
        }
        assigneePresets = arrayOfNulls(entityList.size)
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
            updatedEntityRole!!.erGroupUid = groupPositionToId[position]
    }

    fun updateRole(position: Int) {
        if (rolePositionToId.containsKey(position))
            updatedEntityRole!!.erRoleUid = rolePositionToId[position]
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

    private fun handleEntityRoleChanged(changedEntityRole: EntityRole) {
        //set the og person value
        if (originalEntityRole == null)
            originalEntityRole = changedEntityRole

        if (updatedEntityRole == null || updatedEntityRole != changedEntityRole) {
            //update class edit views

            view.updateRoleAssignmentOnView(updatedEntityRole!!, 0, 0)
            //Update the currently editing class object
            updatedEntityRole = changedEntityRole
        }
    }

    fun handleClickDone() {
        updatedEntityRole!!.erActive = true
        entityRoleDao.updateAsync(updatedEntityRole!!, object : UmCallback<Int> {
            override fun onSuccess(result: Int?) {
                view.finish()
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })

    }
}
