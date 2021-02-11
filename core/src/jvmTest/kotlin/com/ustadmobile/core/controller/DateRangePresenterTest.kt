
package com.ustadmobile.core.controller


import com.nhaarman.mockitokotlin2.*
import com.soywiz.klock.DateTime
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.DateRangeView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Moment
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
class DateRangePresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: DateRangeView

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
    }

    @Test
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldReturnToResult() {
        val presenterArgs = mapOf<String, String>()

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

        verify(mockView, timeout(5000)).finishWithResult(listOf(initialEntity))

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