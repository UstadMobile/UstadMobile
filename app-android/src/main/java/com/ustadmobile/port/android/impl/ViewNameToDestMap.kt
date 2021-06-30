package com.ustadmobile.port.android.impl

import androidx.annotation.Keep
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.impl.UstadDestination
import com.ustadmobile.core.view.*

@Keep
class ViewNameToDestMap: DestinationProvider {

    private val destinationMap = mapOf(
            ClazzEdit2View.VIEW_NAME to UstadDestination(R.id.clazz_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            ClazzList2View.VIEW_NAME to UstadDestination(R.id.home_clazzlist_dest),
            ClazzDetailView.VIEW_NAME to UstadDestination(R.id.clazz_detail_dest),
            HolidayCalendarListView.VIEW_NAME to UstadDestination(R.id.holidaycalendar_list_dest),
            HolidayCalendarEditView.VIEW_NAME to UstadDestination(R.id.holidaycalendar_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            SettingsView.VIEW_NAME to UstadDestination(R.id.settings_list_dest),
            PersonEditView.VIEW_NAME to UstadDestination(R.id.person_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            PersonEditView.VIEW_NAME_REGISTER to UstadDestination(R.id.person_edit_register_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideBottomNavigation = true,
                    hideAccountIcon = true),
            SchoolListView.VIEW_NAME to UstadDestination(R.id.home_schoollist_dest),
            SchoolEditView.VIEW_NAME to UstadDestination(R.id.school_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            SchoolDetailView.VIEW_NAME to UstadDestination(R.id.school_detail_dest),
            SchoolDetailOverviewView.VIEW_NAME to UstadDestination(R.id.school_detail_overview_dest),
            PersonDetailView.VIEW_NAME to UstadDestination(R.id.person_detail_dest),
            PersonListView.VIEW_NAME to UstadDestination(R.id.person_list_dest),
            SchoolMemberListView.VIEW_NAME to UstadDestination(R.id.schoolmember_list_dest),
            ClazzWorkListView.VIEW_NAME to UstadDestination(R.id.clazzwork_list_dest),
            ClazzWorkEditView.VIEW_NAME to UstadDestination(R.id.clazzwork_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            ClazzWorkQuestionAndOptionsEditView.VIEW_NAME to UstadDestination(
                    R.id.clazzworkquestionandoptions_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            ClazzWorkDetailView.VIEW_NAME to UstadDestination(R.id.clazzwork_detail_list),
            ContentEntryEdit2View.VIEW_NAME to UstadDestination(R.id.content_entry_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            ContentEntryListTabsView.VIEW_NAME to UstadDestination(R.id.home_content_dest),
            ContentEntryList2View.VIEW_NAME to UstadDestination(R.id.content_entry_list_dest),
            ContentEntryList2View.FOLDER_VIEW_NAME to UstadDestination(R.id.content_entry_list_select_folder),
            ContentEntryDetailOverviewView.VIEW_NAME to UstadDestination(R.id.content_entry_detail_overview_dest),
            ContentEntryDetailView.VIEW_NAME to UstadDestination(R.id.content_entry_detail_dest),
            ContentEntryDetailAttemptsListView.VIEW_NAME to UstadDestination(R.id.content_entry_detail_attempt_dest),
            SessionListView.VIEW_NAME to UstadDestination(R.id.content_entry_detail_session_list_dest),
            StatementListView.VIEW_NAME to UstadDestination(R.id.content_entry_detail_session_detail_list_dest),
            ClazzLogEditAttendanceView.VIEW_NAME to UstadDestination(R.id.clazz_log_edit_attendance_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            ClazzWorkDetailProgressListView.VIEW_NAME to UstadDestination(R.id.clazzwork_detail_progress_list),
            ClazzWorkSubmissionMarkingView.VIEW_NAME to UstadDestination(R.id.clazzworksubmission_marking_edit,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            ClazzEnrolmentListView.VIEW_NAME to UstadDestination(R.id.clazz_enrolment_list),
            ClazzEnrolmentEditView.VIEW_NAME to UstadDestination(R.id.clazz_enrolment_edit,
                    hideAccountIcon = true),
            LeavingReasonListView.VIEW_NAME to UstadDestination(R.id.leaving_reason_list),
            LeavingReasonEditView.VIEW_NAME to UstadDestination(R.id.leaving_reason_edit,
                    hideAccountIcon = true),
            VideoPlayerView.VIEW_NAME to UstadDestination(R.id.video_content),
            WebChunkView.VIEW_NAME to UstadDestination(R.id.webchunk_view,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideBottomNavigation = true),
            // If ActionBarScrollBehavior is collapsing/scroll, then the WebView for Xapi content
            // gets the wrong height.
            XapiPackageContentView.VIEW_NAME to UstadDestination(R.id.content_xapi_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideBottomNavigation = true),
            ReportListView.VIEW_NAME to UstadDestination(R.id.report_list_dest),
            ReportTemplateListView.VIEW_NAME to UstadDestination(R.id.report_template_list_dest),
            ReportEditView.VIEW_NAME to UstadDestination(R.id.report_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            ReportFilterEditView.VIEW_NAME to UstadDestination(R.id.report_filter_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            ReportDetailView.VIEW_NAME to UstadDestination(R.id.report_detail_dest),
            DateRangeView.VIEW_NAME to UstadDestination(R.id.date_range_dest),
            SiteEnterLinkView.VIEW_NAME to UstadDestination(R.id.site_enterlink_dest,
                hideBottomNavigation = true, hideAccountIcon = true),
            Login2View.VIEW_NAME to UstadDestination(R.id.login_dest, hideBottomNavigation = true,
                    hideAccountIcon = true),
            AccountListView.VIEW_NAME to UstadDestination(R.id.account_list_dest,
                    hideBottomNavigation = true, hideAccountIcon = true),
            PersonAccountEditView.VIEW_NAME to UstadDestination(R.id.person_account_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            InviteViaLinkView.VIEW_NAME to UstadDestination(R.id.invite_via_link_dest),
            LanguageListView.VIEW_NAME to UstadDestination(R.id.language_list_dest),
            LanguageEditView.VIEW_NAME to UstadDestination(R.id.language_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            JoinWithCodeView.VIEW_NAME to UstadDestination(R.id.join_with_code_dest),
            LearnerGroupMemberListView.VIEW_NAME to UstadDestination(R.id.learner_group_member_list_dest),
            TimeZoneListView.VIEW_NAME to UstadDestination(R.id.time_zone_list_dest),
            ClazzLogEditView.VIEW_NAME to UstadDestination(R.id.clazz_log_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            SiteDetailView.VIEW_NAME to UstadDestination(R.id.site_detail_dest),
            SiteEditView.VIEW_NAME to UstadDestination(R.id.site_edit_dest,
                actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            SiteTermsEditView.VIEW_NAME to UstadDestination(R.id.site_terms_edit_dest,
                actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideBottomNavigation = true,
                hideAccountIcon = true),
            SiteTermsDetailView.VIEW_NAME to UstadDestination(R.id.site_terms_detail_dest),
            SiteTermsDetailView.VIEW_NAME_ACCEPT_TERMS to UstadDestination(R.id.site_terms_detail_accept_dest,
                actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideBottomNavigation = true,
                hideAccountIcon = true),
            ScheduleEditView.VIEW_NAME to UstadDestination(R.id.schedule_edit_dest,
                actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            BitmaskEditView.VIEW_NAME to UstadDestination(R.id.bitmask_edit_dest,
                actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            RegisterMinorWaitForParentView.VIEW_NAME to UstadDestination(R.id.register_minor_wait_for_parent_dest,
                hideBottomNavigation = true, hideAccountIcon = true),
            RegisterAgeRedirectView.VIEW_NAME to UstadDestination(R.id.register_age_redirect_dest,
                hideBottomNavigation = true, hideAccountIcon = true),
            ParentalConsentManagementView.VIEW_NAME to UstadDestination(R.id.parental_consent_management_dest),
            ScopedGrantEditView.VIEW_NAME to UstadDestination(R.id.scoped_grant_edit_dest,
                hideAccountIcon = true),
            ErrorReportView.VIEW_NAME to UstadDestination(R.id.error_report_dest),
    )

    override val navControllerViewId: Int
        get() = R.id.activity_main_navhost_fragment

    override fun lookupDestinationName(viewName: String) = destinationMap[viewName]

    override fun lookupDestinationById(destinationId: Int) = destinationMap.values
            .firstOrNull { it.destinationId == destinationId }

    override fun lookupViewNameById(destinationId: Int) = destinationMap.entries
            .firstOrNull { it.value.destinationId == destinationId }?.key
}