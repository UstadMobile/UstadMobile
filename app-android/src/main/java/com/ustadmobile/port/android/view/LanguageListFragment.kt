package com.ustadmobile.port.android.view

import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.LanguageListUiState
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.android.view.composable.UstadListSortHeader

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LanguageListScreen(
    uiState: LanguageListUiState,
    onListItemClick: (Language) -> Unit = {},
    onClickSort: () -> Unit = {}
){
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {

        item {
            UstadListSortHeader(
                activeSortOrderOption = uiState.sortOrder,
                onClickSort = onClickSort
            )
        }

        items(
            uiState.languageList,
            key = {
                it.langUid
            }
        ){  language ->
            ListItem(
                modifier = Modifier
                    .clickable {
                        onListItemClick(language)
                    },
                text = { Text(text = language.name ?: "")},
                secondaryText = { Text(text = "${language.iso_639_1_standard} / ${language.iso_639_2_standard}")}
            )
        }
    }
}

@Composable
@Preview
fun LanguageListScreenPreview(){
    LanguageListScreen(
        uiState = LanguageListUiState(
            languageList = listOf(
                Language().apply {
                    name = "Farsi"
                    iso_639_1_standard = "fa"
                    iso_639_2_standard = "far"
                    langUid = 5
                },
                Language().apply {
                    name = "English"
                    iso_639_1_standard = "en"
                    iso_639_2_standard = "eng"
                    langUid = 6
                }
            )
        )
    )
}