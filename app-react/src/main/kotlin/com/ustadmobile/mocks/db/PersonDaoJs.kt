package com.ustadmobile.mocks.db

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mocks.DoorLiveDataJs
import com.ustadmobile.mocks.db.DatabaseJs.Companion.ALLOW_ACCESS

class PersonDaoJs: PersonDao() {

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
        username: String,
        passwordHash: String
    ): Person? {
        return ENTRIES.first{it.username == username}
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
        return ENTRIES.first{it.personUid == accountPersonUid}.admin
    }

    override fun findByUsername(username: String?): Person? {
        return ENTRIES.first{it.username == username}
    }

    override suspend fun findByUid(uid: Long): Person? {
        return ENTRIES.first{it.personUid == uid}
    }

    override suspend fun findPersonAccountByUid(uid: Long): PersonWithAccount? {
        return ENTRIES.first{it.personUid == uid}
    }

    override fun findByUidLive(uid: Long): DoorLiveData<Person?> {
        return DoorLiveDataJs(ENTRIES.first{it.personUid == uid})
    }

    override suspend fun findByUidAsync(uid: Long): Person? {
        return ENTRIES.first{it.personUid == uid}
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
    ): DoorDataSourceFactory<Int, PersonWithDisplayDetails> {
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
        personUid: Long,
        activeUserPersonUid: Long
    ): DoorLiveData<PersonWithPersonParentJoin?> {
        return DoorLiveDataJs(ENTRIES.first{it.personUid == personUid}
            .unsafeCast<PersonWithPersonParentJoin>())
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
                personUid = 1
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
                personUid = 2
                username = "janeDoe"
                firstNames = "Jane"
                admin = false
                emailAddr = "jane@users.com"
                phoneNum = "+255 71242 5886"
                personAddress = "Proper, Dodoma"
                dateOfBirth = 706513066080
                lastName = "Doe"
                gender = Person.GENDER_FEMALE
            },
            PersonWithAccount().apply {
                personUid = 3
                username = "johnDoe"
                firstNames = "John"
                admin = false
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