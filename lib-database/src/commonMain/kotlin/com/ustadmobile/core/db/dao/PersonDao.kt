package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.PersonAuthDao.Companion.ENCRYPTED_PASS_PREFIX
import com.ustadmobile.core.db.dao.PersonDao.Companion.ENTITY_LEVEL_PERMISSION_CONDITION1
import com.ustadmobile.core.db.dao.PersonDao.Companion.ENTITY_LEVEL_PERMISSION_CONDITION2
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.QueryLiveTables
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.database.annotation.UmRestAccessible
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.authenticateEncryptedPassword
import com.ustadmobile.lib.util.encryptPassword
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.serialization.Serializable
import kotlin.js.JsName
import kotlin.math.log


@UmDao(selectPermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_PERSON_SELECT
        + ENTITY_LEVEL_PERMISSION_CONDITION2,
        updatePermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_PERSON_UPDATE
        + ENTITY_LEVEL_PERMISSION_CONDITION2)
@Dao
@UmRepository
abstract class  PersonDao : BaseDao<Person> {

    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(entityList: List<Person>)

    class PersonUidAndPasswordHash {
        var passwordHash: String = ""

        var personUid: Long = 0
    }

    inner class PersonWithGroup internal constructor(var personUid: Long, var personGroupUid: Long)


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
            return onSuccessCreateAccessTokenAsync(newPerson.personUid, newPerson.username!!)
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

    @Insert
    abstract fun insertListAndGetIds(personList: List<Person>): List<Long>

    private fun onSuccessCreateAccessTokenAsync(personUid: Long, username: String): UmAccount {

        val accessToken = AccessToken(personUid, getSystemTimeInMillis() +
                SESSION_LENGTH, getSystemTimeInMillis().toString())
        insertAccessToken(accessToken)
        return UmAccount(personUid, username, accessToken.token, null)
    }

    fun authenticate(token: String, personUid: Long): Boolean {
        return isValidToken(token, personUid)
    }

    @Query("SELECT EXISTS(SELECT token FROM AccessToken WHERE token = :token " +
            " and accessTokenPersonUid = :personUid)")
    abstract fun isValidToken(token: String, personUid: Long): Boolean

    @Insert
    abstract fun insertAccessToken(token: AccessToken)


    @Query("SELECT Person.personUid, PersonAuth.passwordHash " +
            " FROM Person LEFT JOIN PersonAuth ON Person.personUid = PersonAuth.personAuthUid " +
            "WHERE Person.username = :username")
    abstract suspend fun findUidAndPasswordHashAsync(username: String): PersonUidAndPasswordHash?

    @Insert
    abstract fun insertPersonAuth(personAuth: PersonAuth)

    @JsName("getAllPersons")
    @Query("SELECT Person.personUid, (Person.firstNames || ' ' || Person.lastName) AS name " +
            " FROM Person WHERE name LIKE :name AND Person.personUid NOT IN (:uidList)")
    abstract suspend fun getAllPersons(name: String, uidList: List<Long>): List<PersonNameAndUid>

    @JsName("getAllPersonsInList")
    @Query("SELECT Person.personUid, (Person.firstNames || ' ' || Person.lastName) AS name " +
            " FROM Person WHERE Person.personUid IN (:uidList)")
    abstract suspend fun getAllPersonsInList(uidList: List<Long>): List<PersonNameAndUid>

    /**
     * Checks if a user has the given permission over a given person in the database
     *
     * @param accountPersonUid the personUid of the person who wants to perform the operation
     * @param personUid the personUid of the person object in the database to perform the operation on
     * @param permission permission to check for
     * @param callback result callback
     */
    @Query("SELECT 1 FROM Person WHERE Person.personUid = :personUid AND (" +
            ENTITY_LEVEL_PERMISSION_CONDITION1 + " :permission " + ENTITY_LEVEL_PERMISSION_CONDITION2 + ") ")
    abstract fun personHasPermission(accountPersonUid: Long, personUid: Long, permission: Long): Boolean

    @Query("SELECT 1 FROM Person WHERE Person.personUid = :personUid AND (" +
            ENTITY_LEVEL_PERMISSION_CONDITION1 + " :permission " + ENTITY_LEVEL_PERMISSION_CONDITION2 + ") ")
    abstract fun personHasPermissionLive(accountPersonUid: Long, personUid: Long, permission: Long)
            : DoorLiveData<Boolean>

    @Query("SELECT Person.* FROM PERSON Where Person.username = :username")
    abstract fun findByUsername(username: String?): Person?

    @JsName("findByUid")
    @Query("SELECT * FROM PERSON WHERE Person.personUid = :uid")
    abstract suspend fun findByUid(uid: Long): Person?

    @Query("SELECT * From Person WHERE personUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<Person?>

    @Query("SELECT Count(*) FROM Person")
    abstract fun countAll(): Long

    @Query("SELECT * FROM Clazz")
    abstract fun findAllClazzes(): List<Clazz>

    @Query("SELECT * FROM Person WHERE personUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : Person?


    @Query("SELECT * FROM Person WHERE CAST(active AS INTEGER) =1 " +
            "AND Person.personRoleUid != (SELECT Role.roleUid FROM ROLE WHERE Role.roleName = '"  +
            Role.ROLE_NAME_CUSTOMER + "' LIMIT 1 ) " )
    abstract fun findAllPeopleProvider(): DataSource.Factory<Int, Person>

    @Query("SELECT * FROM Person WHERE CAST(active AS INTEGER)=1 " +
            "AND Person.personRoleUid != (SELECT Role.roleUid FROM ROLE WHERE Role.roleName = '"  +
             Role.ROLE_NAME_CUSTOMER + "' LIMIT 1 ) " +
            "ORDER BY firstNames ASC")
    abstract fun findAllPeopleNameAscProvider(): DataSource.Factory<Int, Person>

    //TODO: Query is wrong, used while testing. Please fix this. (Varuna)
    @Query("SELECT * FROM PERSON WHERE CAST(active AS INTEGER) = 1 AND Person.personRoleUid != :leUid " +
            " AND Person.personRoleUid = :roleUid  ORDER BY firstNames ASC")
    abstract fun findAllPeopleByLEAndRoleUid(leUid: Long, roleUid: Long): DataSource.Factory<Int, Person>

    @Query("SELECT * FROM Person WHERE CAST(active AS INTEGER)=1 " +
            "AND Person.personRoleUid != (SELECT Role.roleUid FROM ROLE WHERE Role.roleName = '"  +
            Role.ROLE_NAME_CUSTOMER + "' LIMIT 1 ) " +
            "ORDER BY firstNames DESC")
    abstract fun findAllPeopleNameDescProvider(): DataSource.Factory<Int, Person>

    @Update
    abstract fun updateAsync(entity: Person):Int

    @Query("Select * From Person WHERE CAST(active AS INTEGER) = 1")
    abstract fun findAllPeople(): List<Person>

    @Query("Select * From Person")
    abstract fun findAllPeopleIncludingInactive(): List<Person>

    @Query("select group_concat(firstNames||' '||lastNAme, ', ') " +
            " from Person WHERE personUid in (:uids)")
    abstract suspend fun findAllPeopleNamesInUidList(uids: List<Long>):String?


    @Query("SELECT * FROM Person WHERE admin = 1")
    abstract fun findAllAdminsAsList(): List<Person>


    @Query("SELECT Person.* , Role.*, (0) AS clazzUid, " +
            " (0) AS attendancePercentage, " +
            " '' AS clazzName, " +
            " (0) AS clazzMemberRole, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            " PersonPicture.personPicturePersonUid = Person.personUid ORDER BY picTimestamp " +
            " DESC LIMIT 1) AS personPictureUid, " +
            " CASE WHEN EXISTS " +
            " (SELECT * FROM PersonGroupMember WHERE PersonGroupMember.groupMemberGroupUid = :groupUid " +
            " AND PersonGroupMember.groupMemberPersonUid = Person.personUid AND PersonGroupMember.groupMemberActive = 1) " +
            "   THEN 1 " +
            "   ELSE 0 " +
            " END AS enrolled " +
            "  FROM Person " +
            " LEFT JOIN Role ON Role.roleUid = Person.personRoleUid " +
            "WHERE CAST(Person.active AS INTEGER) = 1 ORDER BY Person.firstNames ASC")
    abstract fun findAllPeopleWithEnrollmentInGroup(groupUid: Long): DataSource.Factory<Int, PersonWithEnrollment>

    @Insert
    abstract suspend fun insertPersonGroup(personGroup:PersonGroup):Long

    @Insert
    abstract suspend fun insertPersonGroupMember(personGroupMember:PersonGroupMember):Long


    @QueryLiveTables(["Person", "PersonPicture", "Role"])
    @Query(QUERY_FIND_ALL)
    abstract fun findAllPeopleWithEnrollment(): DataSource.Factory<Int, PersonWithEnrollment>

    @QueryLiveTables(["Person", "PersonPicture", "Role"])
    @Query(QUERY_FIND_ALL + QUERY_SEARCH_BIT)
    abstract fun findAllPeopleWithEnrollmentBySearch(searchQuery: String):
            DataSource.Factory<Int, PersonWithEnrollment>

    @QueryLiveTables(["Person", "PersonPicture", "Role"])
    @Query(QUERY_FIND_ALL + QUERY_SORT_BY_NAME_DESC)
    abstract fun findAllPeopleWithEnrollmentSortNameDesc(): DataSource.Factory<Int, PersonWithEnrollment>

    @QueryLiveTables(["Person", "PersonPicture", "Role"])
    @Query(QUERY_FIND_ALL + QUERY_SORT_BY_NAME_ASC)
    abstract fun findAllPeopleWithEnrollmentSortNameAsc(): DataSource.Factory<Int, PersonWithEnrollment>

    @Query("SELECT * FROM Person where CAST(active AS INTEGER) = 1")
    abstract fun findAllActiveLive(): DoorLiveData<List<Person>>


    private suspend fun createPersonCommon(person: Person, loggedInPersonUid: Long): PersonWithGroup{
        val personUid = insertAsync(person)
        person.personUid = personUid

        val personGroup = PersonGroup()
        personGroup.groupName = if (person.firstNames != null)
            person.firstNames + " 's individual group"
        else
            "" + "Individual Person group"

        personGroup.groupPersonUid = person.personUid
        val personGroupUid = insertPersonGroup(personGroup)
        personGroup.groupUid = personGroupUid

        val personGroupMember = PersonGroupMember()
        personGroupMember.groupMemberPersonUid = personUid
        personGroupMember.groupMemberGroupUid = personGroupUid
        personGroupMember.groupMemberActive = true
        val personGroupMemberUid = insertPersonGroupMember(personGroupMember)
        personGroupMember.groupMemberUid = personGroupMemberUid
        createAuditLog(personUid, loggedInPersonUid)

        val personWithGroup = PersonWithGroup(personUid, personGroupUid)
        return personWithGroup
    }

    /**
     * Creates actual person and assigns it to a group for permissions' sake. Use this
     * instead of direct insert.
     *
     * @param person    The person entity
     * @param callback  The callback.
     */
    suspend fun createPersonAsync(person: Person, loggedInPersonUid: Long):Long  {
        val personWithGroup = createPersonCommon(person, loggedInPersonUid)
        return personWithGroup.personUid
    }


    /**
     * Insert the person given and create a PersonGroup for it and set it to individual.
     * Note: this does not check if the person exists. The given person must not exist.
     *
     * @param person    The person object to persist. Must not already exist.
     * @param callback  The callback that returns PersonWithGroup pojo object - basically
     * Person Uids and PersonGroup Uids
     */
    suspend fun createPersonWithGroupAsync(person: Person): PersonWithGroup {
        val personWithGroup = createPersonCommon(person, 0)
        return personWithGroup

    }

    private fun createAuditLog(toPersonUid: Long, fromPersonUid: Long) {
        if(fromPersonUid != 0L) {
            val auditLog = AuditLog(fromPersonUid, Person.TABLE_ID, toPersonUid)
            insertAuditLog(auditLog)
        }
    }

    @Insert
    abstract fun insertAuditLog(entity: AuditLog): Long

    fun updatePersonAsync(entity: Person, loggedInPersonUid: Long): Int  {
        val result = updateAsync(entity)
        createAuditLog(entity.personUid, loggedInPersonUid)
        return result
    }

    @JsName("getAllPerson")
    @Query("SELECT * FROM Person")
    abstract fun getAllPerson(): List<Person>

    companion object {

        const val ENTITY_LEVEL_PERMISSION_CONDITION1 = " Person.personUid = :accountPersonUid OR" +
                "(SELECT admin FROM Person WHERE personUid = :accountPersonUid) = 1 OR " +
                "EXISTS(SELECT PersonGroupMember.groupMemberPersonUid FROM PersonGroupMember " +
                "JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid " +
                "JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
                "WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
                " AND (" +
                "(EntityRole.ertableId = " + Person.TABLE_ID +
                " AND EntityRole.erEntityUid = Person.personUid) " +
                "OR " +
                "(EntityRole.ertableId = " + Clazz.TABLE_ID +
                " AND EntityRole.erEntityUid IN (SELECT DISTINCT clazzMemberClazzUid FROM " +
                " ClazzMember WHERE clazzMemberPersonUid = Person.personUid))" +
                "OR" +
                "(EntityRole.ertableId = " + Location.TABLE_ID +
                " AND EntityRole.erEntityUid IN " +
                "(SELECT locationAncestorAncestorLocationUid FROM LocationAncestorJoin " +
                " WHERE locationAncestorChildLocationUid " +
                "IN (SELECT personLocationLocationUid FROM PersonLocationJoin " +
                " WHERE personLocationPersonUid = Person.personUid)))" +
                ") AND (Role.rolePermissions & "

        const val ENTITY_LEVEL_PERMISSION_CONDITION2 = ") > 0)"

        const val SESSION_LENGTH = 28L * 24L * 60L * 60L * 1000L// 28 days

        const val QUERY_FIND_ALL = "SELECT Person.* , Role.*, (0) AS clazzUid, " +
                "   '' AS clazzName, " +
                "   (0) AS attendancePercentage, " +
                "   (0) AS clazzMemberRole, " +
                "   (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
                "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY picTimestamp " +
                "   DESC LIMIT 1) AS personPictureUid, " +
                "   (0) AS enrolled " +
                " FROM Person " +
                " LEFT JOIN Role ON Role.roleUid = Person.personRoleUid " +
                " WHERE CAST(Person.active AS INTEGER) = 1 " +
                " AND Person.personRoleUid != " +
                "   (SELECT rol.roleUid FROM ROLE AS rol " +
                "   WHERE rol.roleName = '" + Role.ROLE_NAME_CUSTOMER + "' LIMIT 1 ) "

        const val QUERY_SEARCH_BIT = " AND (Person.firstNames || ' ' || Person.lastName) LIKE " +
            ":searchQuery "

        const val QUERY_SORT_BY_NAME_DESC = " ORDER BY Person.lastName DESC "
        const val QUERY_SORT_BY_NAME_ASC = " ORDER BY Person.firstNames ASC "
    }

    @Serializable
    data class PersonNameAndUid(var personUid: Long = 0L, var name: String = ""){

        override fun toString(): String {
            return name
        }
    }
}
