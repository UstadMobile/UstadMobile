
package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.view.StatementListView
import com.ustadmobile.core.view.SessionListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.mockito.kotlin.*

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 */

class StatementListPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: StatementListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var statementDao: StatementDao

    private lateinit var di: DI

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)

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
        presenterArgs[ARG_CONTENT_ENTRY_UID] = 1000L.toString()
        presenterArgs[ARG_PERSON_UID] = 1000L.toString()
        presenterArgs[SessionListView.ARG_CONTEXT_REGISTRATION] = "abc"
        val presenter = StatementListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(statementDao, timeout(5000)).findSessionDetailForPerson(
                eq(1000L), any(),eq(1000L), eq("abc"))
        verify(mockView, timeout(5000)).list = any()
    }


}
