package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.StatementListUiState
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.StatementWithSessionDetailDisplay
import com.ustadmobile.lib.db.entities.VerbEntity
import csstype.px
import kotlinx.datetime.TimeZone
import mui.icons.material.CalendarToday
import mui.icons.material.Check
import mui.icons.material.Folder
import mui.icons.material.Timer
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.*

external interface StatementListScreenProps : Props {

    var uiState: StatementListUiState

    var onClickStatement: (StatementWithSessionDetailDisplay) -> Unit

}

val StatementListScreenComponent2 = FC<StatementListScreenProps> { props ->

    val strings = useStringsXml()

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

                            ListItemText {
                                primary = ReactNode(
                                    statementUiState.personVerbTitleText ?: ""
                                )

                                secondary = SecondaryTextContent.create{
                                    statement = statementItem
                                }
                            }
                        }

                        secondaryAction = Icon.create{
                            + Folder.create()
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

    Stack {
        if (statementUiState.descriptionVisible){
            Typography {
                + (props.statement.objectDisplay ?: "")
            }
        }

        if (statementUiState.questionAnswerVisible){
            Typography {
                + (props.statement.objectDisplay ?: "")
            }
        }

        Stack{
            direction = responsive(StackDirection.row)


            Typography {
                + "1 hour 30 mins"
            }

            Icon {
                + Timer.create()
            }

            Box{
                sx { width = 8.px }
            }

            Typography {
                + dateTimeFormatter
            }

            Icon{
                + CalendarToday.create()
            }
        }

        if (statementUiState.resultScoreMaxVisible){
            Stack {
                direction = responsive(StackDirection.row)

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
                    statementVerbUid = VerbEntity.VERB_COMPLETED_UID
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