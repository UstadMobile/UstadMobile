package com.ustadmobile.libuicompose.view.parentalconsentmanagement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.viewmodel.parentalconsentmanagement.ParentalConsentManagementUiState
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.libuicompose.util.rememberFormattedDate
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.locale.entityconstants.PersonParentJoinConstants
import com.ustadmobile.core.viewmodel.parentalconsentmanagement.ParentalConsentManagementViewModel
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadDetailHeader
import com.ustadmobile.libuicompose.components.UstadHtmlText
import com.ustadmobile.libuicompose.components.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import kotlinx.datetime.TimeZone
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun ParentalConsentManagementScreen(
    viewModel: ParentalConsentManagementViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        ParentalConsentManagementUiState())

    ParentalConsentManagementScreen(
        uiState = uiState,
        onClickConsent = viewModel::onClickConsent,
        onClickDoNotConsent = viewModel::onClickDontConsent,
        onClickChangeConsent = viewModel::onClickChangeConsent,
        onChangeRelation = viewModel::onEntityChanged
    )
}

@Composable
fun ParentalConsentManagementScreen(
    uiState: ParentalConsentManagementUiState = ParentalConsentManagementUiState(),
    onClickConsent: () -> Unit = {},
    onClickDoNotConsent: () -> Unit = {},
    onClickChangeConsent: () -> Unit = {},
    onChangeRelation: (PersonParentJoin?) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    )  {
        val minorDateOfBirth = rememberFormattedDate(
            timeInMillis = uiState.parentJoinAndMinor?.minorPerson?.dateOfBirth ?: 0,
            timeZoneId = UstadMobileConstants.UTC,
        )

        if(uiState.consentStatusVisible) {
            val statusDate = rememberFormattedDateTime(
                timeInMillis = uiState.parentJoinAndMinor?.personParentJoin?.ppjApprovalTiemstamp ?: 0L,
                timeZoneId = TimeZone.currentSystemDefault().id,
            )

            Text(
                text = uiState.consentStatusText?.let {
                    stringResource(it, statusDate)
                } ?: "",
                modifier = Modifier.defaultItemPadding(),
            )

            Divider(Modifier.height(1.dp))
        }

        Text(
            text = stringResource(MR.strings.parent_consent_explanation,
                uiState.parentJoinAndMinor?.minorPerson?.fullName() ?: "",
                minorDateOfBirth, uiState.appName),
            modifier = Modifier.defaultItemPadding(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.relationshipVisible){
            UstadMessageIdOptionExposedDropDownMenuField(
                modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                value = uiState.parentJoinAndMinor?.personParentJoin?.ppjRelationship ?: 0,
                label = stringResource(MR.strings.relationship),
                options = PersonParentJoinConstants.RELATIONSHIP_MESSAGE_IDS,
                onOptionSelected = {
                    onChangeRelation(uiState.parentJoinAndMinor?.personParentJoin?.shallowCopy{
                        ppjRelationship = it.value
                    })
                },
                isError = uiState.relationshipError != null,
                enabled = uiState.fieldsEnabled,
            )
        }

        UstadDetailHeader(modifier = Modifier.defaultItemPadding()) {
            Text(stringResource(MR.strings.terms_and_policies))
        }

        Spacer(modifier = Modifier.height(10.dp))

        UstadHtmlText(
            html = uiState.siteTerms?.termsHtml ?: "",
            modifier = Modifier.defaultItemPadding()
        )


        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.consentButtonVisible){
            Button(
                onClick = onClickConsent,
                modifier = Modifier
                    .fillMaxWidth().defaultItemPadding(),
                enabled = uiState.fieldsEnabled,
            ) {
                Text(stringResource(MR.strings.i_consent))
            }
        }

        if (uiState.dontConsentButtonVisible){
            OutlinedButton(
                onClick = onClickDoNotConsent,
                modifier = Modifier
                    .fillMaxWidth().defaultItemPadding(),
                enabled = uiState.fieldsEnabled,
            ) {
                Text(stringResource(MR.strings.i_do_not_consent))
            }
        }

        if (uiState.changeConsentButtonVisible){
            val changeConsentLabel = uiState.changeConsentLabel
            Button(
                onClick = onClickChangeConsent,
                modifier = Modifier
                    .fillMaxWidth().defaultItemPadding(),
                enabled = uiState.fieldsEnabled,
            ) {
                Text(changeConsentLabel?.let { stringResource(it) } ?: "")
            }
        }
    }
}
