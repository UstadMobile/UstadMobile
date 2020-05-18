package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzLogDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzLogListAttendanceView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.UmAccount
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
        mockLifecycleOwner = mock { }
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

}