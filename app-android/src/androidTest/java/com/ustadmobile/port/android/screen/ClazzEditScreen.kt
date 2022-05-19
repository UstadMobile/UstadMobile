package com.ustadmobile.port.android.screen

import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.fragment.findNavController
import io.github.kakaocup.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchoolAndTerminology
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.port.android.view.ClazzEditFragment
import com.ustadmobile.test.port.android.KNestedScrollView

object ClazzEditScreen : KScreen<ClazzEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_clazz_edit
    override val viewClass: Class<*>?
        get() = ClazzEditFragment::class.java

    val scrollView = KNestedScrollView { withId(R.id.activity_clazz_edit_fields_scrollview) }

    val editNameLayout = KTextInputLayout { withId(R.id.activity_clazz_edit_name)}

    val editDescTextInput = KTextInputLayout { withId(R.id.activity_clazz_edit_description)}

    val holidayCalendarTextInput = KTextInputLayout { withId(R.id.activity_clazz_edit_holiday_calendar_selected)}

    val clazzStartTextInput = KTextInputLayout { withId(R.id.activity_clazz_edit_start_date_edittext) }

    val clazzEndTextInput = KTextInputLayout { withId(R.id.activity_clazz_edit_end_date_edittext) }

    val schoolTextInputLayout = KTextInputLayout { withId(R.id.fragment_clazz_edit_school_selected) }

    fun fillFields(fragmentScenario: FragmentScenario<ClazzEditFragment>,
                   clazz: ClazzWithHolidayCalendarAndSchoolAndTerminology,
                   clazzOnForm: ClazzWithHolidayCalendarAndSchoolAndTerminology?,
                   schedules: List<Schedule> = listOf(),
                   schedulesOnForm: List<Schedule>? = null,
                   setFieldsRequiringNavigation: Boolean = true) {

        clazz.clazzName?.takeIf { it != clazzOnForm?.clazzName }?.also {
            editNameLayout{
                edit{
                    replaceText(it)
                }
            }
        }

        clazz.clazzDesc?.takeIf { it != clazzOnForm?.clazzDesc}?.also {
            editDescTextInput{
                edit{
                    replaceText(it)
                }
            }
        }

        if(!setFieldsRequiringNavigation) {
            return
        }


        schedules.filter { schedulesOnForm == null || it !in schedulesOnForm }.forEach {schedule ->
            fragmentScenario.onFragment {
                it.findNavController().currentBackStackEntry?.savedStateHandle
                        ?.set("Schedule", defaultGson().toJson(listOf(schedule)))
            }
        }

        fragmentScenario.onFragment {fragment ->
            fragment.takeIf {clazz.holidayCalendar != clazzOnForm?.holidayCalendar }
                    ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                    ?.set("HolidayCalendar", defaultGson().toJson(listOf(clazz.holidayCalendar)))
        }

    }

}