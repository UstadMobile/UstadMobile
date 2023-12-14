package com.ustadmobile.libuicompose.view.parentalconsentmanagement

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.parentalconsentmanagement.ParentalConsentManagementUiState
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonParentJoinAndMinorPerson
import com.ustadmobile.lib.db.entities.SiteTerms


@Composable
@Preview
fun ParentalConsentManagementScreenPreview() {
    val uiState = ParentalConsentManagementUiState(
        siteTerms = SiteTerms().apply {
            termsHtml = "https://www.ustadmobile.com"
        },
        parentJoinAndMinor = PersonParentJoinAndMinorPerson(
            personParentJoin = PersonParentJoin().apply {
                ppjParentPersonUid = 0
                ppjRelationship = 1
            },
            minorPerson = Person().apply {
                firstNames = "Pit"
                lastName = "The Young"
            }
        ),
        fieldsEnabled = true
    )


    ParentalConsentManagementScreen(uiState)

}
