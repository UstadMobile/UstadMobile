package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.ReportEditView
import com.nhaarman.mockitokotlin2.*
import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.ReportDao
import com.ustadmobile.core.db.waitUntil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.*
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.lib.db.entities.ReportSeries
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.util.test.ext.insertTestStatements
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance

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

        runBlocking {
            repo.insertTestStatements()
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
            reportSeries = Json.stringify(ReportSeries.serializer().list, reportSeriesList)
        }

        val presenterArgs = mapOf(ARG_ENTITY_JSON to Json.stringify(ReportWithSeriesWithFilters.serializer(), testEntity))
        val presenter = ReportDetailPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleOnClickAddFromDashboard(testEntity)

        val existingEntitiesLive = db.reportDao.findAllActiveReportLive(false)
        val entitySaved = runBlocking {
            existingEntitiesLive.waitUntil(10000) { it.size == 1 }
        }.getValue()!!.first()
        Assert.assertEquals("Entity was saved to database", "New Report Title",
                entitySaved.reportTitle)




    }

    @Test
    fun givenReportExists_whenHandleOnClickEditCalled_thenSystemImplGoToEditViewIsCalled() {
        val repo: UmAppDatabase by di.activeRepoInstance()
        val systemImpl: UstadMobileSystemImpl by di.instance()

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

        verify(systemImpl, timeout(5000)).go(eq(ReportEditView.VIEW_NAME),
                eq(mapOf(ARG_ENTITY_UID to testEntity.reportUid.toString())), any())
    }

}