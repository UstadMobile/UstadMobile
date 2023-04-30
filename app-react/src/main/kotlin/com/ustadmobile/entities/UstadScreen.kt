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
import com.ustadmobile.view.timezonelist.TimeZoneListScreen
import com.ustadmobile.core.viewmodel.TimeZoneListViewModel
import com.ustadmobile.core.viewmodel.CourseTerminologyListViewModel
import com.ustadmobile.view.clazzassignment.edit.ClazzAssignmentEditScreen
import com.ustadmobile.view.courseterminologylist.CourseTerminologyListScreen
import com.ustadmobile.view.clazz.detail.ClazzDetailScreen
import com.ustadmobile.view.clazz.list.ClazzListScreen
import com.ustadmobile.view.clazz.detailoverview.ClazzDetailOverviewScreen
import com.ustadmobile.view.clazzgroupset.ClazzGroupSetDummy
import com.ustadmobile.view.person.detail.PersonDetailPreview
import com.ustadmobile.view.person.detail.PersonDetailScreen
import com.ustadmobile.view.person.edit.PersonEditScreen
import com.ustadmobile.view.person.edit.PersonEditScreenPreview
import com.ustadmobile.view.person.list.PersonListScreen
import com.ustadmobile.view.person.list.PersonListScreenPreview
import com.ustadmobile.view.clazzassignment.detail.ClazzAssignmentDetailScreen

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
        LoginScreen),
    UstadScreen(SiteEnterLinkView.VIEW_NAME, "Site Enter Link Preview",
        SiteEnterLinkScreen),
    UstadScreen(ParentalConsentManagementView.VIEW_NAME, "Parental Consent Management Preview",
        ParentalConsentManagementPreview),
    UstadScreen(SettingsView.VIEW_NAME, "Settings Preview",
        SettingsPreview),
    UstadScreen(InviteViaLinkView.VIEW_NAME, "Invite Via Link Preview",
        InviteViaLinkPreview),
    UstadScreen(ClazzEnrolmentEditView.VIEW_NAME, "ClazzEnrolmentEdit Preview",
        ClazzEnrolmentEditScreenPreview),
    UstadScreen(SiteTermsDetailView.VIEW_NAME, "SiteTermsDetail Preview",
        SiteTermsDetailScreenPreview),
    UstadScreen(RegisterMinorWaitForParentView.VIEW_NAME, "RegisterMinorWaitForParent Preview",
        RegisterMinorWaitForParentPreview),
    UstadScreen(ScheduleEditView.VIEW_NAME, "ScheduleEdit",
        ScheduleEditScreen),
    UstadScreen(ContentEntryEdit2View.VIEW_NAME, "ContentEntryEdit Preview",
        ContentEntryEditScreenPreview),
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
    UstadScreen(ScopedGrantDetailView.VIEW_NAME, "ScopedGrantDetail Preview",
        ScopedGrantDetailScreenPreview),
    UstadScreen(ContentEntryImportLinkView.VIEW_NAME, "ContentEntryImportLink Preview",
        ContentEntryImportLinkScreenPreview),
    UstadScreen(HolidayCalendarDetailView.VIEW_NAME, "HolidayCalendarDetail Preview",
        HolidayCalendarDetailPreview),
    UstadScreen(ContentEntryDetailOverviewView.VIEW_NAME,
        "ContentEntryDetailOverview Preview",
        ContentEntryDetailOverviewScreenPreview),
    UstadScreen(HolidayCalendarEditView.VIEW_NAME, "HolidayCalendarEdit Preview",
        HolidayCalendarEditPreview),
    UstadScreen(ScopedGrantEditView.VIEW_NAME,
        "ScopedGrantEdit Preview", ScopedGrantEditScreenPreview),
    UstadScreen(CourseTerminologyEditView.VIEW_NAME,
        "CourseTerminologyEdit Preview", CourseTerminologyEditScreen),
    UstadScreen("UstadListFilterChipsHeader",
        "UstadListFilterChipsHeaderPreview Preview", UstadListFilterChipsHeaderPreview),
    UstadScreen("UstadListSortHeader",
        "UstadListSortHeader", UstadListSortHeaderPreview),
    UstadScreen(ClazzLogEditView.VIEW_NAME, "ClazzLogEdit Preview",
        ClazzLogEditScreenPreview),
    UstadScreen(CourseGroupSetEditView.VIEW_NAME, "CourseGroupSetEdit Preview",
        CourseGroupSetEditScreenPreview),
    UstadScreen("UstadContentEntryListItem", "UstadContentEntryListItem Preview",
        UstadContentEntryListItemPreview),
    UstadScreen(CourseGroupSetDetailView.VIEW_NAME, "CourseGroupSetDetail Preview",
        CourseGroupSetDetailScreenPreview),
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
    UstadScreen(ClazzLogEditAttendanceView.VIEW_NAME, name = "ClazzLogEditAttendance Preview",
        ClazzLogEditAttendanceScreenPreview),
    UstadScreen(JoinWithCodeView.VIEW_NAME, "JoinWithCode Preview",
        JoinWithCodeScreenPreview),
    UstadScreen(ClazzLogListAttendanceView.VIEW_NAME, "ClazzLogListAttendance Preview",
        ClazzLogListAttendanceScreenPreview),
    UstadScreen(ContentEntryList2View.VIEW_NAME, "ContentEntryList Preview",
        ContentEntryListScreenPreview),
    UstadScreen(AccountListView.VIEW_NAME, "AccountList Preview", AccountListScreenPreview),
    UstadScreen(ClazzMemberListView.VIEW_NAME, "ClazzMemberList Preview",
        ClazzMemberListScreenPreview),
    UstadScreen(ClazzList2View.VIEW_NAME, "Clazz List",
        ClazzListScreen
    ),
    UstadScreen("VirtualListPreview", "Virtual List Preview",
        VirtualListPreview),
    UstadScreen("UstadAssignmentFileSubmissionHeader",
        "UstadAssignmentFileSubmissionHeader Preview",
        UstadAssignmentFileSubmissionHeaderPreview),
    UstadScreen("UstadCourseAssignmentMarkListItem", "UstadCourseAssignmentMarkListItem Preview",
        UstadCourseAssignmentMarkListItemPreview),
    UstadScreen("UstadCommentListItem", "UstadCommentListItem Preview",
        UstadCommentListItemPreview),
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
    UstadScreen(CourseTerminologyListViewModel.DEST_NAME, "Course Terminology List",
        CourseTerminologyListScreen),
    UstadScreen(
        ClazzAssignmentEditView.VIEW_NAME,
        "ClazzAssignmentEdit",
        ClazzAssignmentEditScreen
    ),
    UstadScreen("DateTimeEdit", "Date Time Edit", DateTimeEditFieldPreview),
    UstadScreen(ClazzDetailView.VIEW_NAME, "Clazz Detail", ClazzDetailScreen),
    UstadScreen(ClazzDetailOverviewView.VIEW_NAME, "Clazz Detail Overview",
        ClazzDetailOverviewScreen
    ),
    UstadScreen(CourseGroupSetListView.VIEW_NAME, "Clazz Groups",ClazzGroupSetDummy),
    UstadScreen(ClazzAssignmentDetailView.VIEW_NAME, "ClazzAssignmentDetail",
        ClazzAssignmentDetailScreen),
    UstadScreen(ClazzAssignmentDetailOverviewView.VIEW_NAME, "ClazzAssignmentDetailOverview",
        ClazzAssignmentDetailOverviewScreenPreview)
)




typealias UstadScreens = Iterable<UstadScreen>
