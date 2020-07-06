package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ReportDao
import com.ustadmobile.core.db.waitUntil
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ReportWithFilters
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.instance

class ReportEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ReportEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoReportDaoSpy: ReportDao

    private lateinit var di: DI

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoReportDaoSpy = spy(repo.reportDao)
        whenever(repo.reportDao).thenReturn(repoReportDaoSpy)
    }


    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldGoToDetailWithJson() {
        val presenterArgs = mapOf<String, String>()
        val systemImpl: UstadMobileSystemImpl by di.instance()

        val presenter = ReportEditPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        initialEntity.reportTitle = "New Report Title"

        presenter.handleClickSave(initialEntity)

        val jsonStr = Json.stringify(ReportWithFilters.serializer(), initialEntity)

        verify(systemImpl, timeout(5000)).go(eq(ReportDetailView.VIEW_NAME),
              eq(mapOf(UstadEditView.ARG_ENTITY_JSON to jsonStr)), eq(context))
    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalledAndTitleNotSet_thenShouldNotGoToDetailWithJson() {
        val presenterArgs = mapOf<String, String>()
        val systemImpl: UstadMobileSystemImpl by di.instance()

        val presenter = ReportEditPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        presenter.handleClickSave(initialEntity)

        verify(systemImpl, timeout(5000)).getString(eq(MessageID.field_required_prompt), any())

        // verify its never called because view would show as error
        verify(systemImpl, never()).go(eq(ReportDetailView.VIEW_NAME),
                eq(mapOf(UstadEditView.ARG_ENTITY_JSON to """{"reportUid":0,"reportOwnerUid":0,
                    |"chartType":100,"xAxis":300,"yAxis":201,"subGroup":0,"fromDate":0,"toDate":0,
                    |"reportTitle":"New Report Title","reportInactive":false,
                    |"reportMasterChangeSeqNum":0,"reportLocalChangeSeqNum":0,
                    |"reportLastChangedBy":0,"reportFilterList":[]}"}""".trimMargin())), any())

    }


    @Test
    fun givenExistingReport_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()
        val testEntity = ReportWithFilters().apply {
            reportTitle = "Old Title"
            reportUid = repo.reportDao.insert(this)
        }

        val presenterArgs = mapOf(UstadView.ARG_ENTITY_UID to testEntity.reportUid.toString())
        val presenter = ReportEditPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity.reportTitle = "new Title"

        presenter.handleClickSave(initialEntity)

        val entitySaved = runBlocking {
            repo.reportDao.findByUidLive(initialEntity.reportUid)
                    .waitUntil(5000) { it?.reportTitle == "new Title" }.getValue()
        }

        Assert.assertEquals("Name was saved and updated",
                "new Title", entitySaved?.reportTitle)
    }


}