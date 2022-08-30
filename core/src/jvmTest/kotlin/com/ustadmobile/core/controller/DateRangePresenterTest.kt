
package com.ustadmobile.core.controller


import org.mockito.kotlin.*
import com.soywiz.klock.DateTime
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.util.safeParseList
import com.ustadmobile.core.view.DateRangeView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.DateRangeMoment
import com.ustadmobile.lib.db.entities.Moment
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
class DateRangePresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: DateRangeView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

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
            on { getBackStackEntry(any()) }.thenReturn(ustadBackStackEntry)
        }

        di = DI {
            import(ustadTestRule.diModule)
            bind<UstadNavController>(overrides = true) with singleton { testNavController }
        }
    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldReturnToResult() {
        val presenterArgs = mapOf<String, String>(
            UstadView.ARG_RESULT_DEST_VIEWNAME to "view",
            UstadView.ARG_RESULT_DEST_KEY to "key"
        )

        val presenter = DateRangePresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        initialEntity.fromMoment.typeFlag = Moment.TYPE_FLAG_RELATIVE
        initialEntity.fromMoment.relUnit = Moment.WEEKS_REL_UNIT
        initialEntity.fromMoment.relOffSet = -10
        initialEntity.fromMoment.relTo = Moment.TODAY_REL_TO

        initialEntity.toMoment.typeFlag = Moment.TYPE_FLAG_RELATIVE
        initialEntity.toMoment.relUnit = Moment.WEEKS_REL_UNIT
        initialEntity.toMoment.relOffSet = -2
        initialEntity.toMoment.relTo = Moment.TODAY_REL_TO

        presenter.handleClickSave(initialEntity)

        verify(savedStateHandle, timeout(2000))[any()] = argWhere<String> {
            safeParseList(di, ListSerializer(DateRangeMoment.serializer()),
                DateRangeMoment::class, it).first().fromMoment.typeFlag == Moment.TYPE_FLAG_RELATIVE
        }
    }

    @Test
    fun givenNoExistingEntityWithNoValuesSet_whenOnCreateAndHandleClickSaveCalled_thenShouldShowErrors() {

        val impl = di.direct.instance<UstadMobileSystemImpl>()

        val presenterArgs = mapOf<String, String>()
        val presenter = DateRangePresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue()!!

        presenter.handleClickSave(initialEntity)

        verify(mockView, timeout(5000)).fromFixedDateMissing = eq(impl.getString(
                MessageID.field_required_prompt, context))

    }


}