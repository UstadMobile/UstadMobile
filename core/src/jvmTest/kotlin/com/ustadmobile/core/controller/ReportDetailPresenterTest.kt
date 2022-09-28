package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.ReportEditView
import org.mockito.kotlin.*
import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.core.db.dao.ReportDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.*
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.lib.db.entities.ReportSeries
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import com.ustadmobile.util.test.ext.insertTestStatementsForReports
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.kodein.di.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ReportDetailPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ReportDetailView

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

        runBlocking {
            repo.insertTestStatementsForReports()
        }
    }

    @Test
    fun givenReportExists_whenOnCreateCalled_thenReportIsSetOnView() {
        val repo: UmAppDatabase by di.activeRepoInstance()

        val testEntity = ReportWithSeriesWithFilters().apply {
            //set variables here
            xAxis = Report.MONTH
            fromDate = DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportUid = repo.reportDao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.reportUid.toString())
        val presenter = ReportDetailPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)

        presenter.onCreate(null)

        val entityValSet = mockView.captureLastEntityValue()!!
        Assert.assertEquals("Expected entity was set on view",
                testEntity.reportUid, entityValSet.reportUid)
    }

    @Test
    fun givenNewReport_whenUserClicksOnAddToDashboard_thenDatabaseIsCreated() {
        val db: UmAppDatabase by di.activeDbInstance()

        val reportSeriesList = listOf(ReportSeries().apply {
            reportSeriesYAxis = ReportSeries.TOTAL_DURATION
            reportSeriesVisualType = ReportSeries.LINE_GRAPH
            reportSeriesSubGroup = Report.CLASS
            reportSeriesUid = 4
            reportSeriesName = "total duration"
        })

        val testEntity = ReportWithSeriesWithFilters().apply {
            //set variables here
            reportTitle = "New Report Title"
            xAxis = Report.MONTH
            fromDate = DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportSeries = Json.encodeToString(ListSerializer(ReportSeries.serializer()),
                reportSeriesList)
        }

        val presenterArgs = mapOf(ARG_ENTITY_JSON to
                Json.encodeToString(ReportWithSeriesWithFilters.serializer(), testEntity))
        val presenter = ReportDetailPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleOnClickAddFromDashboard(testEntity)

        runBlocking {
            db.waitUntil(5000, listOf("Report")){
                db.reportDao.findAllActiveReportList(false).size == 1
            }
        }

        val existingEntitiesLive = db.reportDao.findAllActiveReportList(false)[0]
        Assert.assertEquals("Entity was saved to database", "New Report Title",
                existingEntitiesLive.reportTitle)




    }

    @Test
    fun givenReportExists_whenHandleOnClickEditCalled_thenSystemImplGoToEditViewIsCalled() {
        val repo: UmAppDatabase by di.activeRepoInstance()
        val testNavController:UstadNavController = di.direct.instance()
        val testEntity = ReportWithSeriesWithFilters().apply {
            //set variables here
            xAxis = Report.MONTH
            fromDate = DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportUid = repo.reportDao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.reportUid.toString())
        val presenter = ReportDetailPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)

        presenter.onCreate(null)

        //wait for the entity value to be set
        mockView.captureLastEntityValue(5000)

        presenter.handleClickEdit()

        argumentCaptor<Map<String, String>>().apply {
            verify(testNavController, times(1)).navigate(any(), capture(), any())

            Assert.assertTrue("Same arguments were passed during navigation",
                lastValue[ARG_ENTITY_UID].toString() == testEntity.reportUid.toString())
        }
    }

}