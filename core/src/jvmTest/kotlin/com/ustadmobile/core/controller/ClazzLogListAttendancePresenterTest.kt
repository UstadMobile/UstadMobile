package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.soywiz.klock.DateTime
import com.soywiz.klock.hours
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzLogDao
import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.asPerson
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.view.ClazzLogListAttendanceView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.*
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.test.ext.insertClazzLogs
import com.ustadmobile.util.test.ext.startLocalTestSessionBlocking
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.instance

class ClazzLogListAttendancePresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var repoClazzLogDao: ClazzLogDao

    private lateinit var mockView: ClazzLogListAttendanceView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var di: DI

    @Before
    fun setup(){
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoClazzLogDao = spy(repo.clazzLogDao)
        whenever(repo.clazzLogDao).thenReturn(repoClazzLogDao)
    }

    @Test
    fun givenClazzUidFilter_whenOnCreateCalled_thenShouldFindByClazzUidAndSetList() {
        val repo: UmAppDatabase by di.activeRepoInstance()

        val testClazz = Clazz("Test Clazz").apply {
            clazzUid = 42L
            clazzTimeZone = "Asia/Dubai"
        }
        repo.clazzDao.insert(testClazz)


        val presenter = ClazzLogListAttendancePresenter(context,
                mapOf(UstadView.ARG_CLAZZUID to "42"), mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(repoClazzLogDao, timeout(5000)).findByClazzUidAsFactory(42L,
                ClazzLog.STATUS_RESCHEDULED)
        verify(mockView, timeout(5000)).clazzTimeZone = "Asia/Dubai"
    }

    @Test
    fun givenExistingCompletedClazzLogs_whenOnCreateCalled_thenShouldSetGraphDataAndSetFabVisible() {
        val repo: UmAppDatabase by di.activeRepoInstance()
        val accountManager: UstadAccountManager by di.instance()

        val activePerson = Person().also {
            it.personUid = 42L
            it.username = "admin"
            it.firstNames = "Admin"
            it.lastName = "admin"
            it.admin = true
        }

        runBlocking {
            repo.insertPersonAndGroup(activePerson)
            repo.grantScopedPermission(activePerson.personGroupUid, Role.ALL_PERMISSIONS,
                ScopedGrant.ALL_TABLES, ScopedGrant.ALL_ENTITIES)
        }

        accountManager.startLocalTestSessionBlocking(activePerson,
            accountManager.activeEndpoint.url)

        val testClazz = Clazz("Test Clazz").apply {
            clazzTimeZone = "Asia/Dubai"
            clazzUid = repo.clazzDao.insert(this)
        }

        val oneDayInMs = (1000 * 60 * 60 * 24)
        val timeNow = DateTime.now().toOffsetByTimezone("Asia/Dubai").localMidnight.utc.unixMillisLong + 12.hours.millisecondsLong
        val timeRange = (timeNow - oneDayInMs * 6) to timeNow

        //make five ClazzLogs showing attendance
        val numInClazz = 10
        val clazzLogs = runBlocking { repo.insertClazzLogs(testClazz.clazzUid, 5) {index ->
            ClazzLog().apply {
                logDate = timeRange.first + (index * oneDayInMs)
                clazzLogNumAbsent = if(index.rem(2) == 0) 2 else 4
                clazzLogNumPresent = numInClazz - clazzLogNumAbsent
                clazzLogStatusFlag = ClazzLog.STATUS_RECORDED
            }
        } }

        val presenter = ClazzLogListAttendancePresenter(context,
                mapOf(UstadView.ARG_CLAZZUID to testClazz.clazzUid.toString()), mockView, di,
                mockLifecycleOwner)
        presenter.onCreate(null)
        nullableArgumentCaptor<MutableLiveData<ClazzLogListAttendancePresenter.AttendanceGraphData>>() {
            verify(mockView, timeout(5000)).graphData = capture()
            runBlocking {
                waitForLiveData(firstValue as LiveData<ClazzLogListAttendancePresenter.AttendanceGraphData>, 5000) {
                    it.percentageAttendedSeries.size == 5 && it.percentageLateSeries.size == 5
                }
            }

            val currentGraphData = firstValue!!.getValue()
            assertEquals("Found expected number of ClazzLogs in graph data", clazzLogs.size,
                currentGraphData!!.percentageAttendedSeries.size)
            assertTrue("Clazz logs have expected absent or present percentage value",
                    currentGraphData.percentageAttendedSeries.all { it.second == 0.8f || it.second == 0.6f })
        }

        verify(mockView, timeout(5000).atLeastOnce()).recordAttendanceOptions = argWhere {
            ClazzLogListAttendancePresenter.RecordAttendanceOption.RECORD_ATTENDANCE_MOST_RECENT_SCHEDULE in it
                    && ClazzLogListAttendancePresenter.RecordAttendanceOption.RECORD_ATTENDANCE_NEW_SCHEDULE in it
        }
    }


    @Test
    fun givenUserDoesNotHaveRecordAttendancePermission_whenOnCreateCalled_thenShouldSetRecordOptionsAsEmpty() {
        val repo: UmAppDatabase by di.activeRepoInstance()

        val testClazz = Clazz("Test Clazz").apply {
            clazzTimeZone = "Asia/Dubai"
            clazzUid = repo.clazzDao.insert(this)
        }

        val presenter = ClazzLogListAttendancePresenter(context,
                mapOf(UstadView.ARG_CLAZZUID to testClazz.clazzUid.toString()), mockView, di,
                mockLifecycleOwner)
        presenter.onCreate(null)

        verify(mockView, timeout(5000).atLeastOnce()).recordAttendanceOptions = argWhere {
            it.isEmpty()
        }
    }

}