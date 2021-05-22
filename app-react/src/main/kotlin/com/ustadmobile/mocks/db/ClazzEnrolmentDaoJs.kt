package com.ustadmobile.mocks.db

import androidx.paging.DataSource
import com.ustadmobile.core.db.dao.ClazzEnrolmentDao
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import kotlinx.serialization.builtins.ListSerializer

class ClazzEnrolmentDaoJs: ClazzEnrolmentDao() {
    override fun insertListAsync(entityList: List<ClazzEnrolment>) {
        TODO("Not yet implemented")
    }

    override suspend fun findByPersonUidAndClazzUidAsync(
        personUid: Long,
        clazzUid: Long
    ): ClazzEnrolment? {
        TODO("Not yet implemented")
    }

    override fun findAllEnrolmentsByPersonAndClazzUid(
        personUid: Long,
        clazzUid: Long
    ): DataSource.Factory<Int, ClazzEnrolmentWithLeavingReason> {
        TODO("Not yet implemented")
    }

    override suspend fun findEnrolmentWithLeavingReason(enrolmentUid: Long): ClazzEnrolmentWithLeavingReason? {
        TODO("Not yet implemented")
    }

    override suspend fun updateDateLeftByUid(clazzEnrolmentUid: Long, endDate: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun updateAsync(entity: ClazzEnrolment): Int {
        TODO("Not yet implemented")
    }

    override fun findAllClazzesByPersonWithClazz(personUid: Long): DataSource.Factory<Int, ClazzEnrolmentWithClazzAndAttendance> {
        return DataSourceFactoryJs<Int,ClazzEnrolmentWithClazzAndAttendance, Any>(null,personUid, "",
            ListSerializer(ClazzEnrolmentWithClazzAndAttendance.serializer()))
    }

    override suspend fun findMaxEndDateForEnrolment(
        selectedClazz: Long,
        selectedPerson: Long,
        selectedEnrolment: Long
    ): Long {
        TODO("Not yet implemented")
    }

    override suspend fun findAllClazzesByPersonWithClazzAsListAsync(personUid: Long): List<ClazzEnrolmentWithClazz> {
        return listOf()
    }

    override suspend fun getAllClazzEnrolledAtTimeAsync(
        clazzUid: Long,
        date: Long,
        roleFilter: Int,
        personUidFilter: Long
    ): List<ClazzEnrolmentWithPerson> {
        TODO("Not yet implemented")
    }

    override suspend fun findByUid(uid: Long): ClazzEnrolment? {
        TODO("Not yet implemented")
    }

    override fun findByUidLive(uid: Long): DoorLiveData<ClazzEnrolment?> {
        TODO("Not yet implemented")
    }

    override fun findByClazzUidAndRole(
        clazzUid: Long,
        roleId: Int,
        sortOrder: Int,
        searchText: String?,
        filter: Int,
        accountPersonUid: Long,
        currentTime: Long
    ): DataSource.Factory<Int, PersonWithClazzEnrolmentDetails> {
        TODO("Not yet implemented")
    }

    override suspend fun updateClazzEnrolmentActiveForPersonAndClazz(
        personUid: Long,
        clazzUid: Long,
        roleId: Int,
        active: Boolean
    ): Int {
        TODO("Not yet implemented")
    }

    override fun updateClazzEnrolmentActiveForClazzEnrolment(
        clazzEnrolmentUid: Long,
        enrolled: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override suspend fun updateClazzEnrolmentRole(
        personUid: Long,
        clazzUid: Long,
        newRole: Int,
        oldRole: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override fun insert(entity: ClazzEnrolment): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: ClazzEnrolment): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<ClazzEnrolment>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<ClazzEnrolment>) {
        TODO("Not yet implemented")
    }

    override fun update(entity: ClazzEnrolment) {
        TODO("Not yet implemented")
    }
}