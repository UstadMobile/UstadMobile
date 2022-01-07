package com.ustadmobile.core.db.dao

import com.ustadmobile.door.DoorDataSourceFactory
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.PersonAuthDao.Companion.ENCRYPTED_PASS_PREFIX
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.Person.Companion.FROM_PERSON_TO_SCOPEDGRANT_JOIN_ON_CLAUSE
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2
import com.ustadmobile.lib.util.encryptPassword
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.serialization.Serializable
import kotlin.js.JsName


@Dao
@Repository
abstract class PersonDao : BaseDao<Person> {

    @Query("""
     REPLACE INTO PersonReplicate(personPk, personDestination)
      SELECT Person.personUid AS personUid,
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
  SELECT Person.personUid AS personUid,
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

    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(entityList: List<Person>)

    class PersonUidAndPasswordHash {
        var passwordHash: String = ""

        var personUid: Long = 0

        var firstNames: String? = null

        var lastName: String? = null

        var admin: Boolean = false
    }

    @JsName("insertOrReplace")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrReplace(person: Person)

    @Query("SELECT COUNT(*) FROM Person where Person.username = :username")
    abstract suspend fun findByUsernameCount(username: String): Int


    fun authenticate(token: String, personUid: Long): Boolean {
        return isValidToken(token, personUid)
    }

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

    @JsName("findByUid")
    @Query("SELECT * FROM PERSON WHERE Person.personUid = :uid")
    abstract suspend fun findByUid(uid: Long): Person?

    @JsName("findPersonAccountByUid")
    @Query("SELECT Person.*, null as newPassword, null as currentPassword,null as confirmedPassword" +
            " FROM PERSON WHERE Person.personUid = :uid")
    abstract suspend fun findPersonAccountByUid(uid: Long): PersonWithAccount?

    @Query("SELECT * From Person WHERE personUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<Person?>

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
                                                 accountPersonUid: Long, sortOrder: Int, searchText: String? = "%"): DoorDataSourceFactory<Int, PersonWithDisplayDetails>

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
    abstract fun findByUidWithDisplayDetailsLive(personUid: Long, activeUserPersonUid: Long): DoorLiveData<PersonWithPersonParentJoin?>

    private fun createAuditLog(toPersonUid: Long, fromPersonUid: Long) {
        if(fromPersonUid != 0L) {
            val auditLog = AuditLog(fromPersonUid, Person.TABLE_ID, toPersonUid)
            insertAuditLog(auditLog)
        }
    }

    @Insert
    abstract fun insertAuditLog(entity: AuditLog): Long

    @JsName("getAllPerson")
    @Query("SELECT * FROM Person")
    abstract fun getAllPerson(): List<Person>


    companion object {

        const val SORT_FIRST_NAME_ASC = 1

        const val SORT_FIRST_NAME_DESC = 2

        const val SORT_LAST_NAME_ASC = 3

        const val SORT_LAST_NAME_DESC = 4

        const val SQL_SELECT_LIST_WITH_PERMISSION = """
         SELECT Person.* 
           FROM PersonGroupMember 
                ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_PERSON_SELECT}
                    ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
         WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid
           AND PersonGroupMember.groupMemberActive 
           AND (:excludeClazz = 0 OR :excludeClazz NOT IN
                    (SELECT clazzEnrolmentClazzUid 
                       FROM ClazzEnrolment 
                      WHERE clazzEnrolmentPersonUid = Person.personUid 
                            AND :timestamp BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                                AND ClazzEnrolment.clazzEnrolmentDateLeft
           AND ClazzEnrolment.clazzEnrolmentActive))
           AND (:excludeSchool = 0 OR :excludeSchool NOT IN
                    (SELECT schoolMemberSchoolUid
                      FROM SchoolMember 
                     WHERE schoolMemberPersonUid = Person.personUid 
                       AND :timestamp BETWEEN SchoolMember.schoolMemberJoinDate
                            AND SchoolMember.schoolMemberLeftDate )) 
           AND (Person.personUid NOT IN (:excludeSelected))
           AND (:searchText = '%' 
               OR Person.firstNames || ' ' || Person.lastName LIKE :searchText)
      GROUP BY Person.personUid
      ORDER BY CASE(:sortOrder)
               WHEN $SORT_FIRST_NAME_ASC THEN Person.firstNames
               WHEN $SORT_LAST_NAME_ASC THEN Person.lastName
               ELSE ''
               END ASC,
               CASE(:sortOrder)
               WHEN $SORT_FIRST_NAME_DESC THEN Person.firstNames
               WHEN $SORT_LAST_NAME_DESC THEN Person.lastName
               ELSE ''
               END DESC
    """


        private const val ENTITY_PERSONS_WITH_PERMISSION_PT1 = """
            SELECT DISTINCT Person_Perm.personUid FROM Person Person_Perm
            LEFT JOIN PersonGroupMember ON Person_Perm.personUid = PersonGroupMember.groupMemberPersonUid
            LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
            LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid
            WHERE
            CAST(Person_Perm.admin AS INTEGER) = 1 OR ( (
            """
        private const val ENTITY_PERSONS_WITH_PERMISSION_PT2 =  """
            = 0) AND (Person_Perm.personUid = Person.personUid))
            OR
            (
            ((EntityRole.erTableId = ${Person.TABLE_ID} AND EntityRole.erEntityUid = Person.personUid) OR 
            (EntityRole.erTableId = ${Clazz.TABLE_ID} AND EntityRole.erEntityUid IN (SELECT DISTINCT clazzEnrolmentClazzUid FROM ClazzEnrolment WHERE clazzEnrolmentPersonUid = Person.personUid)) OR
            (EntityRole.erTableId = ${School.TABLE_ID} AND EntityRole.erEntityUid IN (SELECT DISTINCT schoolMemberSchoolUid FROM SchoolMember WHERE schoolMemberPersonUid = Person.PersonUid)) OR
            (EntityRole.erTableId = ${School.TABLE_ID} AND EntityRole.erEntityUid IN (
                SELECT DISTINCT Clazz.clazzSchoolUid 
                FROM Clazz
                JOIN ClazzEnrolment ON ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid AND ClazzEnrolment.clazzEnrolmentPersonUid = Person.personUid
            ))
            ) 
            AND (Role.rolePermissions & 
        """

        private const val ENTITY_PERSONS_WITH_PERMISSION_PT4 = ") > 0)"

        const val SESSION_LENGTH = 28L * 24L * 60L * 60L * 1000L// 28 days

        @Deprecated("Replaced with ScopedGrant")
        const val ENTITY_PERSONS_WITH_LEARNING_RECORD_PERMISSION = "$ENTITY_PERSONS_WITH_PERMISSION_PT1 0 ${ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} $ENTITY_PERSONS_WITH_PERMISSION_PT4"


    }

    @Serializable
    data class PersonNameAndUid(var personUid: Long = 0L, var name: String = ""){

        override fun toString(): String {
            return name
        }
    }
}
