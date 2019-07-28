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
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.database.annotation.UmRestAccessible
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.authenticateEncryptedPassword
import com.ustadmobile.lib.util.encryptPassword
import com.ustadmobile.lib.util.getSystemTimeInMillis


@UmDao(selectPermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_PERSON_SELECT
        + ENTITY_LEVEL_PERMISSION_CONDITION2, updatePermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_PERSON_UPDATE
        + ENTITY_LEVEL_PERMISSION_CONDITION2)
@Dao
@UmRepository
abstract class PersonDao : BaseDao<Person> {

    class PersonUidAndPasswordHash {
        var passwordHash: String = ""

        var personUid: Long = 0
    }

    //TODO: KMP Undo when Server bits ready
    @UmRestAccessible
    //@UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
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
            onSuccessCreateAccessTokenAsync(person.personUid, username)
        }
    }

    @UmRestAccessible
    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
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

    @Insert
    abstract fun insertListAndGetIds(personList: List<Person>): List<Long>

  /*  @Query("UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE tableId = " + Person.TABLE_ID)
    protected abstract fun incrementPrimaryKey()*/

    private fun onSuccessCreateAccessTokenAsync(personUid: Long, username: String): UmAccount {
        var accessToken = AccessToken(personUid,
                getSystemTimeInMillis() + SESSION_LENGTH)

        //TODO: KMP Disable when Access Token TODO is fixed.
        accessToken = AccessToken(personUid, getSystemTimeInMillis() +
                SESSION_LENGTH, getSystemTimeInMillis().toString())
        insertAccessToken(accessToken)
        return UmAccount(personUid, username, accessToken.token, null)
    }

    fun authenticate(token: String, personUid: Long): Boolean {
        return isValidToken(token, personUid)
    }

    @Query("SELECT EXISTS(SELECT token FROM AccessToken WHERE token = :token and accessTokenPersonUid = :personUid)")
    abstract fun isValidToken(token: String, personUid: Long): Boolean

    @Insert
    abstract fun insertAccessToken(token: AccessToken)


    @Query("SELECT Person.personUid, PersonAuth.passwordHash " +
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
     * @param callback result callback
     */
    @Query("SELECT 1 FROM Person WHERE Person.personUid = :personUid AND (" +
            ENTITY_LEVEL_PERMISSION_CONDITION1 + " :permission " + ENTITY_LEVEL_PERMISSION_CONDITION2 + ") ")
    abstract fun personHasPermissionAsync(accountPersonUid: Long, personUid: Long, permission: Long): Boolean

    @Query("SELECT Person.* FROM PERSON Where Person.username = :username")
    abstract fun findByUsername(username: String?): Person?

    @Query("SELECT Person.* FROM PERSON WHERE Person.personUid = :uid")
    abstract fun findByUid(uid: Long): Person?

    @Query("SELECT * From Person WHERE personUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<Person?>

    @Query("SELECT Count(*) FROM Person")
    abstract fun countAll(): Long

    @Query("SELECT * FROM Person WHERE personUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : Person?


    @Query("SELECT * FROM Person WHERE active =1")
    abstract fun findAllPeopleProvider(): DataSource.Factory<Int, Person>

    @Query("SELECT * FROM Person WHERE active=1 ORDER BY firstNames ASC")
    abstract fun findAllPeopleNameAscProvider(): DataSource.Factory<Int, Person>

    @Query("SELECT * FROM Person WHERE active=1 ORDER BY firstNames DESC")
    abstract fun findAllPeopleNameDescProvider(): DataSource.Factory<Int, Person>

    @Update
    abstract fun updateAsync(entity: Person):Int

    @Query("Select * From Person WHERE active = 1")
    abstract fun findAllPeople(): List<Person>

    @Query("Select * From Person")
    abstract fun findAllPeopleIncludingInactive(): List<Person>

    @Query("select group_concat(firstNames||' '||lastNAme, ', ') " +
            " from Person WHERE personUid in (:uids)")
    abstract suspend fun findAllPeopleNamesInUidList(uids: List<Long>):String?


    @Insert
    abstract suspend fun insertPersonGroup(personGroup:PersonGroup):Long

    @Insert
    abstract suspend fun insertPersonGroupMember(personGroupMember:PersonGroupMember):Long

    suspend fun createPersonAndGroup(person:Person):Long{
        //1. Insert the person if unique
        try {
            val personUid = insertAsync(person)
            person.personUid = personUid

            //2. Create person group

            val personGroup = PersonGroup()
            personGroup.groupName =
                    (if (person.firstNames != null) {
                        person.firstNames
                    }else{
                        person.personUid.toString() }) + "'s group"
            val personGroupUid = insertPersonGroup(personGroup)

            //3. Create person group member and assign to it
            val personGroupMember = PersonGroupMember()
            personGroupMember.groupMemberGroupUid = personGroupUid
            personGroupMember.groupMemberPersonUid = personUid
            personGroupMember.groupMemberUid = insertPersonGroupMember(personGroupMember)

            return personGroupUid

        }catch(e:Exception){
            e.message
            return 0L
        }
    }

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
                " AND EntityRole.erEntityUid IN (SELECT DISTINCT clazzMemberClazzUid FROM ClazzMember WHERE clazzMemberPersonUid = Person.personUid))" +
                "OR" +
                "(EntityRole.ertableId = " + Location.TABLE_ID +
                " AND EntityRole.erEntityUid IN " +
                "(SELECT locationAncestorAncestorLocationUid FROM LocationAncestorJoin WHERE locationAncestorChildLocationUid " +
                "IN (SELECT personLocationLocationUid FROM PersonLocationJoin WHERE personLocationPersonUid = Person.personUid)))" +
                ") AND (Role.rolePermissions & "

        const val ENTITY_LEVEL_PERMISSION_CONDITION2 = ") > 0)"

        const val SESSION_LENGTH = 28L * 24L * 60L * 60L * 1000L// 28 days
    }
}
