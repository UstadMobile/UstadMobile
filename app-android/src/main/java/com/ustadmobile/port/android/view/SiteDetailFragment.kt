package com.ustadmobile.port.android.view

import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.*
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.SiteDetailUiState
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.composable.UstadDetailField
import com.ustadmobile.core.R as CR

class SiteDetailFragment: UstadBaseMvvmFragment() {

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SiteDetailScreen(
    uiState: SiteDetailUiState,
    onClickLang: (SiteTermsWithLanguage) -> Unit = {},
){

    Column {
        UstadDetailField(
            valueText = uiState.site?.siteName ?: "",
            labelText = stringResource(CR.string.name_key),
            imageId = R.drawable.ic_account_balance_black_24dp
        )
        UstadDetailField(
            valueText = stringResource(if(uiState.site?.guestLogin == true){CR.string.yes} else {CR.string.no}),
            labelText = stringResource(id = CR.string.guest_login_enabled),
            imageId = R.drawable.ic_document_preview
        )
        UstadDetailField(
            valueText = stringResource(if(uiState.site?.registrationAllowed == true){CR.string.yes} else {CR.string.no}),
            labelText = stringResource(id = CR.string.registration_allowed),
            imageId = R.drawable.ic_baseline_how_to_reg_24
        )
        Text(stringResource(CR.string.terms_and_policies), style = Typography.h6)

        uiState.siteTerms.forEach {siteTermsWithLanguage -> 
            ListItem(
                modifier = Modifier.clickable {
                    onClickLang(siteTermsWithLanguage)
                },
                text = {Text(siteTermsWithLanguage.stLanguage?.name ?: "")},
            )
        }
    }
}

@Composable
@Preview
fun SiteDetailScreenPreview(){
    SiteDetailScreen(
        uiState = SiteDetailUiState(
            site = Site().apply {
                siteName = "My Site"
            },
            siteTerms = listOf(
                SiteTermsWithLanguage().apply {
                    stLanguage = Language().apply {
                        name = "fa"
                    }
                }
            )
        ),
    )
}
