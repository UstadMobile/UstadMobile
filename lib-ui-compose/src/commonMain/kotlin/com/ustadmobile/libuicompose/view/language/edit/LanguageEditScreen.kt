package com.ustadmobile.libuicompose.view.language.edit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.LanguageEditUiState
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn

@Composable
fun LanguageEditScreen(
    uiState: LanguageEditUiState,
    onLanguageChanged: (Language?) -> Unit = {}
){
    UstadVerticalScrollColumn (
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {

        UstadInputFieldLayout(
            modifier = Modifier.fillMaxWidth(),
            errorText = uiState.languageNameError,
        ) {
            OutlinedTextField(
                value = uiState.language?.name ?: "",
                modifier = Modifier.testTag("lang_name").fillMaxWidth(),
                label = {
                    Text(stringResource(MR.strings.name_key))
                },
                isError = uiState.languageNameError != null,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onLanguageChanged(uiState.language?.shallowCopy{
                        name = it
                    })
                }
            )
        }

        UstadInputFieldLayout(
            modifier = Modifier.fillMaxWidth(),
            errorText = uiState.twoLettersCodeError,
        ) {
            OutlinedTextField(
                modifier = Modifier.testTag("two_letter_code").fillMaxWidth(),
                value = uiState.language?.iso_639_1_standard ?: "",
                label = {
                    Text(stringResource(MR.strings.two_letter_code))
                },
                isError = uiState.twoLettersCodeError != null,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onLanguageChanged(uiState.language?.shallowCopy{
                        iso_639_1_standard = it
                    })
                }
            )
        }

        UstadInputFieldLayout(
            modifier = Modifier.fillMaxWidth(),
            errorText = uiState.threeLettersCodeError
        ) {
            OutlinedTextField(
                modifier = Modifier.testTag("three_letter_code").fillMaxWidth(),
                value = uiState.language?.iso_639_2_standard ?: "",
                label = {
                    Text(stringResource(MR.strings.three_letter_code))
                },
                isError = uiState.threeLettersCodeError != null,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onLanguageChanged(uiState.language?.shallowCopy{
                        iso_639_2_standard = it
                    })
                }
            )
        }


    }
}
