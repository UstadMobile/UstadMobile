package com.ustadmobile.port.android.impl

import androidx.annotation.Keep
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.impl.UstadDestination
import com.ustadmobile.core.view.*
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditViewModel
import com.ustadmobile.core.viewmodel.HtmlEditViewModel
import com.ustadmobile.core.viewmodel.timezone.TimeZoneListViewModel
import com.ustadmobile.core.viewmodel.QRCodeScannerViewModel
import com.ustadmobile.core.viewmodel.TimeZoneListViewModel
import com.ustadmobile.port.android.view.PanicButtonSettingsView
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailViewModel
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListViewModel
import com.ustadmobile.core.viewmodel.clazzlog.edit.ClazzLogEditViewModel
import com.ustadmobile.core.viewmodel.clazzlog.editattendance.ClazzLogEditAttendanceViewModel
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.edit.CourseGroupSetEditViewModel
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailViewModel
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailViewModel
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditViewModel

@Keep
class ViewNameToDestMap: DestinationProvider {

    private val destinationMap = mapOf(
            ClazzEdit2View.VIEW_NAME to UstadDestination(R.id.clazz_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                    hideBottomNavigation = true),
            ClazzList2View.VIEW_NAME_HOME to UstadDestination(R.id.home_clazzlist_dest),
            ClazzList2View.VIEW_NAME to UstadDestination(R.id.clazz_list_dest),
            ClazzDetailViewModel.DEST_NAME to UstadDestination(R.id.clazz_detail_dest),
            HolidayEditView.VIEW_NAME to UstadDestination(R.id.holiday_edit_dest,
                    hideBottomNavigation = true),
            HolidayCalendarListView.VIEW_NAME to UstadDestination(R.id.holidaycalendar_list_dest),
            HolidayCalendarEditView.VIEW_NAME to UstadDestination(R.id.holidaycalendar_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                    hideBottomNavigation = true),
            SettingsView.VIEW_NAME to UstadDestination(R.id.settings_list_dest),
            PersonEditView.VIEW_NAME to UstadDestination(R.id.person_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                    hideBottomNavigation = true),
            PersonEditView.VIEW_NAME_REGISTER to UstadDestination(R.id.person_edit_register_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideBottomNavigation = true,
                    hideAccountIcon = true),
            SchoolListView.VIEW_NAME to UstadDestination(R.id.home_schoollist_dest),
            SchoolEditView.VIEW_NAME to UstadDestination(R.id.school_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                    hideBottomNavigation = true),
            SchoolDetailView.VIEW_NAME to UstadDestination(R.id.school_detail_dest),
            SchoolDetailOverviewView.VIEW_NAME to UstadDestination(R.id.school_detail_overview_dest),
            PersonDetailView.VIEW_NAME to UstadDestination(R.id.person_detail_dest),
            PersonListView.VIEW_NAME to UstadDestination(R.id.person_list_dest),
            PersonListView.VIEW_NAME_HOME to UstadDestination(R.id.home_personlist_dest),
            SchoolMemberListView.VIEW_NAME to UstadDestination(R.id.schoolmember_list_dest),
            ClazzAssignmentEditView.VIEW_NAME to UstadDestination(R.id.clazz_assignment_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                    hideBottomNavigation = true),
            ModuleCourseBlockEditView.VIEW_NAME to UstadDestination(R.id.module_course_block_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                    hideBottomNavigation = true),
            CourseDiscussionDetailViewModel.DEST_NAME to UstadDestination(R.id.course_discussion_detail_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            DiscussionPostEditViewModel.DEST_NAME to UstadDestination(R.id.discussion_post_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                    hideBottomNavigation = true),
            DiscussionPostDetailViewModel.DEST_NAME to UstadDestination(R.id.discussion_post_detail_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            CourseTerminologyListView.VIEW_NAME to UstadDestination(R.id.course_terminology_list_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            CourseTerminologyEditView.VIEW_NAME to UstadDestination(R.id.course_terminology_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                    hideBottomNavigation = true),
            PeerReviewerAllocationEditView.VIEW_NAME to UstadDestination(R.id.assignment_peer_allocation_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            CourseGroupSetListViewModel.DEST_NAME to UstadDestination(R.id.course_group_set_list,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            CourseGroupSetEditViewModel.DEST_NAME to UstadDestination(R.id.course_group_set_edit,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                    hideBottomNavigation = true),
            CourseGroupSetDetailViewModel.DEST_NAME to UstadDestination(R.id.course_group_set_detail,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            ClazzAssignmentDetailView.VIEW_NAME to UstadDestination(R.id.clazz_assignment_detail_dest),
            ClazzAssignmentSubmitterDetailViewModel.DEST_NAME to
                UstadDestination(R.id.clazz_assignment_submitter_detail_dest),
            ContentEntryEditViewModel.DEST_NAME to UstadDestination(R.id.content_entry_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                    hideBottomNavigation = true),
            ContentEntryList2View.VIEW_NAME to UstadDestination(R.id.content_entry_list_dest),
            ContentEntryList2View.VIEW_NAME_HOME to UstadDestination(R.id.content_entry_list_home_dest),
            ContentEntryList2View.FOLDER_VIEW_NAME to UstadDestination(R.id.content_entry_list_select_folder),
            ContentEntryDetailOverviewView.VIEW_NAME to UstadDestination(R.id.content_entry_detail_overview_dest),
            ContentEntryDetailView.VIEW_NAME to UstadDestination(R.id.content_entry_detail_dest),
            ContentEntryDetailAttemptsListView.VIEW_NAME to UstadDestination(R.id.content_entry_detail_attempt_dest),
            SessionListView.VIEW_NAME to UstadDestination(R.id.content_entry_detail_session_list_dest),
            StatementListView.VIEW_NAME to UstadDestination(R.id.content_entry_detail_session_detail_list_dest),
            ClazzLogEditAttendanceViewModel.DEST_NAME to UstadDestination(R.id.clazz_log_edit_attendance_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true),
            ClazzEnrolmentListViewModel.DEST_NAME to UstadDestination(R.id.clazz_enrolment_list),
            ClazzEnrolmentEditView.VIEW_NAME to UstadDestination(R.id.clazz_enrolment_edit,
                    hideAccountIcon = true, hideBottomNavigation = true),
            LeavingReasonListView.VIEW_NAME to UstadDestination(R.id.leaving_reason_list),
            LeavingReasonEditView.VIEW_NAME to UstadDestination(R.id.leaving_reason_edit,
                    hideAccountIcon = true, hideBottomNavigation = true),
            SelectFileView.VIEW_NAME to UstadDestination(R.id.select_file_view),
            SelectExtractFileView.VIEW_NAME to UstadDestination(R.id.select_extract_file_view),
            SelectFolderView.VIEW_NAME to UstadDestination(R.id.select_folder_view),
            ContentEntryImportLinkView.VIEW_NAME to UstadDestination(R.id.import_link_view),
            VideoContentView.VIEW_NAME to UstadDestination(R.id.video_content),
            PDFContentView.VIEW_NAME to UstadDestination(R.id.pdf_content,
                    hideBottomNavigation = true, actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            WebChunkView.VIEW_NAME to UstadDestination(R.id.webchunk_view,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideBottomNavigation = true),
            // If ActionBarScrollBehavior is collapsing/scroll, then the WebView for Xapi content
            // gets the wrong height.
            XapiPackageContentView.VIEW_NAME to UstadDestination(R.id.content_xapi_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideBottomNavigation = true),
            ReportListView.VIEW_NAME to UstadDestination(R.id.report_list_dest),
            ReportTemplateListView.VIEW_NAME to UstadDestination(R.id.report_template_list_dest),
            ReportEditView.VIEW_NAME to UstadDestination(R.id.report_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                    hideBottomNavigation = true),
            ReportFilterEditView.VIEW_NAME to UstadDestination(R.id.report_filter_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                    hideBottomNavigation = true),
            ReportDetailView.VIEW_NAME to UstadDestination(R.id.report_detail_dest),
            DateRangeView.VIEW_NAME to UstadDestination(R.id.date_range_dest),
            SiteEnterLinkView.VIEW_NAME to UstadDestination(R.id.site_enterlink_dest,
                hideBottomNavigation = true, hideAccountIcon = true),
            Login2View.VIEW_NAME to UstadDestination(R.id.login_dest, hideBottomNavigation = true,
                    hideAccountIcon = true),
            AccountListView.VIEW_NAME to UstadDestination(R.id.account_list_dest,
                    hideBottomNavigation = true, hideAccountIcon = true),
            PersonAccountEditView.VIEW_NAME to UstadDestination(R.id.person_account_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                    hideBottomNavigation = true),
            InviteViaLinkView.VIEW_NAME to UstadDestination(R.id.invite_via_link_dest),
            LanguageListView.VIEW_NAME to UstadDestination(R.id.language_list_dest),
            LanguageEditView.VIEW_NAME to UstadDestination(R.id.language_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                    hideBottomNavigation = true),
            JoinWithCodeView.VIEW_NAME to UstadDestination(R.id.join_with_code_dest),
            LearnerGroupMemberListView.VIEW_NAME to UstadDestination(R.id.learner_group_member_list_dest),
            TimeZoneListView.VIEW_NAME to UstadDestination(R.id.time_zone_list_dest),
            ClazzLogEditViewModel.DEST_NAME to UstadDestination(R.id.clazz_log_edit_dest),
            SiteDetailView.VIEW_NAME to UstadDestination(R.id.site_detail_dest),
            SiteEditView.VIEW_NAME to UstadDestination(R.id.site_edit_dest,
                actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                    hideBottomNavigation = true),
            SiteTermsEditView.VIEW_NAME to UstadDestination(R.id.site_terms_edit_dest,
                actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideBottomNavigation = true,
                hideAccountIcon = true),
            HtmlTextViewDetailView.VIEW_NAME to UstadDestination(R.id.text_html_view_detail_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideBottomNavigation = true,
                    hideAccountIcon = true),
            SiteTermsDetailView.VIEW_NAME to UstadDestination(R.id.site_terms_detail_dest),
            SiteTermsDetailView.VIEW_NAME_ACCEPT_TERMS to UstadDestination(R.id.site_terms_detail_accept_dest,
                actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideBottomNavigation = true,
                hideAccountIcon = true),
            ScheduleEditView.VIEW_NAME to UstadDestination(R.id.schedule_edit_dest,
                actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                hideBottomNavigation = true),
            BitmaskEditView.VIEW_NAME to UstadDestination(R.id.bitmask_edit_dest,
                actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL, hideAccountIcon = true,
                hideBottomNavigation = true),
            RegisterMinorWaitForParentView.VIEW_NAME to UstadDestination(R.id.register_minor_wait_for_parent_dest,
                hideBottomNavigation = true, hideAccountIcon = true),
            RegisterAgeRedirectView.VIEW_NAME to UstadDestination(R.id.register_age_redirect_dest,
                hideBottomNavigation = true, hideAccountIcon = true),
            ParentalConsentManagementView.VIEW_NAME to UstadDestination(R.id.parental_consent_management_dest),
            ScopedGrantEditView.VIEW_NAME to UstadDestination(R.id.scoped_grant_edit_dest,
                hideAccountIcon = true, actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL,
                hideBottomNavigation = true),
            ErrorReportView.VIEW_NAME to UstadDestination(R.id.error_report_dest),
            RedirectView.VIEW_NAME to UstadDestination(R.id.redirect_dest),
            ChatListView.VIEW_NAME to UstadDestination(R.id.chat_list_home_dest),
            ChatDetailView.VIEW_NAME to UstadDestination(R.id.chat_detail_dest,
                actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL,
                hideAccountIcon = true,
                hideBottomNavigation = true),
            ScopedGrantListView.VIEW_NAME to UstadDestination(R.id.scoped_grant_list_dest),
            ScopedGrantDetailView.VIEW_NAME to UstadDestination(R.id.scoped_grant_detail_dest),
            PanicButtonSettingsView.VIEW_NAME to UstadDestination(R.id.panic_button_settings_dest),
            GrantAppPermissionView.VIEW_NAME to UstadDestination(R.id.grant_app_permission_dest),
            CourseBlockEditViewModel.DEST_NAME to UstadDestination(R.id.course_block_edit_dest,
                actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            HtmlEditViewModel.DEST_NAME to UstadDestination(R.id.html_edit_dest,
                    actionBarScrollBehavior = SCROLL_FLAG_NO_SCROLL),
            TimeZoneListViewModel.DEST_NAME to UstadDestination(R.id.time_zone_list_dest),
            QRCodeScannerViewModel.DEST_NAME to UstadDestination(R.id.qr_code_scan_dest)
    )

    override val navControllerViewId: Int
        get() = R.id.activity_main_navhost_fragment

    override fun lookupDestinationName(viewName: String) = destinationMap[viewName]

    override fun lookupDestinationById(destinationId: Int) = destinationMap.values
            .firstOrNull { it.destinationId == destinationId }

    override fun lookupViewNameById(destinationId: Int) = destinationMap.entries
            .firstOrNull { it.value.destinationId == destinationId }?.key
}