package com.ustadmobile.view.contententry.detailattemptstab

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.verbDisplayName
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.composites.xapi.StatementEntityAndVerb
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadProgressBarWithLabel
import js.objects.jso
import kotlinx.datetime.TimeZone
import mui.icons.material.CalendarToday as CalendarTodayIcon
import mui.icons.material.Work as WorkIcon
import react.Props
import react.FC
import mui.material.ListItem
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Stack
import mui.material.StackDirection
import mui.material.SvgIconSize
import mui.system.responsive
import mui.system.sx
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.div
import react.useRequiredContext
import com.ustadmobile.core.MR

external interface StatementEntityAndVerbListItemProps: Props {

    var statement: StatementEntityAndVerb?

}

val StatementEntityAndVerbListItem = FC<StatementEntityAndVerbListItemProps> { props ->
    val activityName = props.statement?.activityLangMapEntry?.almeValue ?: ""
    val theme by useRequiredContext(ThemeContext)
    val formattedDateAndTime = props.statement?.statementEntity?.timestamp?.let {
        useFormattedDateAndTime(
            timeInMillis = it,
            timezoneId = TimeZone.currentSystemDefault().id
        )
    }
    val stringsXml = useStringProvider()

    ListItem {
        ListItemIcon {
            WorkIcon()
        }

        ListItemText {
            primary = ReactNode(
                "${props.statement?.verbDisplayName?.capitalizeFirstLetter() ?: ""} $activityName"
            )

            secondary = Stack.create {
                direction = responsive(StackDirection.column)

                Stack {
                    sx {
                        paddingBottom = theme.spacing(1)
                    }

                    direction = responsive(StackDirection.row)

                    CalendarTodayIcon {
                        sx {
                            marginInlineEnd = theme.spacing(1)
                        }

                        fontSize = SvgIconSize.small
                    }

                    + formattedDateAndTime
                }

                props.statement?.statementEntity?.extensionProgress?.also { progressVal ->
                    UstadProgressBarWithLabel {
                        progressValue = progressVal
                        label = ReactNode(stringsXml[MR.strings.progress_key].capitalizeFirstLetter())
                    }
                }

                props.statement?.statementEntity?.resultScoreScaled?.also {
                    UstadProgressBarWithLabel {
                        progressValue = (it * 100).toInt()
                        label = ReactNode(stringsXml[MR.strings.content_score].capitalizeFirstLetter())
                    }
                }
            }

            secondaryTypographyProps = jso {
                component = div
            }
        }
    }
}
