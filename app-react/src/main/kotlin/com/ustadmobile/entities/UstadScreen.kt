package com.ustadmobile.entities

import com.ustadmobile.core.components.NAVHOST_CLEARSTACK_VIEWNAME
import com.ustadmobile.core.components.NavHostClearStackPlaceholder
import com.ustadmobile.core.view.*
import com.ustadmobile.entities.UstadScreen
import com.ustadmobile.entities.UstadScreens
import com.ustadmobile.mui.components.*
import com.ustadmobile.view.*
import com.ustadmobile.view.components.UstadImageSelectButtonPreview
import com.ustadmobile.view.components.UstadMessageIdSelectFieldPreview
import com.ustadmobile.view.components.UstadSelectFieldPreview
import com.ustadmobile.view.components.virtuallist.VirtualListPreview
import com.ustadmobile.wrappers.reacteasysort.EasySortPreview
import com.ustadmobile.wrappers.draftjs.EditorPreview
import react.useMemo
import react.Props
import react.FC
import com.ustadmobile.wrappers.quill.QuillDemo

//As per entities/Showcases.kt on MUI-showcase #d71c6d1

data class UstadScreen(
    val key: String,
    val name: String,
    val component: FC<Props>,
)

var USTAD_SCREENS: Iterable<UstadScreen> = setOf(
    UstadScreen(PersonDetailView.VIEW_NAME, "Person Detail", PersonDetailScreen),
    UstadScreen("PersonDetailPreview", "Person Detail Preview",
        PersonDetailPreview),
    UstadScreen("UstadEditFields", "Edit Fields", UstadEditFieldPreviews),
    UstadScreen("UstadDetailFields", "Detail Fields", UstadDetailFieldPreview),
    UstadScreen("UstadCourseBlockEdit", "UstadCourseBlockEdit", UstadCourseBlockEditPreview),
    UstadScreen("PersonEditPreview", "Person Edit Preview",
        PersonEditScreenPreview),
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
        ClazzEditScreen),
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
        "CourseTerminologyEdit Preview", CourseTerminologyEditScreenPreview),
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
        ClazzListScreen),
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
    UstadScreen("DraftJS", "DraftJS", EditorPreview),
    UstadScreen("Quill", "Quill", QuillDemo)
)


typealias UstadScreens = Iterable<UstadScreen>
