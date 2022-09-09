package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ReportDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.util.safeParseList
import com.ustadmobile.core.view.*
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

class ReportFilterEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ReportFilterEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoReportDaoSpy: ReportDao

    private lateinit var di: DI

    private lateinit var testNavController: UstadNavController

    private lateinit var ustadBackStackEntry: UstadBackStackEntry

    private lateinit var savedStateHandle: UstadSavedStateHandle

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()

        savedStateHandle = mock{}

        ustadBackStackEntry = mock{
            on{savedStateHandle}.thenReturn(savedStateHandle)
        }

        testNavController = mock{
            on { currentBackStackEntry }.thenReturn(ustadBackStackEntry)
            on { getBackStackEntry(any()) }.thenReturn(ustadBackStackEntry)
        }

        di = DI {
            import(ustadTestRule.diModule)
            bind<UstadNavController>(overrides = true) with singleton { testNavController }
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoReportDaoSpy = spy(repo.reportDao)
        whenever(repo.reportDao).thenReturn(repoReportDaoSpy)
    }

    @Test
    fun givenNoDataWasEntered_whenClickSavedIsClicked_thenShowErrors(){

        val systemImpl: UstadMobileSystemImpl by di.instance()

        val reportFilter = ReportFilter().apply {
            reportFilterSeriesUid = 1
        }

        val presenterArgs = mapOf(
            UstadEditView.ARG_ENTITY_JSON to Json.encodeToString(ReportFilter.serializer(), reportFilter))
        val presenter = ReportFilterEditPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        presenter.handleClickSave(initialEntity)

        verify(systemImpl, timeout(5000)).getString(eq(MessageID.field_required_prompt), any())
    }

    @Test
    fun givenDataWasEntered_whenClickedSaved_thenFinishResult(){


        val reportFilter = ReportFilter().apply {
            reportFilterSeriesUid = 1
        }

        val presenterArgs = mapOf(UstadEditView.ARG_ENTITY_JSON to
                Json.encodeToString(ReportFilter.serializer(), reportFilter),
                UstadView.ARG_RESULT_DEST_VIEWNAME to "view",
            UstadView.ARG_RESULT_DEST_KEY to "key"
        )
        val presenter = ReportFilterEditPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        val initialEntity: ReportFilter = mockView.captureLastEntityValue()!!
        initialEntity.reportFilterField = ReportFilter.FIELD_PERSON_GENDER
        initialEntity.reportFilterCondition = ReportFilter.CONDITION_IS_NOT
        initialEntity.reportFilterDropDownValue = Person.GENDER_MALE
        initialEntity.reportFilterUid = 1

        presenter.handleClickSave(initialEntity)

        verify(savedStateHandle, timeout(2000))[any()] = argWhere<String> {
            safeParseList(di, ListSerializer(ReportFilter.serializer()),
                ReportFilter::class, it).first().reportFilterUid == initialEntity.reportFilterUid
        }

    }

}