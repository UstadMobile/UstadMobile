package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.soywiz.klock.DateTime
import com.soywiz.klock.hours
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzLogDao
import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.view.ClazzLogListAttendanceView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.util.test.ext.insertClazzLogs
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI

class ClazzLogListAttendancePresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var repoClazzLogDao: ClazzLogDao

    private lateinit var mockView: ClazzLogListAttendanceView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var di: DI

    @Before
    fun setup(){
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
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
        val db: UmAppDatabase by di.activeDbInstance()

        val testClazz = Clazz("Test Clazz").apply {
            clazzUid = 42L
            clazzTimeZone = "Asia/Dubai"
        }
        db.clazzDao.insert(testClazz)


        val presenter = ClazzLogListAttendancePresenter(context,
                mapOf(UstadView.ARG_FILTER_BY_CLAZZUID to "42"), mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        verify(repoClazzLogDao, timeout(5000)).findByClazzUidAsFactory(42L,
                ClazzLog.STATUS_RESCHEDULED)
        verify(mockView, timeout(5000)).clazzTimeZone = "Asia/Dubai"
    }

    @Test
    fun givenExistingCompletedClazzLogs_whenOnCreateCalled_thenShouldSetGraphDataAndSetFabVisible() {
        val db: UmAppDatabase by di.activeDbInstance()

        val testClazz = Clazz("Test Clazz").apply {
            clazzTimeZone = "Asia/Dubai"
            clazzUid = db.clazzDao.insert(this)
        }

        val oneDayInMs = (1000 * 60 * 60 * 24)
        val timeNow = DateTime.now().toOffsetByTimezone("Asia/Dubai").localMidnight.utc.unixMillisLong + 12.hours.millisecondsLong
        val timeRange = (timeNow - oneDayInMs * 6) to timeNow

        //make five ClazzLogs showing attendance
        val numInClazz = 10
        val clazzLogs = runBlocking { db.insertClazzLogs(testClazz.clazzUid, 5) {index ->
            ClazzLog().apply {
                logDate = timeRange.first + (index * oneDayInMs)
                clazzLogNumAbsent = if(index.rem(2) == 0) 2 else 4
                clazzLogNumPresent = numInClazz - clazzLogNumAbsent
                clazzLogStatusFlag = ClazzLog.STATUS_RECORDED
            }
        } }

        val presenter = ClazzLogListAttendancePresenter(context,
                mapOf(UstadView.ARG_FILTER_BY_CLAZZUID to testClazz.clazzUid.toString()), mockView, di,
                mockLifecycleOwner)
        presenter.onCreate(null)
        nullableArgumentCaptor<DoorMutableLiveData<ClazzLogListAttendancePresenter.AttendanceGraphData>>() {
            verify(mockView, timeout(5000)).graphData = capture()
            runBlocking {
                waitForLiveData(firstValue as DoorLiveData<ClazzLogListAttendancePresenter.AttendanceGraphData>, 5000) {
                    it.percentageAttendedSeries.size == 5 && it.percentageLateSeries.size == 5
                }
            }

            val currentGraphData = firstValue!!.getValue()
            assertEquals("Found expected number of ClazzLogs in graph data", clazzLogs.size,
                currentGraphData!!.percentageAttendedSeries.size)
            assertTrue("Clazz logs have expected absent or present percentage value",
                    currentGraphData.percentageAttendedSeries.all { it.second == 0.8f || it.second == 0.6f })
        }

        verify(mockView, timeout(5000).atLeastOnce()).recordAttendanceButtonVisible = true
    }


}