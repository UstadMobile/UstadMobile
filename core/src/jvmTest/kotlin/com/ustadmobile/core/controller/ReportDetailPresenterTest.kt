package com.ustadmobile.core.controller

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.ReportEditView
import com.nhaarman.mockitokotlin2.*
import com.soywiz.klock.DateTime
import com.ustadmobile.core.util.SystemImplRule
import com.ustadmobile.core.util.UmAppDatabaseClientRule
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.core.db.dao.ReportDao
import com.ustadmobile.core.db.waitUntil
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.ReportWithFilters
import com.ustadmobile.util.test.AbstractXapiReportOptionsTest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ReportDetailPresenterTest : AbstractXapiReportOptionsTest() {

    @JvmField
    @Rule
    var systemImplRule = SystemImplRule()

    @JvmField
    @Rule
    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)

    private lateinit var mockView: ReportDetailView

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

        insertXapi(clientDbRule.db)
    }

    @Test
    fun givenReportExists_whenOnCreateCalled_thenReportIsSetOnView() {
        val testEntity = ReportWithFilters().apply {
            //set variables here
            chartType = Report.BAR_CHART
            xAxis = Report.MONTH
            fromDate =  DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportFilterList = listOf(
                    ReportFilter().apply {
                        entityUid = 100
                        entityType = ReportFilter.PERSON_FILTER
                    },
                    ReportFilter().apply {
                        entityUid = 200
                        entityType = ReportFilter.VERB_FILTER
                    },
                    ReportFilter().apply {
                        entityUid = 300
                        entityType = ReportFilter.CONTENT_FILTER
                    }
            )
            reportUid = clientDbRule.db.reportDao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.reportUid.toString())
        val presenter = ReportDetailPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)


        presenter.onCreate(null)

        val entityValSet = mockView.captureLastEntityValue()!!
        Assert.assertEquals("Expected entity was set on view",
                testEntity.reportUid, entityValSet.reportUid)
    }

    @Test
    fun givenNewReport_whenUserClicksOnAddToDashboard_thenDatabaseIsCreated(){
        val testEntity = ReportWithFilters().apply {
            //set variables here
            reportTitle = "New Report Title"
            chartType = Report.BAR_CHART
            xAxis = Report.MONTH
            fromDate =  DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportFilterList = listOf(
                    ReportFilter().apply {
                        entityUid = 100
                        entityType = ReportFilter.PERSON_FILTER
                    },
                    ReportFilter().apply {
                        entityUid = 200
                        entityType = ReportFilter.VERB_FILTER
                    },
                    ReportFilter().apply {
                        entityUid = 300
                        entityType = ReportFilter.CONTENT_FILTER
                    }
            )
        }

        val presenterArgs = mapOf(ARG_ENTITY_JSON to Json.stringify(ReportWithFilters.serializer(), testEntity))
        val presenter = ReportDetailPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)

        presenter.onCreate(null)


        presenter.handleOnClickAddFromDashboard(testEntity)

        val existingEntitiesLive = clientDbRule.db.reportDao.findAllLive()
        val entitySaved = runBlocking {
            existingEntitiesLive.waitUntil { it.size == 1 }
        }.getValue()!!.first()
        Assert.assertEquals("Entity was saved to database", "New Report Title",
                entitySaved.reportTitle)




    }

    @Test
    fun givenReportExists_whenHandleOnClickEditCalled_thenSystemImplGoToEditViewIsCalled() {
        val testEntity = ReportWithFilters().apply {
            //set variables here
            chartType = Report.BAR_CHART
            xAxis = Report.MONTH
            fromDate =  DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportFilterList = listOf(
                    ReportFilter().apply {
                        entityUid = 100
                        entityType = ReportFilter.PERSON_FILTER
                    },
                    ReportFilter().apply {
                        entityUid = 200
                        entityType = ReportFilter.VERB_FILTER
                    },
                    ReportFilter().apply {
                        entityUid = 300
                        entityType = ReportFilter.CONTENT_FILTER
                    }
            )
            reportUid = clientDbRule.db.reportDao.insert(this)
        }
        val presenterArgs = mapOf(ARG_ENTITY_UID to testEntity.reportUid.toString())
        val presenter = ReportDetailPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner,
                systemImplRule.systemImpl, clientDbRule.db, clientDbRule.repo,
                clientDbRule.accountLiveData)

        presenter.onCreate(null)

        //wait for the entity value to be set
        mockView.captureLastEntityValue(120000)

        presenter.handleClickEdit()

        verify(systemImplRule.systemImpl, timeout(5000)).go(eq(ReportEditView.VIEW_NAME),
                eq(mapOf(ARG_ENTITY_UID to testEntity.reportUid.toString())), any())
    }

}