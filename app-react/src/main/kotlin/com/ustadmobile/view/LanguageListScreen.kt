package com.ustadmobile.view

import com.ustadmobile.core.viewmodel.LanguageListUiState
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.mui.components.UstadListSortHeader
import csstype.px
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode

external interface LanguageListProps: Props {
    var uiState: LanguageListUiState
    var onClickSort: () -> Unit
    var onListItemClick: (Language) -> Unit
}

val LanguageListComponent2 = FC<LanguageListProps> { props ->
    Container{
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadListSortHeader {
                activeSortOrderOption = props.uiState.sortOrder
                enabled = true
                onClickSort = {
                    props.onClickSort()
                }
            }

            List {
                props.uiState.languageList.forEach { language ->
                    ListItem {
                        ListItemButton {

                            onClick = {
                                props.onListItemClick(language)
                            }

                            ListItemText{
                                primary = ReactNode(language.name ?: "")
                                secondary = ReactNode("${language.iso_639_1_standard} / ${language.iso_639_2_standard}")
                            }
                        }

                    }
                }
            }
        }
    }
}

val LanguageListScreenPreview = FC<Props> {
    LanguageListComponent2 {
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
    }
}