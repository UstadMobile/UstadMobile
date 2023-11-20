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
import com.ustadmobile.core.viewmodel.LeavingReasonEditViewModel
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewViewModel
import com.ustadmobile.view.leavingreason.edit.LeavingReasonEditScreen
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.ContentEntryDetailOverviewViewModel
import com.ustadmobile.core.viewmodel.HolidayCalendarEditViewModel
import com.ustadmobile.core.viewmodel.ParentalConsentManagementViewModel
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailViewModel
import com.ustadmobile.core.viewmodel.ReportFilterEditViewModel
import com.ustadmobile.core.viewmodel.SchoolDetailOverviewViewModel
import com.ustadmobile.core.viewmodel.SchoolEditViewModel
import com.ustadmobile.core.viewmodel.ScopedGrantEditViewModel
import com.ustadmobile.core.viewmodel.SiteDetailViewModel
import com.ustadmobile.core.viewmodel.SiteEditViewModel
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.edit.ClazzAssignmentEditViewModel
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.HolidayCalendarListViewModel
import com.ustadmobile.core.viewmodel.LanguageListViewModel
import com.ustadmobile.core.viewmodel.courseterminology.edit.CourseTerminologyEditViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.core.viewmodel.schedule.edit.ScheduleEditViewModel
import com.ustadmobile.view.dbexport.DbExportScreen
import com.ustadmobile.core.viewmodel.person.accountedit.PersonAccountEditViewModel
import com.ustadmobile.view.accountlist.AccountListScreen
import com.ustadmobile.view.person.accountedit.PersonAccountEditPreview
import com.ustadmobile.view.person.accountedit.PersonAccountEditScreen
import com.ustadmobile.core.viewmodel.accountlist.AccountListViewModel
import com.ustadmobile.view.contententry.importlink.ContentEntryImportLinkScreen
import com.ustadmobile.core.viewmodel.contententry.importlink.ContentEntryImportLinkViewModel
import com.ustadmobile.view.contententry.getmetadata.ContentEntryGetMetadataScreen
import com.ustadmobile.core.viewmodel.contententry.getmetadata.ContentEntryGetMetadataViewModel
import com.ustadmobile.view.contententry.getmetadata.ContentEntryGetMetadataPreview
import com.ustadmobile.core.viewmodel.contententry.detail.ContentEntryDetailViewModel
import com.ustadmobile.view.contententry.detail.ContentEntryDetailScreen
import com.ustadmobile.view.contententry.detailoverviewtab.ContentEntryDetailOverviewScreen
import com.ustadmobile.core.viewmodel.xapicontent.XapiContentViewModel
import com.ustadmobile.view.xapicontent.XapiContentScreen
import com.ustadmobile.core.viewmodel.pdfcontent.PdfContentViewModel
import com.ustadmobile.view.pdfcontent.PdfContentScreen
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentViewModel
import com.ustadmobile.view.epubcontent.EpubContentScreen
import com.ustadmobile.core.viewmodel.videocontent.VideoContentViewModel
import com.ustadmobile.view.videocontent.VideoContentScreen
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkViewModel

//As per entities/Showcases.kt on MUI-showcase #d71c6d1

data class UstadScreen(
    val key: String,
    val name: String,
    val component: FC<Props>,
)

var USTAD_SCREENS: Iterable<UstadScreen> = setOf(
    UstadScreen(PersonDetailViewModel.DEST_NAME, "Person Detail", PersonDetailScreen),
    UstadScreen("PersonDetailPreview", "Person Detail Preview",
        PersonDetailPreview
    ),
    UstadScreen("UstadEditFields", "Edit Fields", UstadEditFieldPreviews),
    UstadScreen("UstadDetailFields", "Detail Fields", UstadDetailFieldPreview),
    UstadScreen("UstadCourseBlockEdit", "UstadCourseBlockEdit", UstadCourseBlockEditPreview),
    UstadScreen("PersonEditPreview", "Person Edit Preview",
        PersonEditScreenPreview
    ),
    UstadScreen(PersonEditViewModel.DEST_NAME, "PersonEdit", PersonEditScreen),
    UstadScreen(PersonAccountEditView.VIEW_NAME, "Person Account Edit Preview",
        PersonAccountEditPreview
    ),
    UstadScreen(LoginViewModel.DEST_NAME, "Login Preview",
        LoginScreen
    ),
    UstadScreen(SiteEnterLinkViewModel.DEST_NAME, "Site Enter Link Preview",
        SiteEnterLinkScreen
    ),
    UstadScreen(ParentalConsentManagementViewModel.DEST_NAME, "Parental Consent Management Preview",
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
    UstadScreen(ScheduleEditViewModel.DEST_NAME, "ScheduleEdit",
        ScheduleEditScreen
    ),
    UstadScreen(ContentEntryEditViewModel.DEST_NAME, "ContentEntryEdit Preview",
        ContentEntryEditScreen
    ),
    UstadScreen(SiteDetailViewModel.DEST_NAME, name = "Site Detail Preview", SiteDetailPreview),
    UstadScreen(SiteEditViewModel.DEST_NAME, name = "Site Edit Preview", SiteEditPreview),
    UstadScreen(SchoolDetailOverviewViewModel.DEST_NAME, "SchoolDetailOverview Preview",
        SchoolDetailOverviewScreenPreview),
    UstadScreen(SchoolEditViewModel.DEST_NAME, "School Edit Preview",
        SchoolEditScreenPreview),
    UstadScreen(LanguageDetailView.VIEW_NAME, name = "LanguageDetail Preview",
        LanguageDetailPreview),
    UstadScreen(ClazzEditViewModel.DEST_NAME, "Course Edit",
        ClazzEditScreen
    ),
    UstadScreen("EasySort", "Easy Sort", EasySortPreview),
    UstadScreen(ErrorReportView.VIEW_NAME, name = "ErrorReport Preview", ErrorReportPreview),
    UstadScreen(LanguageEditView.VIEW_NAME, "LanguageEdit Preview", LanguageEditPreview),
    UstadScreen(ReportFilterEditViewModel.DEST_NAME,
        "ReportFilterEdit Preview", ReportFilterEditScreenPreview),
    UstadScreen(ScopedGrantDetailView.VIEW_NAME, "ScopedGrantDetail Preview",
        ScopedGrantDetailScreenPreview),
    UstadScreen(ContentEntryImportLinkViewModel.DEST_NAME, "ContentEntryImportLink",
        ContentEntryImportLinkScreen
    ),
    UstadScreen(HolidayCalendarDetailView.VIEW_NAME, "HolidayCalendarDetail Preview",
        HolidayCalendarDetailPreview),
    UstadScreen(
        ContentEntryDetailOverviewViewModel.DEST_NAME,
        "ContentEntryDetailOverview",
        ContentEntryDetailOverviewScreen
    ),
    UstadScreen(HolidayCalendarEditViewModel.DEST_NAME, "HolidayCalendarEdit Preview",
        HolidayCalendarEditPreview),
    UstadScreen(
        ScopedGrantEditViewModel.DEST_NAME,
        "ScopedGrantEdit Preview", ScopedGrantEditScreenPreview),
    UstadScreen(
        CourseTerminologyEditViewModel.DEST_NAME,
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
    UstadScreen(HolidayCalendarListViewModel.DEST_NAME, "HolidayCalendarList Preview",
        HolidayCalendarListScreenPreview),
    UstadScreen(LanguageListViewModel.DEST_NAME, "LanguageList Preview",
        LanguageListScreenPreview),
    UstadScreen("PersonListPreview", "PersonList Preview", PersonListScreenPreview),
    UstadScreen(PersonListViewModel.DEST_NAME, "PersonList", PersonListScreen),
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
    UstadScreen(AccountListViewModel.DEST_NAME, "AccountList", AccountListScreen),
    UstadScreen(
        ClazzMemberListViewModel.DEST_NAME, "ClazzMemberList Preview", ClazzMemberListScreen
    ),
    UstadScreen(ClazzListViewModel.DEST_NAME, "Clazz List",ClazzListScreen),
    UstadScreen(ClazzListViewModel.DEST_NAME_HOME, "ClazzListHome", ClazzListScreen),
    UstadScreen("VirtualListPreview", "Virtual List Preview",
        VirtualListPreview),
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
        ClazzAssignmentEditViewModel.DEST_NAME,
        "ClazzAssignmentEdit",
        ClazzAssignmentEditScreen
    ),
    UstadScreen("DateTimeEdit", "Date Time Edit", DateTimeEditFieldPreview),
    UstadScreen(ClazzDetailViewModel.DEST_NAME, "Clazz Detail", ClazzDetailScreen),
    UstadScreen(
        ClazzDetailOverviewViewModel.DEST_NAME, "Clazz Detail Overview",
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
    UstadScreen(ClazzAssignmentDetailViewModel.DEST_NAME, "ClazzAssignmentDetail",
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
    UstadScreen(LeavingReasonEditViewModel.DEST_NAME, "LeavingReasonEdit", LeavingReasonEditScreen),
    UstadScreen("DbExport", "DbExport", DbExportScreen),
    UstadScreen(PersonAccountEditViewModel.DEST_NAME, "PersonAccountEdit", PersonAccountEditScreen),
    UstadScreen(ContentEntryGetMetadataViewModel.DEST_NAME, "ContentEntryGetMetadata",
        ContentEntryGetMetadataScreen),
    UstadScreen("ContentEntryGetMetadataPreview", "ContentEntryGetMetadataPreview",
        ContentEntryGetMetadataPreview),
    UstadScreen(ContentEntryDetailViewModel.DEST_NAME, "ContentEntryDetail",
        ContentEntryDetailScreen),
    UstadScreen(XapiContentViewModel.DEST_NAME, "XapiContent", XapiContentScreen),
    UstadScreen(PdfContentViewModel.DEST_NAME, "PdfContent", PdfContentScreen),
    UstadScreen(EpubContentViewModel.DEST_NAME, "EpubContent", EpubContentScreen),
    UstadScreen(VideoContentViewModel.DEST_NAME, "VideoContent", VideoContentScreen),
)




typealias UstadScreens = Iterable<UstadScreen>
