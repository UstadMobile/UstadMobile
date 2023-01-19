package com.ustadmobile.view

import com.ustadmobile.core.viewmodel.PersonListUiState
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.mui.components.UstadListSortHeader
import csstype.px
import mui.icons.material.AccountCircle
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode

external interface PersonListProps: Props {
    var uiState: PersonListUiState
    var onClickSort: () -> Unit
    var onListItemClick: (PersonWithDisplayDetails) -> Unit
}

val PersonListComponent2 = FC<PersonListProps> { props ->
    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadListSortHeader {
                activeSortOrderOption = props.uiState.sortOption
                enabled = true
                onClickSort = {
                    props.onClickSort()
                }
            }

            List{
                props.uiState.personList.forEach { person ->
                    ListItem{

                        ListItemButton{

                            onClick = {
                                props.onListItemClick(person)
                            }

                            ListItemIcon{
                                AccountCircle()
                            }

                            ListItemText {
                                primary = ReactNode("${person.firstNames} ${person.lastName}")
                            }
                        }
                    }
                }
            }
        }
    }
}

val PersonListScreenPreview = FC<Props> {
    PersonListComponent2{
        uiState = PersonListUiState(
            personList = listOf(
                PersonWithDisplayDetails().apply {
                    firstNames = "Ahmad"
                    lastName = "Ahmadi"
                    admin = true
                    personUid = 3
                }
            )
        )
    }
}