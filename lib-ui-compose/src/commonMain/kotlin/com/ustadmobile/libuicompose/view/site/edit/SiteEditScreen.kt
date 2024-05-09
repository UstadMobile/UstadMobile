package com.ustadmobile.libuicompose.view.site.edit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.viewmodel.site.edit.SiteEditUiState
import com.ustadmobile.lib.db.entities.Site
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.viewmodel.site.edit.SiteEditViewModel
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadEditHeader
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.libuicompose.components.UstadRichTextEdit
import com.ustadmobile.libuicompose.components.UstadSetLanguageDropDown
import com.ustadmobile.libuicompose.components.UstadSwitchField
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun SiteEditScreen(
    viewModel: SiteEditViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        SiteEditUiState(), Dispatchers.Main.immediate
    )

    SiteEditScreen(
        uiState = uiState,
        onSiteChanged = viewModel::onEntityChanged,
        onChangeTermsLanguage = viewModel::onChangeTermsLanguage,
        onChangeTermsHtml = viewModel::onChangeTermsHtml,
        onClickEditTermsInNewScreen = viewModel::onClickEditTermsInNewScreen,
    )
}


@Composable
fun SiteEditScreen(
    uiState: SiteEditUiState,
    onSiteChanged: (Site?) -> Unit = {},
    onChangeTermsLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit = { },
    onChangeTermsHtml: (String) -> Unit =  { },
    onClickEditTermsInNewScreen: () -> Unit = { },
){
    UstadVerticalScrollColumn (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ){
        OutlinedTextField(
            value = uiState.site?.siteName ?: "",
            modifier = Modifier.fillMaxWidth()
                .defaultItemPadding()
                .testTag("name"),
            onValueChange = {
                onSiteChanged(uiState.site?.shallowCopy{
                    siteName = it
                })
            },
            label = { Text(stringResource(MR.strings.name_key) + "*") },
            isError = uiState.siteNameError != null,
            enabled = uiState.fieldsEnabled,
            supportingText = {
                Text(uiState.siteNameError ?: stringResource(MR.strings.required))
            }
        )

        UstadSwitchField(
            modifier = Modifier.defaultItemPadding().testTag("guest_login_enabled"),
            checked = uiState.site?.guestLogin ?: false,
            label = stringResource(MR.strings.guest_login_enabled),
            onChange = {
                onSiteChanged(uiState.site?.shallowCopy {
                    guestLogin = it
                })
            }
        )

        UstadInputFieldLayout(
            errorText = uiState.registrationEnabledError
        ) {
            UstadSwitchField(
                modifier = Modifier.defaultItemPadding().testTag("registration_allowed"),
                checked = uiState.site?.registrationAllowed ?: false,
                label = stringResource(MR.strings.registration_allowed),
                onChange = {
                    onSiteChanged(uiState.site?.shallowCopy {
                        registrationAllowed = it
                    })
                },
            )
        }

        UstadEditHeader(stringResource(MR.strings.terms_and_policies))

        UstadSetLanguageDropDown(
            langList = uiState.uiLangs,
            currentLanguage = uiState.currentSiteTermsLang,
            onItemSelected = onChangeTermsLanguage,
            modifier = Modifier.defaultItemPadding().testTag("language"),
        )

        UstadRichTextEdit(
            html = uiState.currentSiteTermsHtml ?: "",
            onHtmlChange = onChangeTermsHtml,
            onClickToEditInNewScreen = onClickEditTermsInNewScreen,
            placeholderText = stringResource(MR.strings.terms_and_policies),
            modifier = Modifier.defaultItemPadding().fillMaxWidth(),
            editInNewScreenLabel = stringResource(MR.strings.terms_and_policies) +
                    " (${uiState.currentSiteTermsLang.langDisplay})",
        )

    }
}
