
package com.ustadmobile.core.controller
import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.LanguageDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.waitForListToBeSet
import com.ustadmobile.core.util.safeParseList
import com.ustadmobile.core.view.LanguageEditView
import com.ustadmobile.core.view.LanguageListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.Language
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */

class LanguageListPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: LanguageListView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private lateinit var repoLanguageDaoSpy: LanguageDao

    private lateinit var testNavController: UstadNavController

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
        testNavController = di.direct.instance()
        val repo: UmAppDatabase by di.activeRepoInstance()
        context = Any()
        repoLanguageDaoSpy = spy(repo.languageDao)
        whenever(repo.languageDao).thenReturn(repoLanguageDaoSpy)
    }

    @Test
    fun givenPresenterNotYetCreated_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnView() {
        val presenterArgs = mapOf<String,String>()
        val presenter = LanguageListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)

        //eg. verify the correct DAO method was called and was set on the view
        verify(repoLanguageDaoSpy, timeout(5000)).findLanguagesAsSource(any(),any())
        verify(mockView, timeout(5000)).list = any()

    }

    @Test
    fun givenPresenterCreatedInBrowseMode_whenOnClickEntryCalled_thenShouldGoToDetailView() {

        val repo: UmAppDatabase by di.activeRepoInstance()
        val testEntity = Language().apply {
            //set variables here
            name = "German"
            langUid = repo.languageDao.insert(this)
        }

        val presenterArgs = mapOf<String,String>()
        val presenter = LanguageListPresenter(context,
                presenterArgs, mockView, di, mockLifecycleOwner)
        presenter.onCreate(null)
        mockView.waitForListToBeSet()

        presenter.handleClickEntry(testEntity)

        verify(testNavController, timeout(2000)).navigate(eq(LanguageEditView.VIEW_NAME), any(), any())

    }

}
