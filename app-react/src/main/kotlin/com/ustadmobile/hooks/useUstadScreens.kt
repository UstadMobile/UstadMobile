package com.ustadmobile.hooks

import com.ustadmobile.core.view.*
import com.ustadmobile.entities.UstadScreen
import com.ustadmobile.entities.UstadScreens
import com.ustadmobile.mui.components.UstadCourseBlockEditPreview
import com.ustadmobile.mui.components.UstadDetailFieldPreview
import com.ustadmobile.mui.components.UstadEditFieldPreviews
import com.ustadmobile.view.*
import com.ustadmobile.wrappers.reacteasysort.EasySortPreview
import react.useMemo

fun useUstadScreens(): UstadScreens {
    return useMemo(dependencies = emptyArray()) {
        setOf(
            UstadScreen(PersonDetailView.VIEW_NAME, "Person Detail", PersonDetailScreen),
            UstadScreen("PersonDetailPreview", "Person Detail Preview",
                PersonDetailPreview),
            UstadScreen("UstadEditFields", "Edit Fields", UstadEditFieldPreviews),
            UstadScreen("UstadDetailFields", "Detail Fields", UstadDetailFieldPreview),
            UstadScreen("UstadCourseBlockEdit", "UstadCourseBlockEdit", UstadCourseBlockEditPreview),
            UstadScreen(PersonEditView.VIEW_NAME, "Person Edit Preview",
                PersonEditScreenPreview),
            UstadScreen(PersonAccountEditView.VIEW_NAME, "Person Account Edit Preview",
                PersonAccountEditPreview),
            UstadScreen(Login2View.VIEW_NAME, "Login Preview",
                LoginPreview),
            UstadScreen(SiteEnterLinkView.VIEW_NAME, "Site Enter Link Preview",
                SiteEnterLinkScreenPreview),
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
            UstadScreen(ScheduleEditView.VIEW_NAME, "ScheduleEdit Preview",
                ScheduleEditScreenPreview),
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
            UstadScreen(ClazzEdit2View.VIEW_NAME, "Clazz Edit Preview",
                ClazzEditScreenPreview),
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
            UstadScreen(ContentEntryList2View.VIEW_NAME, "ContentEntryList Preview",
                ContentEntryListScreenPreview)
        )
    }
}
