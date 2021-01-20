
package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ReportDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.ReportTemplateView
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Report
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */

class ReportTemplatePresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ReportTemplateView

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
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        //TODO: insert any entities that are used only in this test
        val repo: UmAppDatabase by di.activeRepoInstance()
        val testEntity = Report().apply {
            //set variables here
            reportTitle = "Blank Report"
            reportDescription = "Blank Description"
            reportUid = repo.reportDao.insert(this)
        }

        val presenterArgs = mapOf<String,String>()
        val presenter = ReportTemplatePresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoReportDaoSpy, timeout(5000)).findAllActiveReport(any(), eq(0), any(), eq(true))
        verify(mockView, timeout(5000)).list = any()
    }

    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToEditView() {
        val systemImpl: UstadMobileSystemImpl by di.instance()

        val presenterArgs = mapOf<String,String>()
        val testEntity = Report().apply {
            //set variables here
            isTemplate = true
            reportUid = 1000
        }
        val presenter = ReportTemplatePresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()

        presenter.handleClickEntry(testEntity)

        val jsonString = safeStringify(di, Report.serializer(), testEntity.apply {
            isTemplate = false
            reportUid = 0
        })

        verify(systemImpl, timeout(5000)).go(eq(ReportEditView.VIEW_NAME),
                eq(mapOf(UstadEditView.ARG_ENTITY_JSON to jsonString)), any())
    }


}
