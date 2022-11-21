package com.ustadmobile.hooks

import com.ustadmobile.core.view.*
import com.ustadmobile.entities.UstadScreen
import com.ustadmobile.entities.UstadScreens
import com.ustadmobile.mui.components.UstadEditFieldPreviews
import com.ustadmobile.view.*
import react.useMemo

fun useUstadScreens(): UstadScreens {
    return useMemo(dependencies = emptyArray()) {
        setOf(
            UstadScreen(PersonDetailView.VIEW_NAME, "Person Detail", PersonDetailScreen),
            UstadScreen("PersonDetailPreview", "Person Detail Preview",
                PersonDetailPreview),
            UstadScreen("UstadEditFields", "Edit Fields", UstadEditFieldPreviews),
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
            UstadScreen(RegisterMinorWaitForParentView.VIEW_NAME, "RegisterMinorWaitForParent Preview",
                RegisterMinorWaitForParentPreview),
            UstadScreen(ContentEntryDetailOverviewView.VIEW_NAME, "ContentEntryDetailOverview Preview",
                ContentEntryDetailOverviewScreenPreview),
            UstadScreen(SiteTermsDetailView.VIEW_NAME, "SiteTermsDetail Preview",
                SiteTermsDetailScreenPreview),
            UstadScreen(ClazzEnrolmentEditView.VIEW_NAME, "ClazzEnrolmentEdit Preview",
                ClazzEnrolmentEditScreenPreview),
        )
    }
}
