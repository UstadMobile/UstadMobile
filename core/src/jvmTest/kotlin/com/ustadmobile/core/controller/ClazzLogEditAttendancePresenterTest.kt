package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzLogEditAttendanceView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.getSystemTimeInMillis
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ClazzLogEditAttendancePresenterTest {

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var mockView: ClazzLogEditAttendanceView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var systemImpl: UstadMobileSystemImpl

    private lateinit var activeAccount: DoorMutableLiveData<UmAccount?>

    data class MockLogAndClazzSet(val clazz: Clazz, val personList: List<Person>,
        val clazzMemberList: List<ClazzMember>, val clazzLog: ClazzLog)


    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock { }
        context = Any()
        systemImpl = spy(UstadMobileSystemImpl.instance)
        activeAccount = DoorMutableLiveData(UmAccount(42, "bobjones", "",
                "http://localhost"))
        val realDb = UmAppDatabase.getInstance(context)
        db =  spy(realDb) { }
        db.clearAllTables()
        repo = spy(realDb) { }

    }

    fun createMockLogAndClazzSet(): MockLogAndClazzSet {
        val mockClazz = Clazz("Test Clazz").apply {
            clazzTimeZone = "Asia/Dubai"
            clazzUid = repo.clazzDao.insert(this)
        }

        val mockPeople = (1 .. 5).map {
            Person("user$it", "Bob", "Jones").apply {
                personUid = repo.personDao.insert(this)
            }
        }

        val clazzJoinTime = getSystemTimeInMillis() - 1000

        val mockClazzMembers = mockPeople.map {
            ClazzMember(mockClazz.clazzUid, it.personUid).apply {
                clazzMemberDateJoined = clazzJoinTime
                clazzMemberUid = repo.clazzMemberDao.insert(this)
            }
        }

        val mockClazzLog = ClazzLog().apply {
            logDate = getSystemTimeInMillis()
            this.clazzLogClazzUid = mockClazz.clazzUid
        }

        return MockLogAndClazzSet(mockClazz, mockPeople, mockClazzMembers, mockClazzLog)
    }



    @Test
    fun givenExistingClazzWithStudentsAndNoAttendanceLogsYet_whenLoadedFromDb_thenShouldSetListWithAllMembers() {
        val mockLogAndClazzSet = createMockLogAndClazzSet()

        val presenter = ClazzLogEditAttendancePresenter(context,
                mapOf(UstadView.ARG_ENTITY_UID to mockLogAndClazzSet.clazzLog.clazzLogUid.toString()), mockView,
                mockLifecycleOwner, systemImpl, db, repo, activeAccount)
        presenter.onCreate(null)

        nullableArgumentCaptor<DoorMutableLiveData<List<ClazzLogAttendanceRecordWithPerson>>>().apply {
            verify(mockView, timeout(5000)).clazzLogAttendanceRecordList = capture()
            assertEquals("Got expected number of class members", 5,
                    mockLogAndClazzSet.clazzMemberList.size)
        }
    }

    @Test
    fun givenExistingClazWithStudentsAndAttendanceLogsInDb_whenLoadedFromDb_thenShouldSetListWithAllMembers() {
        val mockLogAndClazzSet = createMockLogAndClazzSet()

        val mockClazzAttendanceLogs = mockLogAndClazzSet.personList.map {currentPerson ->
            ClazzLogAttendanceRecordWithPerson().apply {
                this.person = currentPerson
                this.clazzLogAttendanceRecordClazzLogUid = mockLogAndClazzSet.clazzLog.clazzLogUid
                this.clazzLogAttendanceRecordClazzMemberUid = mockLogAndClazzSet.clazzMemberList
                        .find { it.clazzMemberPersonUid == currentPerson.personUid }!!.clazzMemberUid
                this.clazzLogAttendanceRecordUid = repo.clazzLogAttendanceRecordDao.insert(this)
            }
        }

        val presenter = ClazzLogEditAttendancePresenter(context,
                mapOf(UstadView.ARG_ENTITY_UID to mockLogAndClazzSet.clazzLog.clazzLogUid.toString()), mockView,
                mockLifecycleOwner, systemImpl, db, repo, activeAccount)
        presenter.onCreate(null)

        nullableArgumentCaptor<DoorMutableLiveData<List<ClazzLogAttendanceRecordWithPerson>>>().apply {
            verify(mockView, timeout(5000)).clazzLogAttendanceRecordList = capture()
            assertEquals("Got expected number of class members", 5,
                    mockLogAndClazzSet.clazzMemberList.size)
        }
    }


}