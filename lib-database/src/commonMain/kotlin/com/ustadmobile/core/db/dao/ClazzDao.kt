package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.core.db.dao.ClazzDao.Companion.ENTITY_LEVEL_PERMISSION_CONDITION1
import com.ustadmobile.core.db.dao.ClazzDao.Companion.ENTITY_LEVEL_PERMISSION_CONDITION2
import com.ustadmobile.core.db.dao.ClazzDao.Companion.TABLE_LEVEL_PERMISSION_CONDITION1
import com.ustadmobile.core.db.dao.ClazzDao.Companion.TABLE_LEVEL_PERMISSION_CONDITION2
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import com.ustadmobile.lib.db.entities.Location
import com.ustadmobile.lib.db.entities.Role

@UmDao(selectPermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_SELECT +
        ENTITY_LEVEL_PERMISSION_CONDITION2, updatePermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_UPDATE +
        ENTITY_LEVEL_PERMISSION_CONDITION2, insertPermissionCondition = TABLE_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_INSERT +
        TABLE_LEVEL_PERMISSION_CONDITION2)
@Dao
@UmRepository
abstract class ClazzDao : BaseDao<Clazz> {

    @Insert
    abstract override fun insert(entity: Clazz): Long

    @Insert
    abstract override suspend fun insertAsync(entity: Clazz): Long

    @Query("SELECT * FROM Clazz WHERE clazzUid = :uid")
    abstract fun findByUid(uid: Long): Clazz?

    @Query("SELECT Clazz.*, " +
            " (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid) AS numStudents" +
            " FROM Clazz WHERE :personUid in " +
            " (SELECT ClazzMember.clazzMemberPersonUid FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid)")
    abstract fun findAllClazzesByPersonUid(personUid: Long): DataSource.Factory<Int, ClazzWithNumStudents>


    /** Check if a permission is present on a specific entity e.g. updateStateAsync/modify etc */
    @Query("SELECT 1 FROM Clazz WHERE Clazz.clazzUid = :clazzUid AND (" + ENTITY_LEVEL_PERMISSION_CONDITION1 +
            " :permission" + ENTITY_LEVEL_PERMISSION_CONDITION2 + ")")
    abstract suspend fun personHasPermissionAsync(accountPersonUid: Long, clazzUid: Long, permission: Long): Boolean

    @Query("SELECT " + TABLE_LEVEL_PERMISSION_CONDITION1 + " :permission "
            + TABLE_LEVEL_PERMISSION_CONDITION2 + " AS hasPermission")
    abstract suspend fun personHasPermission(accountPersonUid: Long, permission: Long): Boolean

//    @Query("SELECT Clazz.clazzUid as primaryKey, " +
//            "(" + ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_UPDATE + ENTITY_LEVEL_PERMISSION_CONDITION2 + ") " +
//            " AS userCanUpdate " +
//            " FROM Clazz WHERE Clazz.clazzUid in (:primaryKeys)")
//    @UmSyncCheckIncomingCanUpdate
//    abstract override fun syncFindExistingEntities(primaryKeys: List<Long>,
//                                                   accountPersonUid: Long): List<UmSyncExistingEntity>

    @Query("SELECT COUNT(*) FROM Clazz " +
            "WHERE " +
            "clazzLocalChangeSeqNum > (SELECT syncedToLocalChangeSeqNum FROM SyncStatus WHERE tableId = 6) " +
            "AND clazzLastChangedBy = (SELECT deviceBits FROM SyncDeviceBits LIMIT 1) " +
            "AND ((" + ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_UPDATE + //can updateStateAsync it

            ENTITY_LEVEL_PERMISSION_CONDITION2 + ") " +
            " OR (" + TABLE_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_INSERT + //can insert on table

            TABLE_LEVEL_PERMISSION_CONDITION2 + "))")
    abstract fun countPendingLocalChanges(accountPersonUid: Long): Int

    companion object {

        const val ENTITY_LEVEL_PERMISSION_CONDITION1 = " (SELECT admin FROM Person WHERE personUid = :accountPersonUid) = 1 OR " +
                "EXISTS(SELECT PersonGroupMember.groupMemberPersonUid FROM PersonGroupMember " +
                "JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid " +
                "JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
                "WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
                " AND (" +
                "(EntityRole.ertableId = " + Clazz.TABLE_ID +
                " AND EntityRole.erEntityUid = Clazz.clazzUid) " +
                "OR" +
                "(EntityRole.ertableId = " + Location.TABLE_ID +
                " AND EntityRole.erEntityUid IN (SELECT locationAncestorAncestorLocationUid FROM LocationAncestorJoin WHERE locationAncestorChildLocationUid = Clazz.clazzLocationUid))" +
                ") AND (Role.rolePermissions & "

        const val ENTITY_LEVEL_PERMISSION_CONDITION2 = ") > 0)"

        const val TABLE_LEVEL_PERMISSION_CONDITION1 = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid) " +
                "OR " +
                "EXISTS(SELECT PersonGroupMember.groupMemberPersonUid FROM PersonGroupMember " +
                " JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid " +
                " JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
                " WHERE " +
                " PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
                " AND EntityRole.erTableId = " + Clazz.TABLE_ID +
                " AND Role.rolePermissions & "

        const val TABLE_LEVEL_PERMISSION_CONDITION2 = " > 0)"
    }

}
