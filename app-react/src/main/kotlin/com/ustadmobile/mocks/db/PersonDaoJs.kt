package com.ustadmobile.mocks.db

import androidx.paging.DataSource
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mocks.DoorLiveDataJs
import com.ustadmobile.mocks.db.DatabaseJs.Companion.ALLOW_ACCESS

class PersonDaoJs: PersonDao() {
    
    private val mPath: String = "people"

    override suspend fun insertListAsync(entityList: List<Person>) {
        TODO("Not yetfindPersonAccountByUid implemented")
    }

    override suspend fun insertOrReplace(person: Person) {
        TODO("Not yet implemented")
    }

    override suspend fun findByUsernameCount(username: String): Int {
        TODO("Not yet implemented")
    }

    override fun isValidToken(token: String, personUid: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun insertAccessToken(token: AccessToken) {
        TODO("Not yet implemented")
    }

    override suspend fun findUidAndPasswordHashAsync(username: String): PersonUidAndPasswordHash? {
        TODO("Not yet implemented")
    }

    override suspend fun findByUsernameAndPasswordHash2(
        mUsername: String,
        passwordHash: String
    ): Person? {
        return ENTRIES.first().apply {
            username = mUsername
        }
    }

    override fun insertPersonAuth(personAuth: PersonAuth) {
        TODO("Not yet implemented")
    }

    override suspend fun personHasPermissionAsync(
        accountPersonUid: Long,
        personUid: Long,
        permission: Long
    ): Boolean {
        return ALLOW_ACCESS
    }

    override suspend fun personIsAdmin(accountPersonUid: Long): Boolean {
        return ENTRIES.first().admin
    }

    override fun findByUsername(username: String?): Person? {
        return ENTRIES.first()
    }

    override suspend fun findByUid(uid: Long): Person? {
        return ENTRIES.first().apply {
            personUid = uid
        }
    }

    override suspend fun findPersonAccountByUid(uid: Long): PersonWithAccount? {
        return ENTRIES.first().apply {
            personUid = uid
        }
    }

    override fun findByUidLive(uid: Long): DoorLiveData<Person?> {
        return DoorLiveDataJs(ENTRIES.first().apply {
            personUid = uid
        })
    }

    override suspend fun findByUidAsync(uid: Long): Person? {
        return ENTRIES.first().apply {
            personUid = uid
        }
    }

    override suspend fun updateAsync(entity: Person): Int {
        TODO("Not yet implemented")
    }

    override suspend fun insertPersonGroup(personGroup: PersonGroup): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertPersonGroupMember(personGroupMember: PersonGroupMember): Long {
        TODO("Not yet implemented")
    }

    override fun findPersonsWithPermission(
        timestamp: Long,
        excludeClazz: Long,
        excludeSchool: Long,
        excludeSelected: List<Long>,
        accountPersonUid: Long,
        sortOrder: Int,
        searchText: String?
    ): DataSource.Factory<Int, PersonWithDisplayDetails> {
        return DataSourceFactoryJs(ENTRIES.unsafeCast<List<PersonWithDisplayDetails>>())
    }

    override fun findPersonsWithPermissionAsList(
        timestamp: Long,
        excludeClazz: Long,
        excludeSchool: Long,
        excludeSelected: List<Long>,
        accountPersonUid: Long,
        sortOrder: Int,
        searchText: String?
    ): List<Person> {
        TODO("Not yet implemented")
    }

    override fun findByUidWithDisplayDetailsLive(
        mPersonUid: Long,
        activeUserPersonUid: Long
    ): DoorLiveData<PersonWithPersonParentJoin?> {
        return DoorLiveDataJs(ENTRIES.first().apply {
            personUid = mPersonUid
        }) as DoorLiveData<PersonWithPersonParentJoin?>
    }

    override fun insertAuditLog(entity: AuditLog): Long {
        TODO("Not yet implemented")
    }

    override fun getAllPerson(): List<Person> {
        return ENTRIES
    }

    override fun insert(entity: Person): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: Person): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<Person>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<Person>) {
        TODO("Not yet implemented")
    }

    override fun update(entity: Person) {
        TODO("Not yet implemented")
    }
    
    companion object {
        val ENTRIES = listOf(
            PersonWithAccount().apply {
                username = "admin"
                firstNames = "Admin"
                admin = true
                emailAddr = "admin@admin.com"
                phoneNum = "+255 71242 5886"
                personAddress = "Miyuji Proper, Dodoma"
                dateOfBirth = 706563066000
                lastName = "Users"
                gender = Person.GENDER_OTHER
            },
            PersonWithAccount().apply {
                username = "janeDoe"
                firstNames = "Jane"
                admin = true
                emailAddr = "jane@users.com"
                phoneNum = "+255 71242 5886"
                personAddress = "Proper, Dodoma"
                dateOfBirth = 706513066080
                lastName = "Doe"
                gender = Person.GENDER_FEMALE
            },
            PersonWithAccount().apply {
                username = "johnDoe"
                firstNames = "John"
                admin = true
                emailAddr = "john@users.com"
                phoneNum = "+255 71242 5886"
                personAddress = "Proper, Dodoma"
                dateOfBirth = 716513066080
                lastName = "Doe"
                gender = Person.GENDER_FEMALE
            }
        )
    }
}