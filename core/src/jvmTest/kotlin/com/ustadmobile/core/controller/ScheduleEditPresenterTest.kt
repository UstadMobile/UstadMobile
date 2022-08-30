package com.ustadmobile.core.controller

import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.ScheduleEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.DoorState
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class ScheduleEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: ScheduleEditView

    private lateinit var mockLifecycleOwner: LifecycleOwner

    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        mockView = mock {  }

        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
    }

    @Test
    fun givenScheduleHasNoStartTime_whenClickSave_thenShouldShowError() {
        val scheduleEditPresenter = ScheduleEditPresenter(Any(), mapOf(), mockView,
                    di, mockLifecycleOwner)

        scheduleEditPresenter.onCreate(null)

        scheduleEditPresenter.handleClickSave(Schedule().apply {
            sceduleStartTime = 0L
            scheduleEndTime = systemTimeInMillis()
        })

        val expectedErrMessage = di.direct.instance<UstadMobileSystemImpl>()
                .getString(MessageID.field_required_prompt, Any())
        verify(mockView).fromTimeError = eq(expectedErrMessage)
    }

    @Test
    fun givenScheduleHasNoEndTime_whenClickSave_thenShouldShowError() {
        val scheduleEditPresenter = ScheduleEditPresenter(Any(), mapOf(), mockView,
                di, mockLifecycleOwner)

        scheduleEditPresenter.onCreate(null)

        scheduleEditPresenter.handleClickSave(Schedule().apply {
            sceduleStartTime = systemTimeInMillis()
            scheduleEndTime = 0L
        })

        val expectedErrMessage = di.direct.instance<UstadMobileSystemImpl>()
                .getString(MessageID.field_required_prompt, Any())
        verify(mockView).toTimeError = eq(expectedErrMessage)
    }

    @Test
    fun givenScheduleStartAfterEndTime_whenClickSave_thenShouldShowError() {
        val scheduleEditPresenter = ScheduleEditPresenter(Any(), mapOf(), mockView,
                di, mockLifecycleOwner)

        scheduleEditPresenter.onCreate(null)

        scheduleEditPresenter.handleClickSave(Schedule().apply {
            sceduleStartTime = systemTimeInMillis()
            scheduleEndTime = systemTimeInMillis() - 1000
        })

        val expectedErrMessage = di.direct.instance<UstadMobileSystemImpl>()
                .getString(MessageID.end_is_before_start_error, Any())
        verify(mockView).toTimeError = eq(expectedErrMessage)
    }

    @Test
    fun givenValidSchedule_whenClickSave_thenShouldFinishWithResult() {
        val navController: UstadNavController = di.direct.instance()

        val presenterArgs = mapOf(UstadView.ARG_RESULT_DEST_VIEWNAME to ClazzEdit2View.VIEW_NAME,
            UstadView.ARG_RESULT_DEST_KEY to "Schedule")

        navController.navigate(ClazzEdit2View.VIEW_NAME, mapOf())
        navController.navigate(ScheduleEditView.VIEW_NAME, presenterArgs)


        val scheduleEditPresenter = ScheduleEditPresenter(Any(), presenterArgs, mockView,
                di, mockLifecycleOwner)

        scheduleEditPresenter.onCreate(null)

        val validSchedule =Schedule().apply {
            scheduleDay = Schedule.DAY_SUNDAY
            sceduleStartTime = systemTimeInMillis()
            scheduleEndTime = systemTimeInMillis() + 1000
        }

        scheduleEditPresenter.handleClickSave(validSchedule)

        verify(navController).popBackStack(ClazzEdit2View.VIEW_NAME, false)

        val resultSavedJson : String? = navController.currentBackStackEntry?.savedStateHandle
            ?.get("Schedule")
        val resultSaved: List<Schedule> = Json.decodeFromString(
            ListSerializer(Schedule.serializer()), resultSavedJson!!)

        Assert.assertEquals("Schedule saved to JSON is equal to valid schedule set on view",
            validSchedule, resultSaved.first())
    }

}