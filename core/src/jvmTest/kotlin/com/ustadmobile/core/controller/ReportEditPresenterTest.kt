package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.dao.ReportDao
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ReportWithFilters
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReportEditPresenterTest {

    @JvmField
    @Rule
    var systemImplRule = SystemImplRule()

    @JvmField
    @Rule
    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)

    private lateinit var mockView: ReportEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoReportDaoSpy: ReportDao

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()
        repoReportDaoSpy = spy(clientDbRule.db.reportDao)
        whenever(clientDbRule.db.reportDao).thenReturn(repoReportDaoSpy)

    }


    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldGoToDetailWithJson() {
        val presenterArgs = mapOf<String, String>()

        val presenter = ReportEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //TODO: Make some changes (e.g. as the user would do using data binding
        initialEntity.reportTitle = "New Report Title"

        presenter.handleClickSave(initialEntity)

        verify(systemImplRule.systemImpl, timeout(5000)).go(eq(ReportDetailView.VIEW_NAME),
                eq(mapOf(UstadEditView.ARG_ENTITY_JSON to """{"reportUid":0,"reportOwnerUid":0,
                    |"chartType":100,"xAxis":300,"yAxis":201,"subGroup":0,"fromDate":0,"toDate":0,
                    |"reportTitle":"New Report Title","reportInactive":false,
                    |"reportMasterChangeSeqNum":0,"reportLocalChangeSeqNum":0,
                    |"reportLastChangedBy":0,"reportFilterList":[]}"}""".trimMargin())), any())
    }

    @Test
    fun givenExistingReport_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val testEntity = ReportWithFilters().apply {
            reportTitle = "Old Title"
            reportUid = clientDbRule.repo.reportDao.insert(this)
        }

        val presenterArgs = mapOf(UstadView.ARG_ENTITY_UID to testEntity.reportUid.toString())
        val presenter = ReportEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity.reportTitle = "new Title"

        presenter.handleClickSave(initialEntity)

        val entitySaved = runBlocking {
            clientDbRule.db.reportDao.findByUid(initialEntity.reportUid)
        }

        Assert.assertEquals("Name was saved and updated",
                "new Title", entitySaved?.reportTitle)
    }




}