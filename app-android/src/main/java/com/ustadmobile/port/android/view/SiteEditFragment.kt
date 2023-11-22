package com.ustadmobile.port.android.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.SiteEditUiState
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.composable.UstadSwitchField
import com.ustadmobile.port.android.view.composable.UstadTextEditField
import com.ustadmobile.core.R as CR

class SiteEditFragment: UstadBaseMvvmFragment() {


}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SiteEditScreen(
    uiState: SiteEditUiState,
    onSiteChanged: (Site?) -> Unit = {},
    onItemClicked: (SiteTermsWithLanguage) -> Unit = {},
    onDeleteIconClicked: (SiteTermsWithLanguage) -> Unit = {},
    onClickAddItem: () -> Unit = {}
){
    Column (
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ){

        UstadTextEditField(
            value = uiState.site?.siteName ?: "",
            label = stringResource(id = CR.string.name_key),
            error = uiState.siteNameError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onSiteChanged(uiState.site?.shallowCopy{
                    siteName = it
                })
            }
        )

        UstadSwitchField(
            modifier = Modifier.padding(top = 10.dp),
            checked = uiState.site?.guestLogin ?: false,
            label = stringResource(id = CR.string.guest_login_enabled),
            onChange = {
                onSiteChanged(uiState.site?.shallowCopy {
                    guestLogin = it
                })
            }
        )

        UstadSwitchField(
            modifier = Modifier.padding(top = 15.dp),
            checked = uiState.site?.registrationAllowed ?: false,
            label = stringResource(id = CR.string.registration_allowed),
            onChange = {
                onSiteChanged(uiState.site?.shallowCopy {
                    registrationAllowed = it
                })
            }
        )

        Text(
            stringResource(CR.string.terms_and_policies),
            style = Typography.h6,
        )

        ListItem(
            modifier = Modifier.clickable {
                onClickAddItem()
            },
            icon = {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null
                )
            },
            text = {Text(stringResource(id = CR.string.terms_and_policies))}
        )

        uiState.siteTerms.forEach {item ->
            ListItem(
                modifier = Modifier.clickable {
                    onItemClicked(item)
                },
                text = {Text(item.stLanguage?.name ?: "")},
                        trailing = {
                    IconButton(onClick = {
                        onDeleteIconClicked(item)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(CR.string.delete)
                        )
                    }
                }
            )
        }

    }
}

@Composable
@Preview
fun SiteEditScreenPreview(){
    SiteEditScreen(
        uiState = SiteEditUiState(
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