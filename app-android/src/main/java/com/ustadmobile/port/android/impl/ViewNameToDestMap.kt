package com.ustadmobile.port.android.impl

import androidx.annotation.Keep
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.impl.UstadDestination
import com.ustadmobile.core.view.*

@Keep
class ViewNameToDestMap: DestinationProvider {

    val destinationMap = mapOf(
            ClazzEdit2View.VIEW_NAME to UstadDestination(R.id.clazz_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            ClazzList2View.VIEW_NAME to UstadDestination(R.id.home_clazzlist_dest),
            ClazzDetailView.VIEW_NAME to UstadDestination(R.id.clazz_detail_dest),
            HolidayCalendarListView.VIEW_NAME to UstadDestination(R.id.holidaycalendar_list_dest),
            HolidayCalendarEditView.VIEW_NAME to UstadDestination(R.id.holidaycalendar_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            SettingsView.VIEW_NAME to UstadDestination(R.id.settings_list_dest),
            RoleListView.VIEW_NAME to UstadDestination(R.id.role_list_dest),
            RoleEditView.VIEW_NAME to UstadDestination(R.id.role_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            PersonEditView.VIEW_NAME to UstadDestination(R.id.person_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            PersonEditView.VIEW_NAME_REGISTER to UstadDestination(R.id.person_edit_register_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideBottomNavigation = true),
            SchoolListView.VIEW_NAME to UstadDestination(R.id.home_schoollist_dest),
            SchoolEditView.VIEW_NAME to UstadDestination(R.id.school_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            SchoolDetailView.VIEW_NAME to UstadDestination(R.id.school_detail_dest),
            SchoolDetailOverviewView.VIEW_NAME to UstadDestination(R.id.school_detail_overview_dest),
            PersonDetailView.VIEW_NAME to UstadDestination(R.id.person_detail_dest),
            PersonListView.VIEW_NAME to UstadDestination(R.id.person_list_dest),
            SchoolMemberListView.VIEW_NAME to UstadDestination(R.id.schoolmember_list_dest),
            ClazzWorkListView.VIEW_NAME to UstadDestination(R.id.clazzwork_list_dest),
            ClazzWorkEditView.VIEW_NAME to UstadDestination(R.id.clazzwork_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            ClazzWorkQuestionAndOptionsEditView.VIEW_NAME to UstadDestination(
                    R.id.clazzworkquestionandoptions_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            ClazzWorkDetailView.VIEW_NAME to UstadDestination(R.id.clazzwork_detail_list),
            ContentEntryEdit2View.VIEW_NAME to UstadDestination(R.id.content_entry_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            ContentEntryListTabsView.VIEW_NAME to UstadDestination(R.id.home_content_dest),
            ContentEntryList2View.VIEW_NAME to UstadDestination(R.id.content_entry_list_dest),
            ContentEntry2DetailView.VIEW_NAME to UstadDestination(R.id.content_entry_details_dest),
            ClazzLogEditAttendanceView.VIEW_NAME to UstadDestination(R.id.clazz_log_edit_attendance_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            ClazzWorkDetailProgressListView.VIEW_NAME to UstadDestination(R.id.clazzwork_detail_progress_list),
            ClazzWorkSubmissionMarkingView.VIEW_NAME to UstadDestination(R.id.clazzworksubmission_marking_edit,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            VideoPlayerView.VIEW_NAME to UstadDestination(R.id.video_content),
            WebChunkView.VIEW_NAME to UstadDestination(R.id.webchunk_view),
            XapiPackageContentView.VIEW_NAME to UstadDestination(R.id.content_xapi_dest,
                hideBottomNavigation = true),
            ReportListView.VIEW_NAME to UstadDestination(R.id.report_list_dest),
            ReportTemplateView.VIEW_NAME to UstadDestination(R.id.report_template_list_dest),
            ReportEditView.VIEW_NAME to UstadDestination(R.id.report_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            ReportFilterEditView.VIEW_NAME to UstadDestination(R.id.report_filter_edit_dest),
            ReportDetailView.VIEW_NAME to UstadDestination(R.id.report_detail_dest),
            DateRangeView.VIEW_NAME to UstadDestination(R.id.date_range_dest),
            SiteEnterLinkView.VIEW_NAME to UstadDestination(R.id.site_enterlink_dest,
                hideBottomNavigation = true),
            Login2View.VIEW_NAME to UstadDestination(R.id.login_dest, hideBottomNavigation = true),
            AccountListView.VIEW_NAME to UstadDestination(R.id.account_list_dest),
            PersonAccountEditView.VIEW_NAME to UstadDestination(R.id.person_account_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            InviteViaLinkView.VIEW_NAME to UstadDestination(R.id.invite_via_link_dest),
            EntityRoleEditView.VIEW_NAME to UstadDestination(R.id.entityrole_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            JoinWithCodeView.VIEW_NAME to UstadDestination(R.id.join_with_code_dest),
            LearnerGroupMemberListView.VIEW_NAME to UstadDestination(R.id.learner_group_member_list_dest),
            TimeZoneListView.VIEW_NAME to UstadDestination(R.id.time_zone_list_dest),
            ClazzLogEditView.VIEW_NAME to UstadDestination(R.id.clazz_log_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            SiteDetailView.VIEW_NAME to UstadDestination(R.id.site_detail_dest),
            SiteEditView.VIEW_NAME to UstadDestination(R.id.site_edit_dest,
                actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            SiteTermsEditView.VIEW_NAME to UstadDestination(R.id.site_terms_edit_dest,
                actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideBottomNavigation = true),
            SiteTermsDetailView.VIEW_NAME to UstadDestination(R.id.site_terms_detail_dest),
            SiteTermsDetailView.VIEW_NAME_ACCEPT_TERMS to UstadDestination(R.id.site_terms_detail_accept_dest,
                actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideBottomNavigation = true)
    )

    override val navControllerViewId: Int
        get() = R.id.activity_main_navhost_fragment

    override fun lookupDestinationName(viewName: String) = destinationMap[viewName]

    override fun lookupDestinationById(destinationId: Int) = destinationMap.values.firstOrNull { it.destinationId == destinationId }
}