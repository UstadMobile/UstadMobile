package com.ustadmobile.core.controller

import com.google.gson.Gson
import org.mockito.kotlin.*
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
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
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

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoReportDaoSpy: ReportDao

    private lateinit var di: DI

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
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

        val jsonStr = Gson().toJson(initialEntity)

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
                eq(mapOf(UstadEditView.ARG_ENTITY_JSON to "".trimMargin())), any())

    }


    @Test
    fun givenExistingReport_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val reportSeriesList = listOf(ReportSeries().apply {
            reportSeriesYAxis = ReportSeries.TOTAL_DURATION
            reportSeriesVisualType = ReportSeries.LINE_GRAPH
            reportSeriesSubGroup = Report.CLASS
            reportSeriesUid = 4
            reportSeriesName = "total duration"
        })


        val testEntity = ReportWithSeriesWithFilters().apply {
            reportTitle = "Old Title"
            xAxis = Report.MONTH
            reportSeries = Json.encodeToString(ListSerializer(ReportSeries.serializer()),
                reportSeriesList)
            reportSeriesWithFiltersList = reportSeriesList
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