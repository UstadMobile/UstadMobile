
package com.ustadmobile.core.controller


import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.WorkspaceTermsEditView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.SiteTerms
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI


/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class WorkspaceTermsEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: WorkspaceTermsEditView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

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

        //TODO: insert any entities required for all tests
    }


    @Test
    fun givenExistingWorkspaceTerms_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        val testEntity = SiteTerms().apply {
            termsHtml = "All your base are belong to us"

        }

        val testEntitySerialized = safeStringify(di, SiteTerms.serializer(), testEntity)

        val presenterArgs = mapOf(UstadEditView.ARG_ENTITY_JSON to testEntitySerialized)

        val presenter = WorkspaceTermsEditPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        initialEntity!!.termsHtml = "<h1>All your base are belong to us</h1>"

        presenter.handleClickSave(initialEntity)

        verify(mockView, timeout(2000)).finishWithResult(argWhere {
            it.first().termsHtml == "<h1>All your base are belong to us</h1>"
        })
    }


}