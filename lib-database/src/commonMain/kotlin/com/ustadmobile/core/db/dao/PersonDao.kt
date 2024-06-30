package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.core.db.dao.PersonDaoCommon.SQL_SELECT_LIST_WITH_PERMISSION
import app.cash.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.composites.PersonAndListDisplayDetails
import com.ustadmobile.lib.db.composites.PersonAndPicture
import com.ustadmobile.lib.db.composites.PersonNames
import com.ustadmobile.lib.db.entities.*


@DoorDao
@Repository
expect abstract class PersonDao : BaseDao<Person> {

    @Insert
    abstract suspend fun insertListAsync(entityList: List<Person>)



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrReplace(person: Person)

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.HTTP_OR_THROW,
    )
    @Query("SELECT COUNT(*) FROM Person where Person.username = :username")
    abstract suspend fun countUsername(username: String): Int


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

    @Query("SELECT COALESCE((SELECT admin FROM Person WHERE personUid = :accountPersonUid), 0)")
    @PostgresQuery("SELECT COALESCE((SELECT admin FROM Person WHERE personUid = :accountPersonUid), FALSE)")
    abstract suspend fun personIsAdmin(accountPersonUid: Long): Boolean

    @Query("SELECT Person.* FROM PERSON Where Person.username = :username")
    abstract fun findByUsername(username: String?): Person?

    @Query("SELECT Person.* FROM PERSON Where Person.username = :username")
    abstract suspend fun findByUsernameAsync(username: String): Person?

    @Query("""
        SELECT Person.*
          FROM Person
         WHERE Person.dateOfBirth = :nodeId
           AND Person.personType = ${Person.TYPE_SYSTEM}
    """)
    abstract suspend fun findSystemAccount(nodeId: Long): Person?

    @Query("SELECT * FROM PERSON WHERE Person.personUid = :uid")
    abstract fun findByUid(uid: Long): Person?

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT Person.*, PersonPicture.*
          FROM Person
               LEFT JOIN PersonPicture
                    ON PersonPicture.personPictureUid = Person.personUid
         WHERE Person.personUid = :accountPersonUid           
    """)
    abstract suspend fun findByUidWithPicture(accountPersonUid: Long): PersonAndPicture?

    @Query("""
        SELECT Person.*, PersonPicture.*
          FROM Person
               LEFT JOIN PersonPicture
                    ON PersonPicture.personPictureUid = Person.personUid
         WHERE Person.personUid = :uid           
    """)
    abstract fun findByUidWithPictureAsFlow(uid: Long): Flow<PersonAndPicture?>

    @Query("SELECT * From Person WHERE personUid = :uid")
    abstract fun findByUidLive(uid: Long): Flow<Person?>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("SELECT * FROM Person WHERE personUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : Person?

    @Query("SELECT * FROM Person WHERE personUid = :uid")
    abstract fun findByUidAsFlow(uid: Long): Flow<Person?>


    @Update
    abstract suspend fun updateAsync(entity: Person):Int

    @Insert
    abstract suspend fun insertPersonGroup(personGroup:PersonGroup):Long

    @Insert
    abstract suspend fun insertPersonGroupMember(personGroupMember:PersonGroupMember):Long

    @Query(SQL_SELECT_LIST_WITH_PERMISSION)
    abstract fun findPersonsWithPermissionAsList(
        timestamp: Long,
        excludeClazz: Long,
        excludeSelected: List<Long>,
        accountPersonUid: Long,
        sortOrder: Int,
        searchText: String? = "%"
    ): List<PersonAndListDisplayDetails>

    @Query(SQL_SELECT_LIST_WITH_PERMISSION)
    @HttpAccessible(
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findPersonsWithPermissionAsPagingSource"),
            HttpServerFunctionCall(
                functionName = "findAllByPersonUid",
                functionDao = SystemPermissionDao::class,
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            ),
            HttpServerFunctionCall(
                functionName = "findApplicableCoursePermissionEntitiesForAccountPerson",
                functionDao = CoursePermissionDao::class,
            ),
            HttpServerFunctionCall(
                functionName = "findClazzEnrolmentEntitiesForPersonViewPermissionCheck",
                functionDao = ClazzEnrolmentDao::class,
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "otherPersonUid",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "0",
                    )
                )
            )
        )
    )
    abstract fun findPersonsWithPermissionAsPagingSource(
        timestamp: Long,
        excludeClazz: Long,
        excludeSelected: List<Long>,
        accountPersonUid: Long,
        sortOrder: Int,
        searchText: String? = "%"
    ): PagingSource<Int, PersonAndListDisplayDetails>

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
    abstract fun findByUidWithDisplayDetailsLive(personUid: Long, activeUserPersonUid: Long): Flow<PersonAndDisplayDetail?>


    @Query("""
        SELECT Person.*, PersonParentJoin.* , PersonPicture.*, TransferJobItem.*
          FROM Person
               LEFT JOIN PersonParentJoin 
                    ON ppjUid =
                    (SELECT ppjUid 
                       FROM PersonParentJoin
                      WHERE ppjMinorPersonUid = :personUid 
                        AND ppjParentPersonUid = :accountPersonUid 
                      LIMIT 1)  
               LEFT JOIN PersonPicture
                    ON PersonPicture.personPictureUid = :personUid
               LEFT JOIN TransferJobItem
                    ON TransferJobItem.tjiUid = 
                       (SELECT TransferJobItem.tjiUid
                          FROM TransferJobItem
                         WHERE TransferJobItem.tjiEntityUid = :personUid
                           AND TransferJobItem.tjiTableId = ${PersonPicture.TABLE_ID}
                           AND TransferJobItem.tjiEntityEtag = PersonPicture.personPictureLct
                           AND TransferJobItem.tjiStatus != 21
                         LIMIT 1)
                          
         WHERE Person.personUid = :personUid
        """)
    @QueryLiveTables(["Person", "PersonPicture", "PersonParentJoin", "TransferJobItem"])
    @HttpAccessible
    abstract fun findByUidWithDisplayDetailsFlow(
        personUid: Long,
        accountPersonUid: Long
    ): Flow<PersonAndDisplayDetail?>


    @Query("SELECT * FROM Person")
    abstract fun getAllPerson(): List<Person>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(functionName = "findByUidAsync")
        )
    )
    @Query("""
        SELECT Person.firstNames, Person.lastName
          FROM Person
         WHERE Person.personUid = :uid  
    """)
    abstract fun getNamesByUid(uid: Long): Flow<PersonNames?>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(functionName = "findByUidAsync")
        )
    )
    @Query("""
        SELECT Person.firstNames, Person.lastName
          FROM Person
         WHERE Person.personUid = :uid  
    """)
    abstract suspend fun getNamesByUidAsync(uid: Long): PersonNames?



    @Query("""
        UPDATE Person
           SET username = :username,
               personLct = :currentTime
         WHERE Person.personUid = :personUid  
    """)
    abstract suspend fun updateUsername(personUid: Long, username: String, currentTime: Long): Int

    @Query("""
        SELECT Person.username
          FROM Person
         WHERE Person.username IN (:usernames)
    """)
    abstract suspend fun selectExistingUsernames(usernames: List<String>): List<String?>


}
