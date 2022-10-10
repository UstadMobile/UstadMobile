
package com.ustadmobile.core.controller


import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import org.mockito.kotlin.*
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.util.safeParseList
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.SiteTermsEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.*


/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class SiteTermsEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: SiteTermsEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var testNavController: UstadNavController

    private lateinit var ustadBackStackEntry: UstadBackStackEntry

    private lateinit var savedStateHandle: UstadSavedStateHandle

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)

        savedStateHandle = mock{}
        ustadBackStackEntry = mock{
            on{savedStateHandle}.thenReturn(savedStateHandle)
        }

        testNavController = mock{
            on { getBackStackEntry(any()) }.thenReturn(ustadBackStackEntry)
            on { currentBackStackEntry }.thenReturn(ustadBackStackEntry)
        }
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
            bind<UstadNavController>(overrides = true) with singleton { testNavController }

        }

        //TODO: insert any entities required for all tests
    }


    @Test
    fun givenExistingWorkspaceTerms_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val testEntity = SiteTermsWithLanguage().apply {
            termsHtml = "All your base are belong to us"
            stLanguage = Language().apply {
                name = "English"
                iso_639_1_standard = "en"
            }
        }

        val testEntitySerialized = safeStringify(di, SiteTermsWithLanguage.serializer(), testEntity)

        val presenterArgs = mapOf(
            UstadEditView.ARG_ENTITY_JSON to testEntitySerialized,
            UstadView.ARG_RESULT_DEST_VIEWNAME to "view",
            UstadView.ARG_RESULT_DEST_KEY to "key"
        )

        testNavController.navigate(SiteTermsEditView.VIEW_NAME, presenterArgs)
        val presenter = SiteTermsEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity!!.termsHtml = "<h1>All your base are belong to us</h1>"

        presenter.handleClickSave(initialEntity)

        verify(savedStateHandle, timeout(2000))[any()] = argWhere<String> {
            safeParseList(di, ListSerializer(SiteTermsWithLanguage.serializer()),
                SiteTermsWithLanguage::class, it).first().termsHtml == "<h1>All your base are belong to us</h1>"
        }
    }


}