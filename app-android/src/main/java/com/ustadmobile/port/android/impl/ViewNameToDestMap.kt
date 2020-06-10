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
            ClazzDetailView.VIEW_NAME to UstadDestination(R.id.clazz_detail_dest, false),
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
            SchoolListView.VIEW_NAME to UstadDestination(R.id.home_schoollist_dest, true),
            SchoolEditView.VIEW_NAME to UstadDestination(R.id.school_edit_dest, false),
            SchoolDetailView.VIEW_NAME to UstadDestination(R.id.school_detail_dest, true),
            SchoolDetailOverviewView.VIEW_NAME to UstadDestination(R.id.school_detail_overview_dest, true),
            PersonDetailView.VIEW_NAME to UstadDestination(R.id.person_detail_dest, true),
            PersonListView.VIEW_NAME to UstadDestination(R.id.person_list_dest, false),
            SchoolMemberListView.VIEW_NAME to UstadDestination(R.id.schoolmember_list_dest, true),
            ClazzWorkListView.VIEW_NAME to UstadDestination(R.id.clazzwork_list_dest, true),
            ClazzWorkEditView.VIEW_NAME to UstadDestination(R.id.clazzwork_edit_dest, false),
            ClazzWorkQuestionAndOptionsEditView.VIEW_NAME to UstadDestination(R.id.clazzworkquestionandoptions_edit_dest, false),
            ClazzWorkDetailView.VIEW_NAME to UstadDestination(R.id.clazzwork_detail_list, true),
            ContentEntryEdit2View.VIEW_NAME to UstadDestination(R.id.content_entry_edit_dest, false),
            ContentEntryListTabsView.VIEW_NAME to UstadDestination(R.id.home_content_dest, true),
            ContentEntryList2View.VIEW_NAME to UstadDestination(R.id.content_entry_list_dest, true),
            ContentEntry2DetailView.VIEW_NAME to UstadDestination(R.id.content_entry_details_dest, true),
            ClazzLogEditAttendanceView.VIEW_NAME to UstadDestination(R.id.clazz_log_edit_attendance_dest, false)
    )

    override val navControllerViewId: Int
        get() = R.id.activity_main_navhost_fragment

    override fun lookupDestinationName(viewName: String) = destinationMap[viewName]

    override fun lookupDestinationById(destinationId: Int) = destinationMap.values.firstOrNull { it.destinationId == destinationId }
}