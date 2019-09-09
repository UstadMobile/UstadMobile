package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.AuditLog
import com.ustadmobile.lib.db.entities.AuditLogWithNames

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class AuditLogDao : BaseDao<AuditLog> {

    @Insert
    abstract override fun insert(entity: AuditLog): Long

    @Update
    abstract override fun update(entity: AuditLog)

    @Update
    abstract suspend fun updateAsync(entity: AuditLog):Int

    @Query("SELECT * FROM AuditLog WHERE auditLogUid = :uid")
    abstract fun findByUid(uid: Long): AuditLog?

    @Query("SELECT * FROM AuditLog WHERE auditLogUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): AuditLog?

    @Query("SELECT * FROM AuditLog")
    abstract fun findAllAuditLogs(): DataSource.Factory<Int, AuditLog>

    @Query(FIND_ALL_NAME)
    abstract fun findAllAuditLogsWithName(): DataSource.Factory<Int, AuditLogWithNames>

    @Query(FIND_ALL_NAME_CLAZZ)
    abstract fun findAllAuditLogsWithNameFilterByClazz(fromTime: Long, toTime: Long,
           clazzes: List<Long>): DataSource.Factory<Int, AuditLogWithNames>

    @Query(FIND_ALL_NAME_ACTOR)
    abstract fun findAllAuditLogsWithNameFilterByActors(fromTime: Long, toTime: Long, actors:
            List<Long>): DataSource.Factory<Int, AuditLogWithNames>

    @Query(FIND_ALL_NAME_PEOPLE)
    abstract fun findAllAuditLogsWithNameFilterByPeople(
            fromTime: Long, toTime: Long, people: List<Long>): DataSource.Factory<Int, AuditLogWithNames>

    @Query(FIND_ALL_NAME_ACTOR_CLAZZ)
    abstract fun findAllAuditLogsWithNameFilterByActorsAndClazzes(fromTime: Long, toTime: Long,
                          actors: List<Long>, clazzes: List<Long>): DataSource.Factory<Int, AuditLogWithNames>

    @Query(FIND_ALL_NAME_ACTOR_PEOPLE)
    abstract fun findAllAuditLogsWithNameFilterByActorsAndPeople( fromTime: Long, toTime: Long,
                                                          actors: List<Long>,people: List<Long>)
            : DataSource.Factory<Int, AuditLogWithNames>

    @Query(FIND_ALL_NAME_PEOPLE_CLAZZ)
    abstract fun findAllAuditLogsWithNameFilterByPeopleAndClazzes(fromTime: Long, toTime: Long,
                                                          people: List<Long>, clazzes: List<Long>)
            : DataSource.Factory<Int, AuditLogWithNames>

    @Query(FIND_ALL_NAME_ACTOR_CLAZZ_PEOPLE)
    abstract fun findAllAuditLogsWithNameFilterByActorsAndClazzesAndPeople(fromTime: Long,
                        toTime: Long, actors: List<Long>, clazzes: List<Long>, people: List<Long>)
            : DataSource.Factory<Int, AuditLogWithNames>

    /* AS LIST */
    @Query(FIND_ALL_NAME)
    abstract suspend fun findAllAuditLogsWithNameAsync():List<AuditLogWithNames>

    @Query(FIND_ALL_NAME_CLAZZ)
    abstract suspend fun findAllAuditLogsWithNameFilterByClazzAsync(fromTime: Long, toTime: Long,
                            clazzes: List<Long>): List<AuditLogWithNames>

    @Query(FIND_ALL_NAME_ACTOR)
    abstract suspend fun findAllAuditLogsWithNameFilterByActorsAsync(fromTime: Long, toTime: Long,
                             actors: List<Long>):List<AuditLogWithNames>

    @Query(FIND_ALL_NAME_PEOPLE)
    abstract suspend fun findAllAuditLogsWithNameFilterByPeopleAsync(
            fromTime: Long, toTime: Long, people: List<Long>):List<AuditLogWithNames>

    @Query(FIND_ALL_NAME_ACTOR_CLAZZ)
    abstract suspend fun findAllAuditLogsWithNameFilterByActorsAndClazzesAsync(fromTime: Long,
        toTime: Long,actors: List<Long>, clazzes: List<Long>): List<AuditLogWithNames>

    @Query(FIND_ALL_NAME_ACTOR_PEOPLE)
    abstract suspend fun findAllAuditLogsWithNameFilterByActorsAndPeopleAsync(fromTime: Long,
                                                                              toTime: Long,
          actors: List<Long>, people: List<Long>): List<AuditLogWithNames>

    @Query(FIND_ALL_NAME_PEOPLE_CLAZZ)
    abstract suspend fun findAllAuditLogsWithNameFilterByPeopleAndClazzesAsync(fromTime: Long,
                                                                               toTime: Long,
        people: List<Long>, clazzes: List<Long>): List<AuditLogWithNames>

    @Query(FIND_ALL_NAME_ACTOR_CLAZZ_PEOPLE)
    abstract suspend fun findAllAuditLogsWithNameFilterByActorsAndClazzesAndPeopleAsync(
            fromTime: Long,toTime: Long, actors: List<Long>,
            clazzes: List<Long>, people: List<Long>) : List<AuditLogWithNames>


    fun findAllAuditLogsWithNameFilter(fromTime: Long, toTime: Long,
                                       locations: List<Long>, clazzes: List<Long>,
                                       people: List<Long>, actors: List<Long>)
            : DataSource.Factory<Int, AuditLogWithNames> {

        return if (clazzes.isEmpty() && people.isEmpty() && actors.isEmpty()) {
            findAllAuditLogsWithName()
        } else if (!clazzes.isEmpty() && !people.isEmpty() && !actors.isEmpty()) {
            findAllAuditLogsWithNameFilterByActorsAndClazzesAndPeople(fromTime, toTime,
                    actors, clazzes, people)
        } else if (!clazzes.isEmpty() && !people.isEmpty() && actors.isEmpty()) {
            findAllAuditLogsWithNameFilterByPeopleAndClazzes(fromTime, toTime, people, clazzes)
        } else if (clazzes.isEmpty() && !people.isEmpty() && !actors.isEmpty()) {
            findAllAuditLogsWithNameFilterByActorsAndPeople(fromTime, toTime, actors, people)
        } else if (!clazzes.isEmpty() && people.isEmpty() && !actors.isEmpty()) {
            findAllAuditLogsWithNameFilterByActorsAndClazzes(fromTime, toTime, actors, clazzes)
        } else if (clazzes.isEmpty() && people.isEmpty() && !actors.isEmpty()) {
            findAllAuditLogsWithNameFilterByActors(fromTime, toTime, actors)
        } else if (clazzes.isEmpty() && !people.isEmpty() && actors.isEmpty()) {
            findAllAuditLogsWithNameFilterByPeople(fromTime, toTime, people)
        } else if (!clazzes.isEmpty() && people.isEmpty() && actors.isEmpty()) {
            findAllAuditLogsWithNameFilterByClazz(fromTime, toTime, clazzes)
        } else {
            findAllAuditLogsWithName()
        }
    }


    suspend fun findAllAuditLogsWithNameFilterList(fromTime: Long, toTime: Long,
                           locations: List<Long>, clazzes: List<Long>, people: List<Long>,
                           actors: List<Long>): List<AuditLogWithNames> {

        if (clazzes.isEmpty() && people.isEmpty() && actors.isEmpty()) {
            return findAllAuditLogsWithNameAsync()
        } else if (!clazzes.isEmpty() && !people.isEmpty() && !actors.isEmpty()) {
            return findAllAuditLogsWithNameFilterByActorsAndClazzesAndPeopleAsync(fromTime, toTime,
                    actors, clazzes, people)
        } else if (!clazzes.isEmpty() && !people.isEmpty() && actors.isEmpty()) {
            return findAllAuditLogsWithNameFilterByPeopleAndClazzesAsync(fromTime, toTime, people,
                    clazzes)
        } else if (clazzes.isEmpty() && !people.isEmpty() && !actors.isEmpty()) {
            return findAllAuditLogsWithNameFilterByActorsAndPeopleAsync(fromTime, toTime, actors,
                    people)
        } else if (!clazzes.isEmpty() && people.isEmpty() && !actors.isEmpty()) {
            return findAllAuditLogsWithNameFilterByActorsAndClazzesAsync(fromTime, toTime, actors,
                    clazzes)
        } else if (clazzes.isEmpty() && people.isEmpty() && !actors.isEmpty()) {
            return findAllAuditLogsWithNameFilterByActorsAsync(fromTime, toTime, actors)
        } else if (clazzes.isEmpty() && !people.isEmpty() && actors.isEmpty()) {
            return findAllAuditLogsWithNameFilterByPeopleAsync(fromTime, toTime, people)
        } else if (!clazzes.isEmpty() && people.isEmpty() && actors.isEmpty()) {
            return findAllAuditLogsWithNameFilterByClazzAsync(fromTime, toTime, clazzes)
        } else {
            return findAllAuditLogsWithNameAsync()
        }

    }

    companion object {


        private const val FIND_ALL_NAME = "SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
                "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
                "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
                "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
                "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid"

        private const val FIND_ALL_NAME_CLAZZ = "SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
                "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
                "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
                "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
                "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid " +
                "WHERE auditLogDate > :fromTime " +
                " AND auditLogDate < :toTime " +
                " AND Clazz.clazzUid IN (:clazzes) "

        private const val FIND_ALL_NAME_ACTOR = "SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
                "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
                "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
                "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
                "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid " +
                "WHERE auditLogDate > :fromTime " +
                " AND auditLogDate < :toTime AND Actor.personUid IN (:actors)"

        private const val FIND_ALL_NAME_PEOPLE = "SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
                "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
                "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
                "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
                "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid " +
                "WHERE auditLogDate > :fromTime " +
                " AND auditLogDate < :toTime AND Person.personUid IN (:people)"

        private const val FIND_ALL_NAME_ACTOR_CLAZZ = "SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
                "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
                "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
                "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
                "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid " +
                "WHERE auditLogDate > :fromTime " +
                " AND auditLogDate < :toTime  " +
                " AND Clazz.clazzUid IN (:clazzes) " +
                " AND Actor.personUid IN (:actors)"

        private const val FIND_ALL_NAME_ACTOR_PEOPLE = "SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
                "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
                "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
                "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
                "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid " +
                "WHERE auditLogDate > :fromTime " +
                " AND auditLogDate < :toTime  " +
                " AND Person.personUid in (:people) " +
                " AND Actor.personUid IN (:actors)"

        private const val FIND_ALL_NAME_PEOPLE_CLAZZ = "SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
                "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
                "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
                "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
                "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid " +
                "WHERE auditLogDate > :fromTime " +
                " AND auditLogDate < :toTime  " +
                " AND Clazz.clazzUid IN (:clazzes) " +
                " AND Person.personUid IN (:people)"

        private const val FIND_ALL_NAME_ACTOR_CLAZZ_PEOPLE = "SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
                "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
                "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
                "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
                "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid " +
                "WHERE auditLogDate > :fromTime " +
                " AND auditLogDate < :toTime " +
                " AND Clazz.clazzUid IN (:clazzes) " +
                " AND Actor.personUid IN (:actors) " +
                " AND Person.personUid in (:people) "
    }


}
