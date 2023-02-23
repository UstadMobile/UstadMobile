package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.StatementListUiState
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.hooks.useFormattedDuration
import com.ustadmobile.lib.db.entities.StatementWithSessionDetailDisplay
import com.ustadmobile.lib.db.entities.VerbEntity
import csstype.*
import kotlinx.datetime.TimeZone
import mui.icons.material.CalendarToday
import mui.icons.material.Check
import mui.icons.material.Timer
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.img

external interface StatementListScreenProps : Props {

    var uiState: StatementListUiState

    var onClickStatement: (StatementWithSessionDetailDisplay) -> Unit

}

val VERB_ICON_MAP = mapOf(
    VerbEntity.VERB_COMPLETED_UID.toInt() to "img/verb_complete.svg",
    VerbEntity.VERB_PROGRESSED_UID.toInt() to "img/verb_progress.svg",
    VerbEntity.VERB_ATTEMPTED_UID.toInt() to "img/verb_attempt.svg",
    VerbEntity.VERB_INTERACTED_UID.toInt() to "img/verb_interactive.svg",
    VerbEntity.VERB_ANSWERED_UID.toInt() to "img/verb_answered.svg",
    VerbEntity.VERB_SATISFIED_UID.toInt() to "img/verb_passed.svg",
    VerbEntity.VERB_PASSED_UID.toInt() to "img/verb_passed.svg",
    VerbEntity.VERB_FAILED_UID.toInt() to "img/verb_failed.svg"
)

val StatementListScreenComponent2 = FC<StatementListScreenProps> { props ->

    Container {
        List{
            props.uiState.statementList
                .forEach { statementItem ->

                    val  statementUiState = statementItem.listItemUiState

                    ListItem{
                        ListItemButton {

                            onClick = {
                                props.onClickStatement(statementItem)
                            }

                            ListItemIcon {
                                img {
                                    src = VERB_ICON_MAP[statementItem.statementVerbUid.toInt()]
                                    alt = ""
                                    height = 40.0
                                    width = 40.0
                                }
                            }

                            ListItemText {
                                primary = ReactNode(
                                    statementUiState.personVerbTitleText ?: ""
                                )

                                secondary = SecondaryTextContent.create{
                                    statement = statementItem
                                }
                            }
                        }
                    }
                }
        }
    }
}

external interface SecondaryTextContentProps : Props {

    var statement: StatementWithSessionDetailDisplay

}

val SecondaryTextContent = FC<SecondaryTextContentProps> { props ->

    val strings = useStringsXml()

    val  statementUiState = props.statement.listItemUiState
    val  dateTimeFormatter = useFormattedDateAndTime(
        timeInMillis = props.statement.timestamp,
        timezoneId = TimeZone.currentSystemDefault().id
    )
    val  duration = useFormattedDuration(timeInMillis = props.statement.resultDuration)

    Stack {
        if (statementUiState.descriptionVisible){
            Typography {
                + (props.statement.objectDisplay ?: "")
            }
        }

//        Typography {
//            + (props.statement.fullStatement ?: "")
//        }

        Stack{
            direction = responsive(StackDirection.row)

            Icon{ + CalendarToday.create() }

            Typography { + dateTimeFormatter }


            if (statementUiState.resultDurationVisible){

                Box { sx { width = 10.px } }

                Icon { + Timer.create() }

                Typography { + duration }


            }
        }

        if (statementUiState.resultScoreMaxVisible){
            Stack {
                direction = responsive(StackDirection.row)

                Icon{
                    + Check.create()
                }

                Typography {
                    + strings[MessageID.percentage_score]
                        .replace("%1\$s",
                            (props.statement.resultScoreScaled * 100).toString())

                }

                Box { sx { width = 10.px } }

                Typography {
                    + statementUiState.scoreResultsText
                }

            }
        }
    }

}

val StatementListScreenPreview = FC<Props> {

    val uiStateVar : StatementListUiState by useState {
        StatementListUiState(
            statementList = listOf(
                StatementWithSessionDetailDisplay().apply {
                    statementUid = 1
                    verbDisplay = "Completed"
                    objectDisplay = "object Display"
                    resultScoreMax = 90
                    resultScoreScaled = 10F
                    resultScoreRaw = 70
                    resultDuration = 1676432503
                    timestamp = 1676432354
                    statementVerbUid = VerbEntity.VERB_COMPLETED_UID
                },
                StatementWithSessionDetailDisplay().apply {
                    statementUid = 2
                    verbDisplay = "Progressed"
                    objectDisplay = "object Display"
                    resultScoreMax = 90
                    resultScoreScaled = 10F
                    resultScoreRaw = 70
                    statementVerbUid = VerbEntity.VERB_PROGRESSED_UID
                },
                StatementWithSessionDetailDisplay().apply {
                    statementUid = 3
                    verbDisplay = "Attempted"
                    objectDisplay = "object Display"
                    resultScoreMax = 90
                    resultScoreScaled = 10F
                    resultScoreRaw = 70
                    resultDuration = 1676432503
                    timestamp = 1676432354
                    statementVerbUid = VerbEntity.VERB_ATTEMPTED_UID
                },
                StatementWithSessionDetailDisplay().apply {
                    statementUid = 4
                    verbDisplay = "Interacted"
                    objectDisplay = "object Display"
                    resultScoreMax = 90
                    resultScoreScaled = 10F
                    resultScoreRaw = 70
                    resultDuration = 1676432503
                    timestamp = 1676432354
                    statementVerbUid = VerbEntity.VERB_INTERACTED_UID
                },
                StatementWithSessionDetailDisplay().apply {
                    statementUid = 5
                    verbDisplay = "Answered"
                    objectDisplay = "object Display"
                    resultScoreMax = 90
                    resultScoreScaled = 10F
                    resultScoreRaw = 70
                    resultDuration = 1676432503
                    timestamp = 1676432354
                    statementVerbUid = VerbEntity.VERB_ANSWERED_UID
                },
                StatementWithSessionDetailDisplay().apply {
                    statementUid = 6
                    verbDisplay = "Satisfied"
                    objectDisplay = "object Display"
                    resultScoreMax = 90
                    resultScoreScaled = 10F
                    resultScoreRaw = 70
                    resultDuration = 1676432503
                    timestamp = 1676432354
                    statementVerbUid = VerbEntity.VERB_SATISFIED_UID
                },
                StatementWithSessionDetailDisplay().apply {
                    statementUid = 7
                    verbDisplay = "Passed"
                    objectDisplay = "object Display"
                    resultScoreMax = 90
                    resultScoreScaled = 10F
                    resultScoreRaw = 70
                    resultDuration = 1676432503
                    timestamp = 1676432354
                    statementVerbUid = VerbEntity.VERB_PASSED_UID
                },
                StatementWithSessionDetailDisplay().apply {
                    statementUid = 8
                    verbDisplay = "Failed"
                    objectDisplay = "object Display"
                    resultScoreMax = 90
                    resultScoreScaled = 10F
                    resultScoreRaw = 70
                    resultDuration = 1676432503
                    timestamp = 1676432354
                    statementVerbUid = VerbEntity.VERB_FAILED_UID
                }
            )
        )
    }

    StatementListScreenComponent2 {
        uiState = uiStateVar
    }
}