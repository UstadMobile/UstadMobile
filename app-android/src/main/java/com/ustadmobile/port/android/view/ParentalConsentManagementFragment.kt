package com.ustadmobile.port.android.view

import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.locale.entityconstants.PersonParentJoinConstants
import com.ustadmobile.core.viewmodel.parentalconsentmanagement.ParentalConsentManagementUiState
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonParentJoinWithMinorPerson
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.libuicompose.util.rememberFormattedDate
import com.ustadmobile.port.android.view.binding.loadHtmlData
import com.ustadmobile.port.android.view.composable.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.core.R as CR

interface ParentAccountLandingFragmentEventHandler {
    fun onClickConsent()
    fun onClickDoNotConsent()
    fun onClickChangeConsent()
}

class ParentalConsentManagementFragment: UstadBaseMvvmFragment() {


}

@Composable
private fun ParentalConsentManagementScreen(
    uiState: ParentalConsentManagementUiState = ParentalConsentManagementUiState(),
    onClickConsent: () -> Unit = {},
    onClickDoNotConsent: () -> Unit = {},
    onClickChangeConsent: () -> Unit = {},
    onChangeRelation: (PersonParentJoin?) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    )  {

        val minorDateOfBirth = rememberFormattedDate(
            timeInMillis = uiState.personParentJoin?.minorPerson?.dateOfBirth ?: 0,
            timeZoneId = UstadMobileConstants.UTC,
        )
        Text(stringResource(id = CR.string.parent_consent_explanation,
            uiState.personParentJoin?.minorPerson?.fullName() ?: "",
            minorDateOfBirth, uiState.appName))

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.relationshipVisible){
            UstadInputFieldLayout(
                modifier = Modifier.fillMaxWidth(),
                errorText = uiState.relationshipError,
            ) {
                UstadMessageIdOptionExposedDropDownMenuField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.personParentJoin?.ppjRelationship ?: 0,
                    label = stringResource(CR.string.relationship),
                    options = PersonParentJoinConstants.RELATIONSHIP_MESSAGE_IDS,
                    onOptionSelected = {
                        onChangeRelation(uiState.personParentJoin?.shallowCopy{
                            ppjRelationship = it.value
                        })
                    },
                    isError = uiState.relationshipError != null,
                    enabled = uiState.fieldsEnabled,
                )
            }

        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(stringResource(id = CR.string.terms_and_policies),
            style = Typography.h4
        )

        Spacer(modifier = Modifier.height(10.dp))

        AndroidView(factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                webViewClient = WebViewClient()
                loadHtmlData(uiState.siteTerms?.termsHtml)
                setTag(R.id.tag_webview_html, uiState.siteTerms?.termsHtml)
            }},
            update = {
                if(uiState.siteTerms?.termsHtml != it.getTag(R.id.tag_webview_html)) {
                    it.loadHtmlData(uiState.siteTerms?.termsHtml)
                    it.setTag(R.id.tag_webview_html, uiState.siteTerms?.termsHtml)
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.consentVisible){
            Button(
                onClick = onClickConsent,
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = uiState.fieldsEnabled,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.secondaryColor)
                )
            ) {
                Text(stringResource(CR.string.i_consent).uppercase(),
                    color = contentColorFor(
                        colorResource(id = R.color.secondaryColor)
                    )
                )
            }
        }

        if (uiState.dontConsentVisible){
            OutlinedButton(
                onClick = onClickDoNotConsent,
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = uiState.fieldsEnabled,
            ) {
                Text(stringResource(CR.string.i_do_not_consent).uppercase())
            }
        }
        if (uiState.changeConsentVisible){
            val changeConsentText: Int =
                if (uiState.personParentJoin?.ppjStatus == PersonParentJoin.STATUS_APPROVED)
                    CR.string.revoke_consent
                else
                    CR.string.restore_consent

            Button(
                onClick = onClickChangeConsent,
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = uiState.fieldsEnabled,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.secondaryColor)
                )
            ) {
                Text(stringResource(changeConsentText).uppercase(),
                    color = contentColorFor(
                        colorResource(id = R.color.secondaryColor)
                    )
                )
            }
        }
    }
}

@Composable
@Preview
fun ParentalConsentManagementScreenPreview() {
    val uiState = ParentalConsentManagementUiState(
        siteTerms = SiteTerms().apply {
            termsHtml = "https://www.ustadmobile.com"
        },
        personParentJoin = PersonParentJoinWithMinorPerson().apply {
            ppjParentPersonUid = 0
            ppjRelationship = 1
            minorPerson = Person().apply {
                firstNames = "Pit"
                lastName = "The Young"
            }
        },
        fieldsEnabled = true
    )

    MdcTheme {
        ParentalConsentManagementScreen(uiState)
    }
}