package com.ustadmobile.mocks.db

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.core.db.dao.ClazzEnrolmentDao
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*

class ClazzEnrolmentDaoJs: ClazzEnrolmentDao() {
    override fun insertListAsync(entityList: List<ClazzEnrolment>) {
        TODO("Not yet implemented")
    }

    override suspend fun findByPersonUidAndClazzUidAsync(
        personUid: Long,
        clazzUid: Long
    ): ClazzEnrolment? {
        return ENTRIES.firstOrNull {
            it.clazzEnrolmentPersonUid == personUid && it.clazzEnrolmentClazzUid == clazzUid
        }
    }

    override fun findAllEnrolmentsByPersonAndClazzUid(
        personUid: Long,
        clazzUid: Long
    ): DoorDataSourceFactory<Int, ClazzEnrolmentWithLeavingReason> {
        val entries = ENTRIES.firstOrNull {
            it.clazzEnrolmentPersonUid == personUid && it.clazzEnrolmentClazzUid == clazzUid
        }.unsafeCast<List<ClazzEnrolmentWithLeavingReason>>()
        return DataSourceFactoryJs(entries)
    }

    override suspend fun findEnrolmentWithLeavingReason(enrolmentUid: Long): ClazzEnrolmentWithLeavingReason? {
        return ENTRIES.firstOrNull {
            it.clazzEnrolmentUid == enrolmentUid
        }.unsafeCast<ClazzEnrolmentWithLeavingReason>()
    }

    override suspend fun updateDateLeftByUid(clazzEnrolmentUid: Long, endDate: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun updateAsync(entity: ClazzEnrolment): Int {
        TODO("Not yet implemented")
    }

    override fun findAllClazzesByPersonWithClazz(personUid: Long): DoorDataSourceFactory<Int, ClazzEnrolmentWithClazzAndAttendance> {
        return DataSourceFactoryJs(ENTRIES.filter { it.clazzEnrolmentPersonUid == personUid})
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
    ): DoorDataSourceFactory<Int, PersonWithClazzEnrolmentDetails> {
        val entries = ENTRIES.filter { it.clazzEnrolmentRole ==  roleId}
        val personList = entries.map {
            PersonDaoJs.ENTRIES.first {
                    person -> person.personUid ==  it.clazzEnrolmentPersonUid
            }.unsafeCast<PersonWithClazzEnrolmentDetails>().apply {
                attendance = it.attendance
                earliestJoinDate = it.clazzEnrolmentDateJoined
                latestDateLeft = it.clazzEnrolmentDateLeft
                enrolmentRole = it.clazzEnrolmentRole
            }
        }
        return DataSourceFactoryJs(personList)
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

    companion object {
        val ENTRIES = listOf(
            ClazzEnrolmentWithClazzAndAttendance().apply {
                clazzEnrolmentUid = 1
                clazzEnrolmentClazzUid = ClazzDaoJs.ENTRIES.first().clazzUid
                attendance = 0.7f
                clazzEnrolmentDateJoined = 1623866241000L
                clazzEnrolmentDateLeft = 1725266241000L
                clazzEnrolmentPersonUid = PersonDaoJs.ENTRIES.first().personUid
                clazz = ClazzDaoJs.ENTRIES.first()
                clazzEnrolmentRole = ClazzEnrolment.ROLE_TEACHER
            },
            ClazzEnrolmentWithClazzAndAttendance().apply {
                clazzEnrolmentUid = 2
                clazzEnrolmentClazzUid = ClazzDaoJs.ENTRIES[1].clazzUid
                attendance = 0.2f
                clazzEnrolmentDateJoined = 1623266241000L
                clazzEnrolmentDateLeft = 1725266241000L
                clazzEnrolmentPersonUid = PersonDaoJs.ENTRIES[1].personUid
                clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT
                clazz = ClazzDaoJs.ENTRIES[1]
            },

            ClazzEnrolmentWithClazzAndAttendance().apply {
                clazzEnrolmentUid = 3
                clazzEnrolmentClazzUid = ClazzDaoJs.ENTRIES[2].clazzUid
                attendance = 0.9f
                clazzEnrolmentDateJoined = 1625266241000L
                clazzEnrolmentDateLeft = 1725266241000L
                clazzEnrolmentPersonUid = PersonDaoJs.ENTRIES[2].personUid
                clazz = ClazzDaoJs.ENTRIES[2]
                clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT_PENDING
            }
        )
    }
}