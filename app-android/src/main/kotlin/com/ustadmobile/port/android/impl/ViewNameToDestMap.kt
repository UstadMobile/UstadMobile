package com.ustadmobile.port.android.impl

import androidx.annotation.Keep
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.impl.UstadDestination
import com.ustadmobile.core.view.*
import com.ustadmobile.staging.core.view.HolidayCalendarListView

@Keep
class ViewNameToDestMap: DestinationProvider {

    val destinationMap = mapOf(
            ClazzEdit2View.VIEW_NAME to UstadDestination(R.id.clazz_edit_dest, false),
            ClazzList2View.VIEW_NAME to UstadDestination(R.id.home_clazzlist_dest, true),
            HolidayCalendarListView.VIEW_NAME to UstadDestination(R.id.holidaycalendar_list_dest, true),
            HolidayCalendarEditView.VIEW_NAME to UstadDestination(R.id.holidaycalendar_edit_dest, false),
            TimeZoneEntityListView.VIEW_NAME to UstadDestination(R.id.timezoneentity_list_dest, false),
            SettingsView.VIEW_NAME to UstadDestination(R.id.settings_list_dest, false),
            SelQuestionSetListView.VIEW_NAME to UstadDestination(R.id.selquestionset_list_dest, false),
            SelQuestionSetEditView.VIEW_NAME to UstadDestination(R.id.selquestionset_edit_dest, false),
            SelQuestionAndOptionsEditView.VIEW_NAME to UstadDestination(R.id.selquestionandoptions_edit_dest, false),
            RoleListView.VIEW_NAME to UstadDestination(R.id.role_list_dest, false),
            RoleEditView.VIEW_NAME to UstadDestination(R.id.role_edit_dest, false),
            PersonEditView.VIEW_NAME to UstadDestination(R.id.person_edit_dest, false),
            SchoolListView.VIEW_NAME to UstadDestination(R.id.home_schoollist_dest, true)
    )

    override val navControllerViewId: Int
        get() = R.id.activity_main_navhost_fragment

    override fun lookupDestinationName(viewName: String) = destinationMap[viewName]

    override fun lookupDestinationById(destinationId: Int) = destinationMap.values.firstOrNull { it.destinationId == destinationId }
}