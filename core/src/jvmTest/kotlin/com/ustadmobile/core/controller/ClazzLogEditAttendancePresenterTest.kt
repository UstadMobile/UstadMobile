package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.soywiz.klock.DateTime
import com.soywiz.klock.hours
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.core.db.waitUntil
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.core.view.ClazzLogEditAttendanceView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ATTENDED
import com.ustadmobile.util.test.ext.insertTestClazzAndMembers
import com.ustadmobile.util.test.ext.insertTestClazzLog
import com.ustadmobile.util.test.ext.insertTestRecordsForClazzLog
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI

class ClazzLogEditAttendancePresenterTest {

    private lateinit var mockView: ClazzLogEditAttendanceView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var di: DI

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    @Before
    fun setup() {
        mockView = mockEditView { }
        mockLifecycleOwner = mock { }
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        db = di.directActiveDbInstance()
        repo = di.directActiveRepoInstance()
    }

    @Test
    fun givenExistingClazzWithStudentsAndNoAttendanceLogsYet_whenLoadedFromDbAndAttendanceSet_thenShouldSetListWithAllMembersAndSaveToDatabase() {
        val testClazzAndMembers = runBlocking { repo.insertTestClazzAndMembers(5, 1) }
        val testClazzLog = runBlocking { repo.clazzLogDao.insertTestClazzLog(testClazzAndMembers.clazz.clazzUid) }

        val presenter = ClazzLogEditAttendancePresenter(context,
                mapOf(UstadView.ARG_ENTITY_UID to testClazzLog.clazzLogUid.toString()), mockView,
                di, mockLifecycleOwner)
        presenter.onCreate(null)

        //wait for the view to finish loading
        val entityVal = nullableArgumentCaptor<ClazzLog>().run {
            verify(mockView, timeout(5000).atLeastOnce()).entity = capture()
            firstValue
        }

        presenter.handleClickMarkAll(ClazzLogAttendanceRecord.STATUS_ATTENDED)

        presenter.handleClickSave(entityVal!!)

        nullableArgumentCaptor<DoorMutableLiveData<List<ClazzLogAttendanceRecordWithPerson>>>().apply {
            verify(mockView, timeout(5000).atLeastOnce()).clazzLogAttendanceRecordList = capture()
            assertEquals("Got expected number of class members", 5,
                    testClazzAndMembers.studentList.size)
        }
    }

    @Test
    fun givenExistingClazWithStudentsAndAttendanceLogsInDb_whenLoadedFromDb_thenShouldSetListWithAllMembers() {
        val testClazzAndMembers = runBlocking { repo.insertTestClazzAndMembers(5, 1) }
        val testClazzLog = runBlocking { repo.clazzLogDao.insertTestClazzLog(testClazzAndMembers.clazz.clazzUid) }
        val testAttendanceLogs = runBlocking {
            repo.clazzLogAttendanceRecordDao.insertTestRecordsForClazzLog(testClazzLog,
                    testClazzAndMembers.studentList)
        }


        val presenter = ClazzLogEditAttendancePresenter(context,
                mapOf(UstadView.ARG_ENTITY_UID to testClazzLog.clazzLogUid.toString()), mockView,
                di, mockLifecycleOwner)
        presenter.onCreate(null)

        nullableArgumentCaptor<DoorMutableLiveData<List<ClazzLogAttendanceRecordWithPerson>>>().apply {
            verify(mockView, timeout(5000)).clazzLogAttendanceRecordList = capture()
            assertEquals("Got expected number of class members", 5,
                    testClazzAndMembers.studentList.size)
        }
    }

    @Test
    fun givenExistingClazzLoaded_whenUserSelectsNextClazzDay_currentValuesAreSavedAndNextDayIsDisplayed() {
        val testClazzAndMembers = runBlocking {
            repo.insertTestClazzAndMembers(
                    5, 1, (DateTime.now() - 36.hours).unixMillisLong)
        }
        val testClazzLog = runBlocking {
            repo.clazzLogDao.insertTestClazzLog(testClazzAndMembers.clazz.clazzUid)
        }
        val prevTestClazzLog = ClazzLog().apply {
            logDate = System.currentTimeMillis() - 12.hours.millisecondsLong
            clazzLogClazzUid = testClazzAndMembers.clazz.clazzUid
            clazzLogUid = repo.clazzLogDao.insert(this)
        }

        val testAttendanceLogs = runBlocking {
            repo.clazzLogAttendanceRecordDao.insertTestRecordsForClazzLog(testClazzLog,
                    testClazzAndMembers.studentList)
        }

        val presenter = ClazzLogEditAttendancePresenter(context,
                mapOf(UstadView.ARG_ENTITY_UID to testClazzLog.clazzLogUid.toString()), mockView,
                di, mockLifecycleOwner)

        presenter.onCreate(null)

        //wait for loading to finish
        verify(mockView, timeout(5000)).loading = false

        val clazzLogAttendanceRecordLiveData = nullableArgumentCaptor<DoorMutableLiveData<List<ClazzLogAttendanceRecordWithPerson>>>().run {
            verify(mockView, timeout(5000)).clazzLogAttendanceRecordList = capture()
            firstValue
        }

        val initialViewOnEntity = mockView.captureLastEntityValue()

        clazzLogAttendanceRecordLiveData!!.setVal(clazzLogAttendanceRecordLiveData!!.getValue()!!.map {
            it.apply {
                attendanceStatus = STATUS_ATTENDED
            }
        })

        presenter.handleSelectClazzLog(initialViewOnEntity!!, prevTestClazzLog)

        //wait for items to be reset on the view
        runBlocking { clazzLogAttendanceRecordLiveData.waitUntil { it.all { it.attendanceStatus == 0 } } }

        //wait for items from previous log to be saved
        runBlocking {
            db.waitUntil(5000, listOf("ClazzLogAttendanceRecord")) {
                runBlocking {
                    db.clazzLogAttendanceRecordDao.findByClazzLogUid(testClazzLog.clazzLogUid).size ==
                            testClazzAndMembers.studentList.size
                }
            }
        }


        Assert.assertTrue("After selecting a different clazz log day, attendance records are reloaded",
                clazzLogAttendanceRecordLiveData.getValue()!!.all { it.attendanceStatus == 0 })
        Assert.assertEquals("After selecting a different clazz log day, same number of student records are loaded",
                testClazzAndMembers.studentList.size, clazzLogAttendanceRecordLiveData.getValue()!!.size)

        presenter.handleClickSave(initialViewOnEntity!!)
        runBlocking {
            db.waitUntil(5000, listOf("ClazzLogAttendanceRecord")) {
                runBlocking {
                    db.clazzLogAttendanceRecordDao.findByClazzLogUid(testClazzLog.clazzLogUid).all {
                        it.attendanceStatus == STATUS_ATTENDED
                    }
                }
            }
        }

        runBlocking {
            val clazzLog1AttendanceRecords = db.clazzLogAttendanceRecordDao.findByClazzLogUid(testClazzLog.clazzLogUid)
            Assert.assertEquals("Previous clazz log was recorded", testClazzAndMembers.studentList.size,
                    clazzLog1AttendanceRecords.size)
            Assert.assertTrue("Previous clazz log attendance records show all students aremarked present",
                    clazzLog1AttendanceRecords.all { it.attendanceStatus == STATUS_ATTENDED })
        }

    }

    @Test
    fun givenExistingClazzWithStudents_whenClickMarkAllThenSavedCalled_thenShouldSetAllAndSaveToDatabase() {
        val testClazzAndMembers = runBlocking { repo.insertTestClazzAndMembers(5, 1) }
        val testClazzLog = runBlocking { repo.clazzLogDao.insertTestClazzLog(testClazzAndMembers.clazz.clazzUid) }

        val presenter = ClazzLogEditAttendancePresenter(context,
                mapOf(UstadView.ARG_ENTITY_UID to testClazzLog.clazzLogUid.toString()), mockView,
                di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(5000)).clazzLogAttendanceRecordList = any()

        nullableArgumentCaptor<DoorMutableLiveData<List<ClazzLogAttendanceRecordWithPerson>>>().apply {
            verify(mockView, timeout(5000)).clazzLogAttendanceRecordList = capture()
            runBlocking {
                waitForLiveData(firstValue as DoorLiveData<List<ClazzLogAttendanceRecordWithPerson>>, 5000) {
                    it.size == testClazzAndMembers.studentList.size
                }

                presenter.handleClickMarkAll(ClazzLogAttendanceRecord.STATUS_ATTENDED)

                waitForLiveData(firstValue as DoorLiveData<List<ClazzLogAttendanceRecordWithPerson>>, 5000) {
                    it.size == testClazzAndMembers.studentList.size &&
                            it.all { it.attendanceStatus == ClazzLogAttendanceRecord.STATUS_ATTENDED }
                }

                assertEquals("Received the expected number of attendance records",
                        testClazzAndMembers.studentList.size, firstValue!!.getValue()!!.size)

                assertTrue("Last value marks all as attended",
                        firstValue!!.getValue()!!.all { it.attendanceStatus == ClazzLogAttendanceRecord.STATUS_ATTENDED })
            }
        }
    }

}