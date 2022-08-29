package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.core.db.dao.PersonDaoCommon.SQL_SELECT_LIST_WITH_PERMISSION
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.Person.Companion.FROM_PERSON_TO_SCOPEDGRANT_JOIN_ON_CLAUSE
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2


@DoorDao
@Repository
expect abstract class PersonDao : BaseDao<Person> {

    @Query("""
     REPLACE INTO PersonReplicate(personPk, personDestination)
      SELECT DISTINCT Person.personUid AS personUid,
             :newNodeId AS personDestination
        FROM UserSession
             JOIN PersonGroupMember
                ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
                   $JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1
                   ${Role.PERMISSION_PERSON_SELECT}
                   $JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2
       WHERE Person.personType = ${Person.TYPE_NORMAL_PERSON}
         AND UserSession.usClientNodeId = :newNodeId
         AND Person.personLct != COALESCE(
             (SELECT personVersionId
                FROM PersonReplicate
               WHERE personPk = Person.personUid
                 AND personDestination = :newNodeId), 0)              
      /*psql ON CONFLICT(personPk, personDestination) DO UPDATE
             SET personPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([Person::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

     @Query("""
 REPLACE INTO PersonReplicate(personPk, personDestination)
  SELECT DISTINCT Person.personUid AS personUid,
         UserSession.usClientNodeId AS personDestination
    FROM ChangeLog
         JOIN Person
             ON ChangeLog.chTableId = 9
                AND ChangeLog.chEntityPk = Person.personUid
         ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
            ${Role.PERMISSION_PERSON_SELECT}
            ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
   WHERE Person.personType = ${Person.TYPE_NORMAL_PERSON}
     AND UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND Person.personLct != COALESCE(
         (SELECT personVersionId
            FROM PersonReplicate
           WHERE personPk = Person.personUid
             AND personDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(personPk, personDestination) DO UPDATE
     SET personPending = true
  */               
 """)
    @ReplicationRunOnChange([Person::class])
    @ReplicationCheckPendingNotificationsFor([Person::class])
    abstract suspend fun replicateOnChange()

    @Insert
    abstract suspend fun insertListAsync(entityList: List<Person>)



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrReplace(person: Person)

    @Query("SELECT COUNT(*) FROM Person where Person.username = :username")
    abstract suspend fun findByUsernameCount(username: String): Int


    @Query("SELECT EXISTS(SELECT token FROM AccessToken WHERE token = :token " +
            " and accessTokenPersonUid = :personUid)")
    abstract fun isValidToken(token: String, personUid: Long): Boolean

    @Insert
    abstract fun insertAccessToken(token: AccessToken)


    @Query("""
        SELECT Person.personUid, Person.admin, Person.firstNames, Person.lastName, 
               PersonAuth.passwordHash
          FROM Person
               JOIN PersonAuth
                    ON Person.personUid = PersonAuth.personAuthUid
         WHERE Person.username = :username
    """)
    abstract suspend fun findUidAndPasswordHashAsync(username: String): PersonUidAndPasswordHash?

    @Query("""
        SELECT Person.*
          FROM Person
               JOIN PersonAuth2
                    ON Person.personUid = PersonAuth2.pauthUid
         WHERE Person.username = :username 
               AND PersonAuth2.pauthAuth = :passwordHash
    """)
    abstract suspend fun findByUsernameAndPasswordHash2(username: String, passwordHash: String): Person?

    @Insert
    abstract fun insertPersonAuth(personAuth: PersonAuth)

    /**
     * Checks if a user has the given permission over a given person in the database
     *
     * @param accountPersonUid the personUid of the person who wants to perform the operation
     * @param personUid the personUid of the person object in the database to perform the operation on
     * @param permission permission to check for
     * @param checkPermissionForSelf if 0 then don't check for permission when accountPersonUid == personUid
     * (e.g. where give the person permission over their own entity automatically).
     */
    @Query("""
        SELECT EXISTS(
                SELECT 1
                  FROM Person
                  JOIN ScopedGrant
                       ON $FROM_PERSON_TO_SCOPEDGRANT_JOIN_ON_CLAUSE
                  JOIN PersonGroupMember 
                       ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
                 WHERE Person.personUid = :personUid
                   AND (ScopedGrant.sgPermissions & :permission) > 0
                   AND PersonGroupMember.groupMemberPersonUid = :accountPersonUid
                 LIMIT 1)
    """)
    abstract suspend fun personHasPermissionAsync(
        accountPersonUid: Long,
        personUid: Long,
        permission: Long
    ): Boolean

    @Query("SELECT COALESCE((SELECT admin FROM Person WHERE personUid = :accountPersonUid), 0)")
    @PostgresQuery("SELECT COALESCE((SELECT admin FROM Person WHERE personUid = :accountPersonUid), FALSE)")
    abstract suspend fun personIsAdmin(accountPersonUid: Long): Boolean

    @Query("SELECT Person.* FROM PERSON Where Person.username = :username")
    abstract fun findByUsername(username: String?): Person?

    @Query("""
        SELECT Person.*
          FROM Person
         WHERE Person.dateOfBirth = :nodeId
           AND Person.personType = ${Person.TYPE_SYSTEM}
    """)
    abstract suspend fun findSystemAccount(nodeId: Long): Person?

    @Query("SELECT * FROM PERSON WHERE Person.personUid = :uid")
    abstract fun findByUid(uid: Long): Person?

    @Query("SELECT Person.*, null as newPassword, null as currentPassword,null as confirmedPassword" +
            " FROM PERSON WHERE Person.personUid = :uid")
    abstract suspend fun findPersonAccountByUid(uid: Long): PersonWithAccount?

    @Query("SELECT * From Person WHERE personUid = :uid")
    abstract fun findByUidLive(uid: Long): LiveData<Person?>

    @Query("SELECT * FROM Person WHERE personUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : Person?


    @Update
    abstract suspend fun updateAsync(entity: Person):Int

    @Insert
    abstract suspend fun insertPersonGroup(personGroup:PersonGroup):Long

    @Insert
    abstract suspend fun insertPersonGroupMember(personGroupMember:PersonGroupMember):Long

    @Query(SQL_SELECT_LIST_WITH_PERMISSION)
    abstract fun findPersonsWithPermission(timestamp: Long, excludeClazz: Long,
                                                 excludeSchool: Long, excludeSelected: List<Long>,
                                                 accountPersonUid: Long, sortOrder: Int, searchText: String? = "%"): DataSourceFactory<Int, PersonWithDisplayDetails>

    @Query(SQL_SELECT_LIST_WITH_PERMISSION)
    abstract fun findPersonsWithPermissionAsList(timestamp: Long, excludeClazz: Long,
                                           excludeSchool: Long, excludeSelected: List<Long>,
                                           accountPersonUid: Long, sortOrder: Int, searchText: String? = "%"): List<Person>



    @Query("""
        SELECT Person.*, PersonParentJoin.* 
          FROM Person
     LEFT JOIN PersonParentJoin on ppjUid = (
                SELECT ppjUid 
                  FROM PersonParentJoin
                 WHERE ppjMinorPersonUid = :personUid 
                       AND ppjParentPersonUid = :activeUserPersonUid 
                LIMIT 1)     
         WHERE Person.personUid = :personUid
        """)
    @QueryLiveTables(["Person", "PersonParentJoin"])
    abstract fun findByUidWithDisplayDetailsLive(personUid: Long, activeUserPersonUid: Long): LiveData<PersonWithPersonParentJoin?>

    @Insert
    abstract fun insertAuditLog(entity: AuditLog): Long

    @Query("SELECT * FROM Person")
    abstract fun getAllPerson(): List<Person>

}
