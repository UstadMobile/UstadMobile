package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazz
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson

@UmDao(inheritPermissionFrom = ClazzDao::class, inheritPermissionForeignKey = "clazzMemberClazzUid",
        inheritPermissionJoinedPrimaryKey = "clazzUid")
@UmRepository
@Dao
abstract class ClazzMemberDao : BaseDao<ClazzMember> {

    @Insert
    abstract override fun insert(entity: ClazzMember): Long

    @Insert
    abstract fun insertListAsync(entityList: List<ClazzMember>)

    open suspend fun updateDateLeft(clazzMemberUidList: List<Long>, endDate: Long) {
        clazzMemberUidList.forEach {
            updateDateLeftByUid(it, endDate)
        }
    }

    @Query("SELECT * FROM ClazzMember WHERE clazzMemberPersonUid = :personUid " + "AND clazzMemberClazzUid = :clazzUid")
    abstract suspend fun findByPersonUidAndClazzUidAsync(personUid: Long, clazzUid: Long): ClazzMember?

    @Query("UPDATE ClazzMember SET clazzMemberDateLeft = :endDate WHERE clazzMemberUid = :clazzMemberUid")
    abstract suspend fun updateDateLeftByUid(clazzMemberUid: Long, endDate: Long)

    @Update
    abstract override fun update(entity: ClazzMember)

    @Update
    abstract suspend fun updateAsync(entity: ClazzMember):Int

    /**
     * Provide a list of the classes a given person is in with the class information itself (e.g.
     * for person detail).
     *
     * @param personUid
     * @param date If this is not 0, then the query will ensure that the registration is current at
     * the given
     */
    @Query("""SELECT ClazzMember.*, Clazz.* 
        FROM ClazzMember
        LEFT JOIN Clazz ON ClazzMember.clazzMemberClazzUid = Clazz.clazzUid
        WHERE ClazzMember.clazzMemberPersonUid = :personUid
        AND (:date = 0 OR :date BETWEEN ClazzMember.clazzMemberDateJoined AND ClazzMember.clazzMemberDateLeft)
    """)
    abstract fun findAllClazzesByPersonWithClazz(personUid: Long, date: Long): DataSource.Factory<Int, ClazzMemberWithClazz>

    @Query("""SELECT ClazzMember.*, Clazz.* 
        FROM ClazzMember
        LEFT JOIN Clazz ON ClazzMember.clazzMemberClazzUid = Clazz.clazzUid
        WHERE ClazzMember.clazzMemberPersonUid = :personUid
        AND (:date = 0 OR :date BETWEEN ClazzMember.clazzMemberDateJoined AND ClazzMember.clazzMemberDateLeft)
    """)
    abstract suspend fun findAllClazzesByPersonWithClazzAsListAsync(personUid: Long, date: Long): List<ClazzMemberWithClazz>

    @Query("""SELECT ClazzMember.*, Person.*
        FROM ClazzMember
        LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid
        WHERE ClazzMember.clazzMemberClazzUid = :clazzUid
        AND :date BETWEEN ClazzMember.clazzMemberDateJoined AND ClazzMember.clazzMemberDateLeft
        AND (:roleFilter = 0 OR ClazzMember.clazzMemberRole = :roleFilter)
    """)
    abstract suspend fun getAllClazzMembersAtTimeAsync(clazzUid: Long, date: Long, roleFilter: Int) : List<ClazzMemberWithPerson>

    @Query("SELECT * FROM ClazzMember WHERE clazzMemberUid = :uid")
    abstract fun findByUid(uid: Long): ClazzMember?

    @Query("""SELECT ClazzMember.*, Person.* FROM 
        ClazzMember
        LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid
        WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND ClazzMember.clazzMemberRole = :roleId
        AND CAST(clazzMemberActive AS INT) = 1
        ORDER BY Person.firstNames
    """)
    abstract fun findByClazzUidAndRole(clazzUid: Long, roleId: Int): DataSource.Factory<Int, ClazzMemberWithPerson>


    @Query("UPDATE ClazzMember SET clazzMemberActive = :enrolled WHERE " + "clazzMemberPersonUid = :personUid AND clazzMemberClazzUid = :clazzUid")
    abstract fun updateClazzMemberActiveForPersonAndClazz(personUid: Long, clazzUid: Long, enrolled: Int): Int

    fun updateClazzMemberActiveForPersonAndClazz(personUid: Long, clazzUid: Long, enrolled: Boolean): Int {
        return if (enrolled) {
            updateClazzMemberActiveForPersonAndClazz(personUid, clazzUid, 1)
        } else {
            updateClazzMemberActiveForPersonAndClazz(personUid, clazzUid, 0)
        }
    }

    @Query("UPDATE ClazzMember SET clazzMemberActive = :enrolled WHERE clazzMemberUid = :clazzMemberUid")
    abstract fun updateClazzMemberActiveForClazzMember(clazzMemberUid: Long, enrolled: Int): Int

    fun updateClazzMemberActiveForClazzMember(clazzMemberUid: Long, enrolled: Boolean): Int {
        return if (enrolled) {
            updateClazzMemberActiveForClazzMember(clazzMemberUid, 1)
        } else {
            updateClazzMemberActiveForClazzMember(clazzMemberUid, 0)
        }
    }

    companion object {

    }
}
