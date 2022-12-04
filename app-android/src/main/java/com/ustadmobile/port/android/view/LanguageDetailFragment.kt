package com.ustadmobile.port.android.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.LanguageDetailUiState
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.android.view.composable.UstadDetailField


@Composable
fun LanguageDetailScreen(
    uiState: LanguageDetailUiState
){
    Column (
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ){

        UstadDetailField(
            valueText = uiState.language?.name ?: "",
            labelText = stringResource(R.string.name)
        )

        UstadDetailField(
            valueText = uiState.language?.iso_639_1_standard ?: "",
            labelText = stringResource(R.string.two_letter_code)
        )

        UstadDetailField(
            valueText = uiState.language?.iso_639_2_standard ?: "",
            labelText = stringResource(R.string.three_letter_code)
        )
    }
}

@Composable
@Preview
fun LanguageDetailScreenPreview(){
    LanguageDetailScreen(
        uiState = LanguageDetailUiState(
            language = Language().apply {
                name = "fa"
                iso_639_1_standard = "fa"
                iso_639_2_standard = "fa"
            }
        )
    )
}