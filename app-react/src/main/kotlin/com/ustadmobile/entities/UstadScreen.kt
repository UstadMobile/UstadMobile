package com.ustadmobile.entities

import com.ustadmobile.core.components.NAVHOST_CLEARSTACK_VIEWNAME
import com.ustadmobile.core.components.NavHostClearStackPlaceholder
import com.ustadmobile.core.view.*
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditViewModel
import com.ustadmobile.mui.components.*
import com.ustadmobile.view.*
import com.ustadmobile.view.clazz.edit.ClazzEditScreen
import com.ustadmobile.view.components.UstadImageSelectButtonPreview
import com.ustadmobile.view.components.UstadMessageIdSelectFieldPreview
import com.ustadmobile.view.components.UstadSelectFieldPreview
import com.ustadmobile.mui.components.DateTimeEditFieldPreview
import com.ustadmobile.mui.components.UstadNumberTextFieldPreview
import com.ustadmobile.view.components.virtuallist.VirtualListPreview
import com.ustadmobile.wrappers.reacteasysort.EasySortPreview
import react.Props
import react.FC
import com.ustadmobile.wrappers.quill.QuillDemo
import com.ustadmobile.view.timezone.TimeZoneListScreen
import com.ustadmobile.core.viewmodel.timezone.TimeZoneListViewModel
import com.ustadmobile.core.viewmodel.courseterminology.list.CourseTerminologyListViewModel
import com.ustadmobile.view.clazzassignment.edit.ClazzAssignmentEditScreen
import com.ustadmobile.view.courseterminology.list.CourseTerminologyListScreen
import com.ustadmobile.view.clazz.detail.ClazzDetailScreen
import com.ustadmobile.view.clazz.list.ClazzListScreen
import com.ustadmobile.view.clazz.detailoverview.ClazzDetailOverviewScreen
import com.ustadmobile.view.person.detail.PersonDetailPreview
import com.ustadmobile.view.person.detail.PersonDetailScreen
import com.ustadmobile.view.person.edit.PersonEditScreen
import com.ustadmobile.view.person.edit.PersonEditScreenPreview
import com.ustadmobile.view.person.list.PersonListScreen
import com.ustadmobile.view.scopedgrant.list.ScopedGrantListScreen
import com.ustadmobile.view.scopedgrant.list.ScopedGrantListScreenPreview
import com.ustadmobile.view.person.list.PersonListScreenPreview
import com.ustadmobile.view.clazzassignment.detail.ClazzAssignmentDetailScreen
import com.ustadmobile.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewScreenPreview
import com.ustadmobile.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewScreen
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewViewModel
import com.ustadmobile.view.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabScreen
import com.ustadmobile.view.clazzenrolment.clazzmemberlist.ClazzMemberListScreen
import com.ustadmobile.view.courseterminology.edit.CourseTerminologyEditScreen
import com.ustadmobile.view.login.LoginScreen
import com.ustadmobile.view.schedule.edit.ScheduleEditScreen
import com.ustadmobile.view.siteenterlink.SiteEnterLinkScreen
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListViewModel
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.view.clazzenrolment.edit.ClazzEnrolmentEditScreen
import com.ustadmobile.core.viewmodel.clazzenrolment.edit.ClazzEnrolmentEditViewModel
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListViewModel
import com.ustadmobile.view.clazzenrolment.list.ClazzEnrolmentListScreen
import com.ustadmobile.view.clazzlog.attendancelist.ClazzLogListAttendanceScreen
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceViewModel
import com.ustadmobile.core.viewmodel.clazzlog.edit.ClazzLogEditViewModel
import com.ustadmobile.core.viewmodel.clazzlog.editattendance.ClazzLogEditAttendanceViewModel
import com.ustadmobile.view.clazzlog.edit.ClazzLogEditScreen
import com.ustadmobile.view.clazzlog.editattendance.ClazzLogEditAttendanceScreen
import com.ustadmobile.view.contententry.list.ContentEntryListScreen
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.view.contententry.UstadContentEntryListItemPreview
import com.ustadmobile.view.contententry.edit.ContentEntryEditScreen
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListViewModel
import com.ustadmobile.view.coursegroupset.list.CourseGroupSetListScreen
import com.ustadmobile.view.coursegroupset.edit.CourseGroupSetEditScreen
import com.ustadmobile.core.viewmodel.coursegroupset.edit.CourseGroupSetEditViewModel
import com.ustadmobile.view.coursegroupset.detail.CourseGroupSetDetailScreen
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabViewModel
import com.ustadmobile.view.discussionpost.coursediscussiondetail.CourseDiscussionDetailScreen
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailViewModel
import com.ustadmobile.view.discussionpost.detail.DiscussionPostDetailPreview
import com.ustadmobile.view.discussionpost.detail.DiscussionPostDetailScreen
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailViewModel
import com.ustadmobile.view.discussionpost.edit.DiscussionPostEditPreview
import com.ustadmobile.view.discussionpost.edit.DiscussionPostEditScreen
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditViewModel
import com.ustadmobile.view.clazzassignment.UstadCommentListItemPreview
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailViewModel
import com.ustadmobile.view.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailScreenPreview
import com.ustadmobile.view.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailScreen
import com.ustadmobile.view.scopedgrant.detail.ScopedGrantDetailScreen
import com.ustadmobile.view.scopedgrant.detail.ScopedGrantDetailScreenPreview
import com.ustadmobile.view.scopedgrant.edit.ScopedGrantEditScreen
import com.ustadmobile.view.scopedgrant.edit.ScopedGrantEditScreenPreview

//As per entities/Showcases.kt on MUI-showcase #d71c6d1

data class UstadScreen(
    val key: String,
    val name: String,
    val component: FC<Props>,
)

var USTAD_SCREENS: Iterable<UstadScreen> = setOf(
    UstadScreen(PersonDetailView.VIEW_NAME, "Person Detail", PersonDetailScreen),
    UstadScreen("PersonDetailPreview", "Person Detail Preview",
        PersonDetailPreview
    ),
    UstadScreen("UstadEditFields", "Edit Fields", UstadEditFieldPreviews),
    UstadScreen("UstadDetailFields", "Detail Fields", UstadDetailFieldPreview),
    UstadScreen("UstadCourseBlockEdit", "UstadCourseBlockEdit", UstadCourseBlockEditPreview),
    UstadScreen("PersonEditPreview", "Person Edit Preview",
        PersonEditScreenPreview
    ),
    UstadScreen(PersonEditView.VIEW_NAME, "PersonEdit", PersonEditScreen),
    UstadScreen(PersonAccountEditView.VIEW_NAME, "Person Account Edit Preview",
        PersonAccountEditPreview),
    UstadScreen(Login2View.VIEW_NAME, "Login Preview",
        LoginScreen
    ),
    UstadScreen(SiteEnterLinkView.VIEW_NAME, "Site Enter Link Preview",
        SiteEnterLinkScreen
    ),
    UstadScreen(ParentalConsentManagementView.VIEW_NAME, "Parental Consent Management Preview",
        ParentalConsentManagementPreview),
    UstadScreen(SettingsView.VIEW_NAME, "Settings Preview",
        SettingsPreview),
    UstadScreen(InviteViaLinkView.VIEW_NAME, "Invite Via Link Preview",
        InviteViaLinkPreview),
    UstadScreen(ClazzEnrolmentEditViewModel.DEST_NAME, "ClazzEnrolmentEdit",
        ClazzEnrolmentEditScreen
    ),
    UstadScreen(ClazzEnrolmentListViewModel.DEST_NAME, "ClazzEnrolmentsList",
        ClazzEnrolmentListScreen),
    UstadScreen(SiteTermsDetailView.VIEW_NAME, "SiteTermsDetail Preview",
        SiteTermsDetailScreenPreview),
    UstadScreen(RegisterMinorWaitForParentView.VIEW_NAME, "RegisterMinorWaitForParent Preview",
        RegisterMinorWaitForParentPreview),
    UstadScreen(ScheduleEditView.VIEW_NAME, "ScheduleEdit",
        ScheduleEditScreen
    ),
    UstadScreen(ContentEntryEditViewModel.DEST_NAME, "ContentEntryEdit Preview",
        ContentEntryEditScreen
    ),
    UstadScreen(SiteDetailView.VIEW_NAME, name = "Site Detail Preview", SiteDetailPreview),
    UstadScreen(SiteEditView.VIEW_NAME, name = "Site Edit Preview", SiteEditPreview),
    UstadScreen(SchoolDetailOverviewView.VIEW_NAME, "SchoolDetailOverview Preview",
        SchoolDetailOverviewScreenPreview),
    UstadScreen(SchoolEditView.VIEW_NAME, "School Edit Preview",
        SchoolEditScreenPreview),
    UstadScreen(LanguageDetailView.VIEW_NAME, name = "LanguageDetail Preview",
        LanguageDetailPreview),
    UstadScreen(ClazzEdit2View.VIEW_NAME, "Course Edit",
        ClazzEditScreen
    ),
    UstadScreen("EasySort", "Easy Sort", EasySortPreview),
    UstadScreen(ErrorReportView.VIEW_NAME, name = "ErrorReport Preview", ErrorReportPreview),
    UstadScreen(LanguageEditView.VIEW_NAME, "LanguageEdit Preview", LanguageEditPreview),
    UstadScreen(ReportFilterEditView.VIEW_NAME,
        "ReportFilterEdit Preview", ReportFilterEditScreenPreview),

    UstadScreen(ContentEntryImportLinkView.VIEW_NAME, "ContentEntryImportLink Preview",
        ContentEntryImportLinkScreenPreview),
    UstadScreen(HolidayCalendarDetailView.VIEW_NAME, "HolidayCalendarDetail Preview",
        HolidayCalendarDetailPreview),
    UstadScreen(ContentEntryDetailOverviewView.VIEW_NAME,
        "ContentEntryDetailOverview Preview",
        ContentEntryDetailOverviewScreenPreview),
    UstadScreen(HolidayCalendarEditView.VIEW_NAME, "HolidayCalendarEdit Preview",
        HolidayCalendarEditPreview),
    UstadScreen("ScopedGrantEditPreview",
        "ScopedGrantEdit Preview", ScopedGrantEditScreenPreview
    ),
    UstadScreen(CourseTerminologyEditView.VIEW_NAME,
        "CourseTerminologyEdit Preview", CourseTerminologyEditScreen
    ),
    UstadScreen("UstadListFilterChipsHeader",
        "UstadListFilterChipsHeaderPreview Preview", UstadListFilterChipsHeaderPreview),
    UstadScreen("UstadListSortHeader",
        "UstadListSortHeader", UstadListSortHeaderPreview),
    UstadScreen(
        ClazzLogEditViewModel.DEST_NAME, "ClazzLogEdit Preview",
        ClazzLogEditScreen
    ),
    UstadScreen(CourseGroupSetEditViewModel.DEST_NAME, "CourseGroupSetEdit Preview",
        CourseGroupSetEditScreen),
    UstadScreen("UstadContentEntryListItem", "UstadContentEntryListItem Preview",
        UstadContentEntryListItemPreview
    ),
    UstadScreen(CourseGroupSetDetailViewModel.DEST_NAME, "CourseGroupSetDetail Preview",
        CourseGroupSetDetailScreen
    ),
    UstadScreen("UstadClazzAssignmentListItem", "UstadClazzAssignmentListItem Preview",
        UstadClazzAssignmentListItemPreview),
    UstadScreen(HolidayCalendarListView.VIEW_NAME, "HolidayCalendarList Preview",
        HolidayCalendarListScreenPreview),
    UstadScreen(LanguageListView.VIEW_NAME, "LanguageList Preview",
        LanguageListScreenPreview),
    UstadScreen("PersonListPreview", "PersonList Preview", PersonListScreenPreview),
    UstadScreen(PersonListView.VIEW_NAME, "PersonList", PersonListScreen),
    UstadScreen(NAVHOST_CLEARSTACK_VIEWNAME, "Clear Stack", NavHostClearStackPlaceholder),
    UstadScreen("UstadAddListItem", "UstadAddListItem Preview",
        UstadAddListItemPreview),
    UstadScreen(
        ClazzLogEditAttendanceViewModel.DEST_NAME, name = "ClazzLogEditAttendance Preview",
        ClazzLogEditAttendanceScreen
    ),
    UstadScreen(JoinWithCodeView.VIEW_NAME, "JoinWithCode Preview",
        JoinWithCodeScreenPreview),
    UstadScreen(ClazzLogListAttendanceViewModel.DEST_NAME, "ClazzLogListAttendance Preview",
        ClazzLogListAttendanceScreen
    ),
    UstadScreen(ContentEntryListViewModel.DEST_NAME, "ContentEntryList Preview",
        ContentEntryListScreen
    ),
    UstadScreen(AccountListView.VIEW_NAME, "AccountList Preview", AccountListScreenPreview),
    UstadScreen(
        ClazzMemberListViewModel.DEST_NAME, "ClazzMemberList Preview", ClazzMemberListScreen
    ),
    UstadScreen(ClazzList2View.VIEW_NAME, "Clazz List",ClazzListScreen),
    UstadScreen(ClazzList2View.VIEW_NAME_HOME, "ClazzListHome", ClazzListScreen),
    UstadScreen("VirtualListPreview", "Virtual List Preview",
        VirtualListPreview),
    UstadScreen("UstadAssignmentFileSubmissionHeader",
        "UstadAssignmentFileSubmissionHeader Preview",
        UstadAssignmentFileSubmissionHeaderPreview),
    UstadScreen("UstadCourseAssignmentMarkListItem", "UstadCourseAssignmentMarkListItem Preview",
        UstadCourseAssignmentMarkListItemPreview),
    UstadScreen("UstadCommentListItem", "UstadCommentListItem Preview",
        UstadCommentListItemPreview
    ),
    UstadScreen("UstadAddCommentListItem", "UstadAddCommentListItem Preview",
        UstadAddCommentListItemPreview),
    UstadScreen("UstadAssignmentFileSubmissionListItem",
        "UstadAssignmentFileSubmissionListItem Preview",
        UstadAssignmentFileSubmissionListItemPreview),
    UstadScreen("UstadImageSelectButtonPreview", "UstadImageSelectButtonPreview",
        UstadImageSelectButtonPreview),
    UstadScreen("UstadSelectFieldPreview", "UstadSelectFieldPreview",
        UstadSelectFieldPreview),
    UstadScreen("UstadMessageIdSelectFieldPreview", "UstadMessageIdSelectFieldPreview",
        UstadMessageIdSelectFieldPreview),
    UstadScreen("Quill", "Quill", QuillDemo),
    UstadScreen("CourseBlockEdit", CourseBlockEditViewModel.DEST_NAME,
        CourseBlockEditScreen),
    UstadScreen("UstadNumberTextEditField", "UstadNumberTextEditField Preview",
        UstadNumberTextFieldPreview),
    UstadScreen(TimeZoneListViewModel.DEST_NAME, "Time Zone List", TimeZoneListScreen),
    UstadScreen(
        CourseTerminologyListViewModel.DEST_NAME, "Course Terminology List",
        CourseTerminologyListScreen
    ),
    UstadScreen(
        ClazzAssignmentEditView.VIEW_NAME,
        "ClazzAssignmentEdit",
        ClazzAssignmentEditScreen
    ),
    UstadScreen("DateTimeEdit", "Date Time Edit", DateTimeEditFieldPreview),
    UstadScreen(ClazzDetailViewModel.DEST_NAME, "Clazz Detail", ClazzDetailScreen),
    UstadScreen(ClazzDetailOverviewView.VIEW_NAME, "Clazz Detail Overview",
        ClazzDetailOverviewScreen
    ),
    UstadScreen(CourseDiscussionDetailViewModel.DEST_NAME, "Course Discussion Detail Preview",
        CourseDiscussionDetailScreen
    ),
    UstadScreen("DiscussionPostDetailViewDemo", name = "Course Discussion Post Detail",
        DiscussionPostDetailPreview
    ),
    UstadScreen(DiscussionPostDetailViewModel.DEST_NAME, name = "Real Course Discussion Post Detail",
        DiscussionPostDetailScreen
    ),
    UstadScreen(DiscussionPostEditViewModel.DEST_NAME, name = "Discussion Post Edit",
        DiscussionPostEditScreen
    ),
    UstadScreen("DiscussionPostEditViewDemo", name = "Discussion Post Edit Preview",
        DiscussionPostEditPreview
    ),
    UstadScreen(ClazzAssignmentDetailView.VIEW_NAME, "ClazzAssignmentDetail",
        ClazzAssignmentDetailScreen),
    UstadScreen("ClazzAssignmentDetailOverviewPreview", "ClazzAssignmentDetailOverview",
        ClazzAssignmentDetailOverviewScreenPreview
    ),
    UstadScreen(ClazzAssignmentDetailOverviewViewModel.DEST_NAME, "ClazzAssignmentDetailOverview",
        ClazzAssignmentDetailOverviewScreen
    ),
    UstadScreen(ClazzAssignmentDetailSubmissionsTabViewModel.DEST_NAME,
        "ClazzAssignmentDetailStudentProgressOverviewList",
        ClazzAssignmentDetailSubmissionsTabScreen),
    UstadScreen(CourseGroupSetListViewModel.DEST_NAME, "CourseGroupSetList",
        CourseGroupSetListScreen
    ),
    UstadScreen("CourseAssignmentSubmitterDetailPreview", "CourseAssignmentSubmitterDetailPreview",
        ClazzAssignmentSubmitterDetailScreenPreview),
    UstadScreen(ClazzAssignmentSubmitterDetailViewModel.DEST_NAME, "CourseAssignmentSubmitterDetail",
        ClazzAssignmentSubmitterDetailScreen),

    UstadScreen(ScopedGrantListView.VIEW_NAME, "Scoped Grant List", ScopedGrantListScreen),
    UstadScreen("ScopedGrantListScreenPreview", "Scoped Grant List Preview ",
        ScopedGrantListScreenPreview),

    UstadScreen(ScopedGrantDetailView.VIEW_NAME, "Scoped Grant Detail", ScopedGrantDetailScreen),
    UstadScreen("ScopedGrantDetailScreenPreview", "Scoped Grant Detail Preview", ScopedGrantDetailScreenPreview),

    UstadScreen(ScopedGrantEditView.VIEW_NAME, "Scoped Grant Edit", ScopedGrantEditScreen),
)




typealias UstadScreens = Iterable<UstadScreen>
