package com.ustadmobile.mocks.db

import androidx.paging.DataSource
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import kotlinx.serialization.builtins.ListSerializer

class PersonDaoJs: PersonDao() {

    private val mPath: String = "people"

    override suspend fun insertListAsync(entityList: List<Person>) {
        TODO("Not yet implemented")
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

    override fun insertPersonAuth(personAuth: PersonAuth) {
        TODO("Not yet implemented")
    }

    override suspend fun personHasPermissionAsync(
        accountPersonUid: Long,
        personUid: Long,
        permission: Long,
        checkPermissionForSelf: Int
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun personIsAdmin(accountPersonUid: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun findByUsername(username: String?): Person? {
        TODO("Not yet implemented")
    }

    override suspend fun findByUid(uid: Long): Person? {
        TODO("Not yet implemented")
    }

    override suspend fun findPersonAccountByUid(uid: Long): PersonWithAccount? {
        TODO("Not yet implemented")
    }

    override fun findByUidLive(uid: Long): DoorLiveData<Person?> {
        TODO("Not yet implemented")
    }

    override suspend fun findByUidAsync(uid: Long): Person? {
        TODO("Not yet implemented")
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
        return DataSourceFactoryJs<Int,PersonWithDisplayDetails, Any>(null,accountPersonUid,mPath,
            ListSerializer(PersonWithDisplayDetails.serializer())
        )
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

    override fun findByUidWithDisplayDetailsLive(personUid: Long): DoorLiveData<PersonWithDisplayDetails?> {
        TODO("Not yet implemented")
    }

    override fun insertAuditLog(entity: AuditLog): Long {
        TODO("Not yet implemented")
    }

    override fun getAllPerson(): List<Person> {
        TODO("Not yet implemented")
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
}