package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.SessionListUiState
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.PersonWithSessionsDisplay
import csstype.px
import kotlinx.datetime.TimeZone
import mui.icons.material.*
import mui.material.*
import mui.material.List
import mui.system.responsive
import react.*

external interface SessionListScreenProps : Props {

    var uiState: SessionListUiState

    var onClickPerson: (PersonWithSessionsDisplay) -> Unit

}


private val SessionListScreenComponent2 = FC<SessionListScreenProps> { props ->

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(20.px)

            List{

                props.uiState.sessionsList.forEach { personItem ->
                    PersonListItem {
                        person = personItem
                        onClick = props.onClickPerson
                    }
                }
            }

        }
    }
}


external interface PersonListItemProps : Props {

    var person: PersonWithSessionsDisplay

    var onClick: (PersonWithSessionsDisplay) -> Unit

}

private val PersonListItem = FC<PersonListItemProps> { props ->

    val strings = useStringsXml()

    val dateTimeFormatted = useFormattedDateAndTime(
        timeInMillis = props.person.startDate,
        timezoneId = TimeZone.currentSystemDefault().id
    )

    ListItem{
        ListItemButton {
            onClick = {
                props.onClick(props.person)
            }

            ListItemIcon {
                + Check.create()
            }

            ListItemText {
                primary = Stack.create {
                    direction = responsive(StackDirection.row)

                    Typography {
                        + ("Passed - ")
                    }

                    Typography {
                        props.person.duration
                    }
                }
                secondary = Stack.create {
                    direction = responsive(StackDirection.column)

                    Typography {
                        + dateTimeFormatted
                    }

                    Stack {
                        direction = responsive(StackDirection.row)

                        Typography {
                            + (strings[MessageID.percentage_score].replace("%1\$s",
                                (props.person.resultScoreScaled * 100).toString())
                               )
                        }

                        Typography {
                            + ("${props.person.resultScore} / ${props.person.resultMax}")
                        }
                    }
                }
            }
        }
    }
}

val SessionListScreenPreview = FC<Props> {

    SessionListScreenComponent2 {
        uiState = SessionListUiState(
            sessionsList = listOf(
                PersonWithSessionsDisplay().apply {
                    startDate = 13
                    resultScoreScaled = 100F
                },
                PersonWithSessionsDisplay().apply {
                    startDate = 13
                }
            ),
        )
    }
}