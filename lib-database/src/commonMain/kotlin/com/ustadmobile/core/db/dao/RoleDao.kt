package com.ustadmobile.core.db.dao

import com.ustadmobile.door.DoorDataSourceFactory
import androidx.room.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.Role

@Repository
@Dao
abstract class RoleDao : BaseDao<Role> {

    @Query("SELECT * FROM Role WHERE roleName=:roleName")
    abstract suspend fun findByName(roleName: String): Role?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrReplace(entity: Role)

    @Query("SELECT * FROM Role WHERE roleName = :roleName")
    abstract fun findByNameSync(roleName: String): Role?

    @Query("SELECT * FROM Role WHERE CAST(roleActive AS INTEGER) = 1 ")
    abstract fun findAllActiveRoles(): DoorDataSourceFactory<Int, Role>

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
    abstract fun findAllActiveRolesSorted(sortOrder: Int, searchText: String): DoorDataSourceFactory<Int, Role>

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
        val teacherRole = findByUidAsync(Role.ROLE_CLAZZ_TEACHER_UID.toLong())
        if(teacherRole == null) {
            insertOrReplace(Role(Role.ROLE_CLAZZ_TEACHER_NAME, Role.ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT).apply {
                roleUid = Role.ROLE_CLAZZ_TEACHER_UID.toLong()
            })
        }

        val studentRole = findByUidAsync(Role.ROLE_CLAZZ_STUDENT_UID.toLong())
        if(studentRole == null) {
            insertOrReplace(Role(Role.ROLE_CLAZZ_STUDENT_NAME, Role.ROLE_CLAZZ_STUDENT_PERMISSIONS_DEFAULT).apply {
                roleUid = Role.ROLE_CLAZZ_STUDENT_UID.toLong()
            })
        }

        val studentPendingRole = findByUidAsync(Role.ROLE_CLAZZ_STUDENT_PENDING_UID.toLong())
        if(studentPendingRole == null) {
            insertOrReplace(Role(Role.ROLE_CLAZZ_STUDENT_PENDING_NAME, Role.ROLE_CLAZZ_STUDENT_PENDING_PERMISSION_DEFAULT).apply {
                roleUid = Role.ROLE_CLAZZ_STUDENT_PENDING_UID.toLong()
            })
        }

        val schoolTeacherRole = findByUidAsync(Role.ROLE_SCHOOL_STAFF_UID.toLong())
        if(schoolTeacherRole == null) {
            insertOrReplace(Role(Role.ROLE_SCHOOL_STAFF_NAME, Role.ROLE_SCHOOL_STAFF_PERMISSIONS_DEFAULT).apply {
                roleUid = Role.ROLE_SCHOOL_STAFF_UID.toLong()
            })
        }

        val schoolStudentRole = findByUidAsync(Role.ROLE_SCHOOL_STUDENT_UID.toLong())
        if(schoolStudentRole == null) {
            insertOrReplace(Role(Role.ROLE_SCHOOL_STUDENT_NAME, Role.ROLE_SCHOOL_STUDENT_PERMISSION_DEFAULT).apply {
                roleUid = Role.ROLE_SCHOOL_STUDENT_UID.toLong()
            })
        }

        //Added School Pending role:
        val schoolStudentPendingRole = findByUidAsync(Role.ROLE_SCHOOL_STUDENT_PENDING_UID.toLong())
        if(schoolStudentPendingRole == null) {
            insertOrReplace(Role(Role.ROLE_SCHOOL_STUDENT_PENDING_NAME, Role.ROLE_SCHOOL_STUDENT_PENDING_PERMISSION_DEFAULT).apply {
                roleUid = Role.ROLE_SCHOOL_STUDENT_PENDING_UID.toLong()
            })
        }

        //Add Principal role
        val principalRole = findByUidAsync(Role.ROLE_PRINCIPAL_UID.toLong())
        if(principalRole == null) {
            insertOrReplace(Role(Role.ROLE_PRINCIPAL_NAME, Role.ROLE_PRINCIPAL_PERMISSIONS_DEFAULT).apply {
                roleUid = Role.ROLE_PRINCIPAL_UID.toLong()
            })
        }
    }


    companion object {

        const val SORT_NAME_ASC = 1

        const val SORT_NAME_DESC = 2

        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"

    }
}
