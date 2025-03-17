package com.ustadmobile.view.contententry.detailattemptstab

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.descriptionStringRes
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.statementSummary
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.composites.xapi.SessionTimeAndProgressInfo
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadProgressBarWithLabel
import js.objects.jso
import kotlinx.datetime.TimeZone
import mui.icons.material.Pending
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Stack
import mui.material.StackDirection
import mui.material.SvgIconSize
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.div
import react.useRequiredContext
import mui.icons.material.CalendarToday as CalendarTodayIcon
import mui.icons.material.Check as CheckIcon
import mui.icons.material.HighlightOffOutlined as HighlightOffOutlinedIcon
import mui.icons.material.Star as StarIcon

external interface ContentEntryDetailAttemptsListSessionListItemProps: Props {

    var sessionTimeAndProgressInfo: SessionTimeAndProgressInfo?

    var onClick: () -> Unit

}

val ContentEntryDetailAttemptsListSessionListItem = FC<ContentEntryDetailAttemptsListSessionListItemProps> { props ->
    val formattedDateAndTime = props.sessionTimeAndProgressInfo?.timeStarted?.let {
        useFormattedDateAndTime(
            timeInMillis = it,
            timezoneId = TimeZone.currentSystemDefault().id
        )
    }

    val stringsXml = useStringProvider()

    val theme by useRequiredContext(ThemeContext)

    ListItem {
        ListItemButton {
            onClick =  { props.onClick() }

            props.sessionTimeAndProgressInfo?.also {
                val component = when {
                    it.isSuccessful == true-> {
                        StarIcon
                    }

                    it.isSuccessful == false -> {
                        HighlightOffOutlinedIcon
                    }

                    it.isCompleted -> {
                        CheckIcon
                    }

                    else -> {
                        Pending
                    }
                }

                ListItemIcon {
                    + component.create {
                        + props
                    }
                }

            }

            ListItemText {
                primary = ReactNode(
                    props.sessionTimeAndProgressInfo?.statementSummary?.descriptionStringRes?.let {
                        stringsXml[it]
                    } ?: ""
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

                    props.sessionTimeAndProgressInfo?.maxProgress?.also { maxProgressVal ->
                        UstadProgressBarWithLabel {
                            progressValue = maxProgressVal
                            label = ReactNode(stringsXml[MR.strings.progress_key])
                        }
                    }

                    props.sessionTimeAndProgressInfo?.maxScore?.also { maxScoreVal ->
                        UstadProgressBarWithLabel {
                            progressValue = (maxScoreVal * 100).toInt()
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
}