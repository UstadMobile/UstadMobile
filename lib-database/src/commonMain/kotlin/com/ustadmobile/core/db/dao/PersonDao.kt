package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.PersonAuthDao.Companion.ENCRYPTED_PASS_PREFIX
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.door.util.KmpUuid
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.database.annotation.UmRestAccessible
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.authenticateEncryptedPassword
import com.ustadmobile.lib.util.encryptPassword
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.serialization.Serializable
import kotlin.js.JsName


@Dao
@Repository
abstract class PersonDao : BaseDao<Person> {

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

    inner class PersonWithGroup internal constructor(var personUid: Long, var personGroupUid: Long)

    @JsName("insertOrReplace")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrReplace(person: Person)

    @UmRestAccessible
    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
    suspend fun loginAsync(username: String, password: String): UmAccount? {

        val person = findUidAndPasswordHashAsync(username)
        return if (person == null) {
            null
        } else if (person.passwordHash.startsWith(PersonAuthDao.PLAIN_PASS_PREFIX) && person.passwordHash.substring(2) != password) {
            null
        } else if (person.passwordHash.startsWith(ENCRYPTED_PASS_PREFIX) && !authenticateEncryptedPassword(password,
                        person.passwordHash.substring(2))) {
            null
        } else {
            createAndInsertAccessToken(person.personUid, username)
        }
    }

    @Repository(methodType = Repository.METHOD_DELEGATE_TO_WEB)
    open suspend fun isUsernameAvailable(username: String): Boolean {
        val count = findByUsernameCount(username)
        println("count: " + count)
        return count == 0
    }

    @Query("SELECT COUNT(*) FROM Person where Person.username = :username")
    abstract suspend fun findByUsernameCount(username: String): Int

    @Repository(methodType = Repository.METHOD_DELEGATE_TO_WEB)
    suspend fun registerAsync(newPerson: Person, password: String): UmAccount? {
        if (newPerson.username.isNullOrBlank())
            throw IllegalArgumentException("New person to be registered has null or blank username")

        val person = findUidAndPasswordHashAsync(newPerson.username ?: "")
        if (person == null) {
            //OK to go ahead and create
            newPerson.personUid = insert(newPerson)
            val newPersonAuth = PersonAuth(newPerson.personUid,
                    ENCRYPTED_PASS_PREFIX + encryptPassword(password))
            insertPersonAuth(newPersonAuth)
            return createAndInsertAccessToken(newPerson.personUid, newPerson.username!!)
        } else {
            throw IllegalArgumentException("Username already exists")
        }
    }

    @Repository(methodType = Repository.METHOD_DELEGATE_TO_WEB)
    open suspend fun registerUser(firstName: String, lastName: String, email:String,
                                  username:String, password: String): Long {
        val newPerson = Person()
        newPerson.firstNames = firstName
        newPerson.lastName = lastName
        newPerson.emailAddr = email
        newPerson.username = username

        if (newPerson.username.isNullOrBlank()) {
            print("New person to be registered has null or blank username")
            return 0

        }else {

            val person = findUidAndPasswordHashAsync(newPerson.username ?: "")
            if (person == null) {
                //OK to go ahead and create
                newPerson.personUid = insert(newPerson)
                val newPersonAuth = PersonAuth(newPerson.personUid,
                        ENCRYPTED_PASS_PREFIX + encryptPassword(password))
                insertPersonAuth(newPersonAuth)
                println("New Person uid: " + newPerson.personUid)

                return newPerson.personUid
            } else {
                print("Username already exists")
                return 0
            }
        }
    }


    private fun createAndInsertAccessToken(personUid: Long, username: String): UmAccount {
        val accessToken = AccessToken(personUid,
                getSystemTimeInMillis() + SESSION_LENGTH)
        accessToken.token = KmpUuid.randomUUID().toString()

        insertAccessToken(accessToken)
        return UmAccount(personUid, username, accessToken.token, "")
    }

    fun authenticate(token: String, personUid: Long): Boolean {
        return isValidToken(token, personUid)
    }

    @Query("SELECT EXISTS(SELECT token FROM AccessToken WHERE token = :token " +
            " and accessTokenPersonUid = :personUid)")
    abstract fun isValidToken(token: String, personUid: Long): Boolean

    @Insert
    abstract fun insertAccessToken(token: AccessToken)


    @Query("SELECT Person.personUid,Person.admin,Person.firstNames, Person.lastName, PersonAuth.passwordHash " +
            " FROM Person LEFT JOIN PersonAuth ON Person.personUid = PersonAuth.personAuthUid " +
            "WHERE Person.username = :username")
    abstract suspend fun findUidAndPasswordHashAsync(username: String): PersonUidAndPasswordHash?

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
    @Query("SELECT EXISTS(SELECT 1 FROM Person WHERE " +
            "Person.personUid = :personUid AND :accountPersonUid IN ($ENTITY_PERSONS_WITH_PERMISSION_PARAM))")
    abstract suspend fun personHasPermissionAsync(accountPersonUid: Long, personUid: Long, permission: Long, checkPermissionForSelf: Int = 0): Boolean

    @Query("SELECT COALESCE((SELECT admin FROM Person WHERE personUid = :accountPersonUid), 0)")
    abstract suspend fun personIsAdmin(accountPersonUid: Long): Boolean

    @Query("SELECT Person.* FROM PERSON Where Person.username = :username")
    abstract fun findByUsername(username: String?): Person?

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

    @Query("""
         SELECT Person.* 
         ${Person.FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT1} ${Role.PERMISSION_PERSON_SELECT} ${Person.FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT2}
         WHERE
         PersonGroupMember.groupMemberPersonUid = :accountPersonUid
         AND PersonGroupMember.groupMemberActive 
         AND (:excludeClazz = 0 OR :excludeClazz NOT IN
            (SELECT clazzEnrolmentClazzUid FROM ClazzEnrolment WHERE clazzEnrolmentPersonUid = Person.personUid 
            AND :timestamp BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined AND ClazzEnrolment.clazzEnrolmentDateLeft
            AND ClazzEnrolment.clazzEnrolmentActive))
            AND (:excludeSchool = 0 OR :excludeSchool NOT IN
            (SELECT schoolMemberSchoolUid FROM SchoolMember WHERE schoolMemberPersonUid = Person.personUid 
            AND :timestamp BETWEEN SchoolMember.schoolMemberJoinDate AND SchoolMember.schoolMemberLeftDate )) 
            AND (Person.personUid NOT IN (:excludeSelected))
            AND Person.firstNames || ' ' || Person.lastName LIKE :searchText
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
    """)
    abstract fun findPersonsWithPermission(timestamp: Long, excludeClazz: Long,
                                                 excludeSchool: Long, excludeSelected: List<Long>,
                                                 accountPersonUid: Long, sortOrder: Int, searchText: String? = "%"): DataSource.Factory<Int, PersonWithDisplayDetails>

    @Query("""
        SELECT Person.* FROM Person 
            WHERE
            (:excludeClazz = 0 OR :excludeClazz NOT IN
            (SELECT clazzEnrolmentClazzUid FROM ClazzEnrolment WHERE clazzEnrolmentPersonUid = Person.personUid 
            AND :timestamp BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined AND ClazzEnrolment.clazzEnrolmentDateLeft ))
            AND (:excludeSchool = 0 OR :excludeSchool NOT IN
            (SELECT schoolMemberSchoolUid FROM SchoolMember WHERE schoolMemberPersonUid = Person.personUid 
            AND :timestamp BETWEEN SchoolMember.schoolMemberJoinDate AND SchoolMember.schoolMemberLeftDate )) 
            AND (Person.personUid NOT IN (:excludeSelected))
            AND :accountPersonUid IN ($ENTITY_PERSONS_WITH_SELECT_PERMISSION) 
            AND Person.firstNames || ' ' || Person.lastName LIKE :searchText
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
    """)
    abstract fun findPersonsWithPermissionAsList(timestamp: Long, excludeClazz: Long,
                                           excludeSchool: Long, excludeSelected: List<Long>,
                                           accountPersonUid: Long, sortOrder: Int, searchText: String? = "%"): List<Person>


    @Query("""SELECT Person.*, 
        
         (SELECT COUNT(DISTINCT StatementEntity.contextRegistration) 
         FROM StatementEntity WHERE Person.personUid = StatementEntity.statementPersonUid 
         AND statementContentEntryUid = :contentEntryUid) AS attempts, 
         
         (SELECT MAX(extensionProgress) FROM StatementEntity WHERE 
         Person.personUid = StatementEntity.statementPersonUid 
         AND statementContentEntryUid = :contentEntryUid) as progress, 
         
         (SELECT MAX(resultScoreScaled * 100) FROM StatementEntity WHERE 
         Person.personUid = StatementEntity.statementPersonUid 
         AND statementContentEntryUid = :contentEntryUid) as score,
          
         (SELECT MIN(timestamp) FROM StatementEntity WHERE 
         Person.personUid = StatementEntity.statementPersonUid 
         AND statementContentEntryUid = :contentEntryUid) as startDate,
         
          (SELECT MAX(timestamp) FROM StatementEntity WHERE 
         Person.personUid = StatementEntity.statementPersonUid 
         AND statementContentEntryUid = :contentEntryUid) as endDate,
         
          (SELECT SUM(resultDuration) FROM StatementEntity WHERE 
         Person.personUid = StatementEntity.statementPersonUid 
         AND statementContentEntryUid = :contentEntryUid) as duration
         
         
         ${Person.FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT1} ${Role.PERMISSION_PERSON_SELECT} ${Person.FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT2}
          WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid 
         AND PersonGroupMember.groupMemberActive  
         AND Person.personUid IN (SELECT statementPersonUid FROM StatementEntity WHERE 
         statementContentEntryUid = :contentEntryUid)
         GROUP BY Person.personUid 
         """)
    abstract fun findPersonsWithContentEntryAttempts(contentEntryUid: Long, accountPersonUid: Long)
                                    : DataSource.Factory<Int, PersonWithStatementDisplay>


    @Query("SELECT Person.* FROM Person WHERE Person.personUid = :personUid")
    abstract fun findByUidWithDisplayDetailsLive(personUid: Long): DoorLiveData<PersonWithDisplayDetails?>

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

        const val ENTITY_PERSONS_WITH_PERMISSION_PT1 = """
            SELECT DISTINCT Person_Perm.personUid FROM Person Person_Perm
            LEFT JOIN PersonGroupMember ON Person_Perm.personUid = PersonGroupMember.groupMemberPersonUid
            LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
            LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid
            WHERE
            CAST(Person_Perm.admin AS INTEGER) = 1 OR ( (
            """
        const val ENTITY_PERSONS_WITH_PERMISSION_PT2 =  """
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

        const val ENTITY_PERSONS_WITH_PERMISSION_PT4 = ") > 0)"

        const val ENTITY_PERSONS_WITH_SELECT_PERMISSION = "$ENTITY_PERSONS_WITH_PERMISSION_PT1 0 ${ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} $ENTITY_PERSONS_WITH_PERMISSION_PT4"

        const val ENTITY_PERSONS_WITH_PERMISSION_PARAM = "$ENTITY_PERSONS_WITH_PERMISSION_PT1 :checkPermissionForSelf $ENTITY_PERSONS_WITH_PERMISSION_PT2  :permission $ENTITY_PERSONS_WITH_PERMISSION_PT4"

        const val SESSION_LENGTH = 28L * 24L * 60L * 60L * 1000L// 28 days

    }

    @Serializable
    data class PersonNameAndUid(var personUid: Long = 0L, var name: String = ""){

        override fun toString(): String {
            return name
        }
    }
}
