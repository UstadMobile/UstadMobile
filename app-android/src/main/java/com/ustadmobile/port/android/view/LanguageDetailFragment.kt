package com.ustadmobile.port.android.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.LanguageDetailUiState
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.android.view.composable.UstadDetailField
import com.ustadmobile.core.R as CR


@Composable
fun LanguageDetailScreen(
    uiState: LanguageDetailUiState
){
    Column (
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ){

        UstadDetailField(
            valueText = uiState.language?.name ?: "",
            labelText = stringResource(CR.string.name_key),
            imageId = 0,
        )

        UstadDetailField(
            valueText = uiState.language?.iso_639_1_standard ?: "",
            labelText = stringResource(CR.string.two_letter_code),
            imageId = 0,
        )

        UstadDetailField(
            valueText = uiState.language?.iso_639_2_standard ?: "",
            labelText = stringResource(CR.string.three_letter_code),
            imageId = 0,
        )
    }
}

@Composable
@Preview
fun LanguageDetailScreenPreview(){
    LanguageDetailScreen(
        uiState = LanguageDetailUiState(
            language = Language().apply {
                name = "فارسی"
                iso_639_1_standard = "fa"
                iso_639_2_standard = "per"
            }
        )
    )
}