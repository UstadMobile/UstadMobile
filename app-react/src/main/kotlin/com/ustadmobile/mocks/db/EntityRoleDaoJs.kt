package com.ustadmobile.mocks.db

import androidx.paging.DataSource
import com.ustadmobile.core.db.dao.EntityRoleDao
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.EntityRole
import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole
import com.ustadmobile.mocks.db.DatabaseJs.Companion.ALLOW_ACCESS

class EntityRoleDaoJs: EntityRoleDao() {
    override suspend fun updateEntityRoleActive(uid: Long, active: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun userHasTableLevelPermission(
        accountPersonUid: Long,
        permission: Long
    ): Boolean {
        return ALLOW_ACCESS
    }

    override suspend fun userHasAnySinglePermission(
        accountPersonUid: Long,
        permission: Long
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun findByEntitiyAndPersonGroupAndRole(
        tableId: Int,
        entityUid: Long,
        groupUid: Long,
        roleUid: Long
    ): List<EntityRole> {
        TODO("Not yet implemented")
    }

    override fun filterByPersonWithExtra(personGroupUid: Long): DataSource.Factory<Int, EntityRoleWithNameAndRole> {
        return DataSourceFactoryJs(listOf())
    }

    override suspend fun filterByPersonWithExtraAsList(personGroupUid: Long): List<EntityRoleWithNameAndRole> {
        TODO("Not yet implemented")
    }

    override suspend fun findByUidAsync(uid: Long): EntityRole? {
        TODO("Not yet implemented")
    }

    override fun findByUidLive(uid: Long): DoorLiveData<EntityRole?> {
        TODO("Not yet implemented")
    }

    override suspend fun updateAsync(entity: EntityRole): Int {
        TODO("Not yet implemented")
    }

    override suspend fun insertOrReplace(entity: EntityRole) {
        TODO("Not yet implemented")
    }

    override fun insert(entity: EntityRole): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: EntityRole): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<EntityRole>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<EntityRole>) {
        TODO("Not yet implemented")
    }

    override fun update(entity: EntityRole) {
        TODO("Not yet implemented")
    }

    override suspend fun insertListAsync(entityList: List<EntityRole>) {
        TODO("Not yet implemented")
    }

    override suspend fun updateListAsync(entityList: List<EntityRole>) {
        TODO("Not yet implemented")
    }


}