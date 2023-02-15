package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.ext.contentCompleteStatus
import com.ustadmobile.core.viewmodel.SessionListUiState
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.hooks.useFormattedDuration
import com.ustadmobile.lib.db.entities.PersonWithSessionsDisplay
import com.ustadmobile.lib.db.entities.StatementEntity
import csstype.px
import kotlinx.datetime.TimeZone
import kotlinx.html.injector.injectTo
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

val CONTENT_COMPLETE_MAP_IMAGE = mapOf(
    PersonWithSessionsDisplay.RESULT_SUCCESS to Check,
    PersonWithSessionsDisplay.RESULT_FAILURE to Close,
    PersonWithSessionsDisplay.RESULT_UNSET to null,
    PersonWithSessionsDisplay.RESULT_INCOMPLETE to null,
)

val CONTENT_COMPLETE_MAP_TEXT = mapOf(
    PersonWithSessionsDisplay.RESULT_SUCCESS to MessageID.passed,
    PersonWithSessionsDisplay.RESULT_FAILURE to MessageID.failed,
    PersonWithSessionsDisplay.RESULT_UNSET to MessageID.completed,
    PersonWithSessionsDisplay.RESULT_INCOMPLETE to MessageID.incomplete,
)

private val PersonListItem = FC<PersonListItemProps> { props ->

    val strings = useStringsXml()

    val dateTimeFormatted = useFormattedDateAndTime(
        timeInMillis = props.person.startDate,
        timezoneId = TimeZone.currentSystemDefault().id
    )

    val uiState = props.person.listItemUiState

    val duration = useFormattedDuration(timeInMillis = props.person.duration)

    val contentCompleteStatus = props.person.contentCompleteStatus()

    ListItem{
        ListItemButton {
            onClick = {
                props.onClick(props.person)
            }

            ListItemIcon {
                + (CONTENT_COMPLETE_MAP_IMAGE[contentCompleteStatus]
                    ?: Check).create()
            }

            ListItemText {
                primary = Stack.create {
                    direction = responsive(StackDirection.row)

                    Typography {
                        + strings[CONTENT_COMPLETE_MAP_TEXT[contentCompleteStatus]
                            ?: MessageID.passed]
                    }

                    Typography {
                       + (" - $duration")
                    }
                }
                secondary = Stack.create {
                    direction = responsive(StackDirection.column)

                    Typography {
                        + dateTimeFormatted
                    }


                    if (uiState.scoreResultVisible){
                        Stack {
                            direction = responsive(StackDirection.row)
                            spacing = responsive(10.px)

                            + Check.create()

                            Typography {
                                + (strings[MessageID.percentage_score]
                                    .replace("%1\$s",
                                        (props.person.resultScoreScaled * 100F).toString())
                                )

                            }

                            Typography {
                                + uiState.scoreResultText
                            }
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
                    resultScoreScaled = 3F
                    resultScore = 5
                    resultMax = 10
                    resultComplete = true
                    resultSuccess = StatementEntity.RESULT_UNSET
                },
                PersonWithSessionsDisplay().apply {
                    startDate = 13
                    resultComplete = true
                },
                PersonWithSessionsDisplay().apply {
                    startDate = 13
                    resultScoreScaled = 3F
                    resultScore = 5
                    resultMax = 10
                    resultComplete = true
                    resultSuccess = StatementEntity.RESULT_SUCCESS
                },
                PersonWithSessionsDisplay().apply {
                    startDate = 13
                    resultScoreScaled = 3F
                    resultScore = 5
                    resultMax = 10
                    resultComplete = true
                    resultSuccess = StatementEntity.RESULT_UNSET
                },
                PersonWithSessionsDisplay().apply {
                    startDate = 13
                    resultScoreScaled = 3F
                    resultScore = 5
                    resultMax = 10
                    resultComplete = false
                }
            ),
        )
    }
}