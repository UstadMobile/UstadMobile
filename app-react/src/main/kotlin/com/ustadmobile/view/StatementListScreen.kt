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
    VerbEntity.VERB_ATTEMPTED_UID.toInt() to "img/verb_complete.svg",
    VerbEntity.VERB_INTERACTED_UID.toInt() to "img/verb_complete.svg",
    VerbEntity.VERB_ANSWERED_UID.toInt() to "img/verb_complete.svg",
    VerbEntity.VERB_SATISFIED_UID.toInt() to "img/verb_complete.svg",
    VerbEntity.VERB_PASSED_UID.toInt() to "img/verb_complete.svg",
    VerbEntity.VERB_FAILED_UID.toInt() to "img/verb_complete.svg"
)

val StatementListScreenComponent2 = FC<StatementListScreenProps> { props ->

    Container {
        List{
            props.uiState.statementList
                .forEach { statementItem ->

                    val  statementUiState = statementItem.listItemUiState

                    ListItem{
                        ListItemButton {
                            sx {
                                textAlign = TextAlign.end
                            }

                            onClick = {
                                props.onClickStatement(statementItem)
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

                        secondaryAction = img.create {
                            src = VERB_ICON_MAP[statementItem.statementVerbUid.toInt()]
                            alt = ""
                            height = 40.0
                            width = 40.0
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
                sx {
                    textAlign = TextAlign.start
                }
                + (props.statement.objectDisplay ?: "")
            }
        }

//        Typography {
//            + (props.statement.fullStatement ?: "")
//        }

        Stack{
            direction = responsive(StackDirection.row)
            sx {
                justifyContent = JustifyContent.end
            }

            if (statementUiState.resultDurationVisible){

                Typography { + duration }

                Icon { + Timer.create() }

                Box{ sx { width = 8.px } }
            }

            Typography { + dateTimeFormatter }

            Icon{ + CalendarToday.create() }
        }

        if (statementUiState.resultScoreMaxVisible){
            Stack {
                direction = responsive(StackDirection.row)
                sx {
                    justifyContent = JustifyContent.end
                }

                Typography {
                    + statementUiState.scoreResultsText
                }

                Typography {
                    + strings[MessageID.percentage_score]
                        .replace("%1\$s",
                            (props.statement.resultScoreScaled * 100).toString())

                }
                Icon{
                    + Check.create()
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
                    statementVerbUid = VerbEntity.VERB_COMPLETED_UID
                    verbDisplay = "Answered"
                    objectDisplay = "object Display"
                    resultScoreMax = 90
                    resultScoreScaled = 10F
                    resultScoreRaw = 70
                    timestamp = 10000
                    resultDuration = 10009
                },
                StatementWithSessionDetailDisplay().apply {
                    statementUid = 2
                    statementVerbUid = VerbEntity.VERB_INTERACTED_UID
                    verbDisplay = "Answered"
                    objectDisplay = "object Display"
                    resultScoreMax = 90
                    resultScoreScaled = 10F
                    resultScoreRaw = 70
                }
            )
        )
    }

    StatementListScreenComponent2 {
        uiState = uiStateVar
    }
}