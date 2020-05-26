package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzLogDao
import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzLogListAttendanceView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.util.test.ext.insertClazzLogs
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class ClazzLogListAttendancePresenterTest {

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var repoClazzLogDao: ClazzLogDao

    private lateinit var mockView: ClazzLogListAttendanceView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var systemImpl: UstadMobileSystemImpl

    private lateinit var activeAccount: DoorMutableLiveData<UmAccount?>



    @Before
    fun setup(){
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }

        context = Any()
        systemImpl = spy(UstadMobileSystemImpl.instance)
        activeAccount = DoorMutableLiveData(UmAccount(42, "bobjones", "",
                "http://localhost"))

        val realDb = UmAppDatabase.getInstance(context)
        repoClazzLogDao = spy(realDb.clazzLogDao)

        db =  spy(realDb) { }
        db.clearAllTables()

        repo = spy(realDb) {
            on { clazzLogDao }.thenReturn(repoClazzLogDao)
        }
    }

    @Test
    fun givenClazzUidFilter_whenOnCreateCalled_thenShouldFindByClazzUidAndSetList() {
        val testClazz = Clazz("Test Clazz").apply {
            clazzUid = 42L
            clazzTimeZone = "Asia/Dubai"
        }
        db.clazzDao.insert(testClazz)


        val presenter = ClazzLogListAttendancePresenter(context,
                mapOf(UstadView.ARG_FILTER_BY_CLAZZUID to "42"), mockView, mockLifecycleOwner,
                systemImpl, db, repo, activeAccount)
        presenter.onCreate(null)

        verify(repoClazzLogDao, timeout(5000)).findByClazzUidAsFactory(42L)
        verify(mockView, timeout(5000)).clazzTimeZone = "Asia/Dubai"
    }

    @Test
    fun givenExistingCompletedClazzLogs_whenOnCreateCalled_thenShouldSetGraphData() {
        val testClazz = Clazz("Test Clazz").apply {
            clazzTimeZone = "Asia/Dubai"
            clazzUid = db.clazzDao.insert(this)
        }

        val oneDayInMs = (1000 * 60 * 60 * 24)
        val oneWeekInMs = (oneDayInMs * 7)
        val timeNow = System.currentTimeMillis()
        val timeRange = (timeNow - oneWeekInMs) to timeNow

        //make five ClazzLogs showing attendance
        val numInClazz = 10
        val clazzLogs = runBlocking { db.insertClazzLogs(testClazz.clazzUid, 5) {index ->
            ClazzLog().apply {
                logDate = timeRange.first + (index * oneDayInMs) + (1000 * 60 * 60 * 8)
                clazzLogNumAbsent = if(index.rem(2) == 0) 2 else 4
                clazzLogNumPresent = numInClazz - clazzLogNumAbsent
                clazzLogStatusFlag = ClazzLog.STATUS_RECORDED
            }
        } }

        val presenter = ClazzLogListAttendancePresenter(context,
                mapOf(UstadView.ARG_FILTER_BY_CLAZZUID to testClazz.clazzUid.toString()), mockView, mockLifecycleOwner,
                systemImpl, db, repo, activeAccount)
        presenter.onCreate(null)
        nullableArgumentCaptor<DoorMutableLiveData<ClazzLogListAttendancePresenter.AttendanceGraphData>>() {
            verify(mockView, timeout(5000 * 10000)).graphData = capture()
            runBlocking {
                waitForLiveData(firstValue as DoorLiveData<ClazzLogListAttendancePresenter.AttendanceGraphData>, 5000) {
                    it.graphData.size == 5
                }
            }

            val currentGraphData = firstValue!!.getValue()
            assertEquals("Found expected number of ClazzLogs in graph data", clazzLogs.size,
                currentGraphData!!.graphData.size)
            assertTrue("Clazz logs have expected absent or present percentage value",
                    currentGraphData.graphData.all { it.second == 0.8f || it.second == 0.6f })
        }
    }


}