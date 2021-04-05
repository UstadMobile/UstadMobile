
package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.view.ContentEntryDetailAttemptsListView
import com.ustadmobile.core.view.SessionsListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.PersonWithStatementDisplay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.instance
import org.mockito.kotlin.*

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 */

class ContentEntryDetailAttemptsListPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: ContentEntryDetailAttemptsListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var statementDao: StatementDao

    private lateinit var di: DI

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }

        di = DI {
            import(ustadTestRule.diModule)
        }
        val repo: UmAppDatabase by di.activeRepoInstance()
        context = Any()
        statementDao = spy(repo.statementDao)
        whenever(repo.statementDao).thenReturn(statementDao)

    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        val presenterArgs = mutableMapOf<String,String>()
        presenterArgs[ARG_ENTITY_UID] = 1000L.toString()
        val presenter = ContentEntryDetailAttemptsListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(statementDao, timeout(5000)).findPersonsWithContentEntryAttempts(
                eq(1000L), any(), any(), any())
        verify(mockView, timeout(5000)).list = any()
    }

    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToSessionsList() {

        val systemImpl: UstadMobileSystemImpl by di.instance()

        val presenterArgs = mutableMapOf<String,String>()
        presenterArgs[ARG_ENTITY_UID] = 1000L.toString()
        val presenter = ContentEntryDetailAttemptsListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()

        presenter.onClickPersonWithStatementDisplay(PersonWithStatementDisplay().apply {
            attempts = 2
            progress = 100
            personUid = 1000L
            duration = 1000
        })

        verify(systemImpl, timeout(5000)).go(eq(SessionsListView.VIEW_NAME),
                eq(mapOf(ARG_ENTITY_UID to 1000L.toString(),
                        UstadView.ARG_PERSON_UID  to 1000L.toString())), any())
    }

}
