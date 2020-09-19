package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.ustadmobile.core.db.dao.RoleDao.Companion.SELECT_ACCOUNT_IS_ADMIN
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Role

@UmDao(updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class RoleDao : BaseDao<Role> {

    @Query("SELECT * FROM Role WHERE roleName=:roleName")
    abstract suspend fun findByName(roleName: String): Role?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrReplace(entity: Role)

    @Query("SELECT * FROM Role WHERE roleName = :roleName")
    abstract fun findByNameSync(roleName: String): Role?

    @Query("SELECT * FROM Role WHERE CAST(roleActive AS INTEGER) = 1 ")
    abstract fun findAllActiveRoles(): DataSource.Factory<Int, Role>

    @Query("""SELECT * FROM Role 
        WHERE CAST(roleActive AS INTEGER) = 1
         AND Role.roleName LIKE :searchText
        ORDER BY CASE(:sortOrder)
                WHEN ${SORT_NAME_ASC} THEN Role.roleName
                ELSE ''
            END ASC,
            CASE(:sortOrder)
                WHEN ${SORT_NAME_DESC} THEN Role.roleName
                ELSE ''
            END DESC
    """)
    abstract fun findAllActiveRolesSorted(sortOrder: Int, searchText: String): DataSource.Factory<Int, Role>

    @Query("SELECT * FROM Role WHERE CAST(roleActive AS INTEGER) = 1 ")
    abstract fun findAllActiveRolesLive(): DoorLiveData<List<Role>>

    @Query("UPDATE Role SET roleActive = 0 WHERE roleUid = :uid")
    abstract fun inactiveRole(uid: Long)

    @Query("UPDATE Role SET roleActive = 0 WHERE roleUid = :uid")
    abstract suspend fun inactiveRoleAsync(uid: Long) :Int

    @Query("SELECT * FROM Role WHERE roleUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): Role?

    @Query("SELECT * FROM Role WHERE roleUid = :uid")
    abstract suspend fun findByUidListAsync(uid: Long): List<Role>

    @Query("SELECT * FROM Role WHERE rolePermissions = :permission AND roleName = :name")
    abstract suspend fun findByPermissionAndNameAsync(permission: Long, name: String): List<Role>

    @Query("SELECT * FROM Role WHERE roleUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<Role?>

    @Update
    abstract suspend fun updateAsync(entitiy: Role):Int


    suspend fun insertDefaultRolesIfRequired() {
        val teacherRole = findByUidAsync(Role.ROLE_TEACHER_UID.toLong())
        if(teacherRole == null) {
            insertOrReplace(Role(Role.ROLE_TEACHER_NAME, Role.ROLE_TEACHER_PERMISSIONS_DEFAULT).apply {
                roleUid = Role.ROLE_TEACHER_UID.toLong()
            })
        }

        val studentRole = findByUidAsync(Role.ROLE_STUDENT_UID.toLong())
        if(studentRole == null) {
            insertOrReplace(Role(Role.ROLE_STUDENT_NAME, Role.ROLE_STUDENT_PERMISSIONS_DEFAULT).apply {
                roleUid = Role.ROLE_STUDENT_UID.toLong()
            })
        }

        val studentPendingRole = findByUidAsync(Role.ROLE_STUDENT_PENDING_UID.toLong())
        if(studentPendingRole == null) {
            insertOrReplace(Role(Role.ROLE_STUDENT_PENDING_NAME, Role.ROLE_STUDENT_PENDING_PERMISSION_DEFAULT).apply {
                roleUid = Role.ROLE_STUDENT_PENDING_UID.toLong()
            })
        }
    }


    companion object {

        const val SORT_NAME_ASC = 1

        const val SORT_NAME_DESC = 2

        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"

    }
}
