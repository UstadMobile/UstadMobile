package com.ustadmobile.view.message.messagelist

import com.ustadmobile.core.viewmodel.message.daysUntil
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListUiState
import com.ustadmobile.hooks.useDayOrDate
import com.ustadmobile.hooks.useFormattedTimeForDate
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadLinkify
import com.ustadmobile.wrappers.intl.Intl
import kotlinx.datetime.TimeZone
import mui.material.Box
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useMemo
import react.useRequiredContext
import web.cssom.Display
import web.cssom.Flex
import web.cssom.JustifyContent
import web.cssom.TextAlign
import web.cssom.px


external interface MessageListItemProps: Props {

    var message: Message?

    var previousMessage: Message?

    var activeUserUid: Long

    var timeFormatter: Intl.Companion.DateTimeFormat

    var listUiState: MessageListUiState

    var dateFormatter: Intl.Companion.DateTimeFormat

}

val MessageListItem = FC<MessageListItemProps> { props ->

    val theme by useRequiredContext(ThemeContext)
    val isFromMe = props.message?.messageSenderPersonUid == props.activeUserUid
    val messageTime = useFormattedTimeForDate(props.message?.messageTimestamp ?: 0, props.timeFormatter)

    val daysSincePrevMessage = useMemo(
        arrayOf(props.message?.messageTimestamp, props.previousMessage?.messageTimestamp)
    ) {
        props.message?.let { props.previousMessage?.daysUntil(it) }
    }

    val dayOrDateHeader = useDayOrDate(
        enabled = daysSincePrevMessage != 0,
        localDateTimeNow = props.listUiState.localDateTimeNow,
        timestamp = props.message?.messageTimestamp ?: 0,
        timeZone = TimeZone.currentSystemDefault(),
        dateFormatter = props.dateFormatter,
        showTimeIfToday = true,
        timeFormatter = props.timeFormatter,
        dayOfWeekStringMap = props.listUiState.dayOfWeekStrings,
    )

    dayOrDateHeader?.also {
        Typography {
            sx {
                display = Display.block
                textAlign = TextAlign.center
            }

            + it
        }
    }

    Box {
        sx {
            display = Display.flex
            flex = Flex.minContent
            justifyContent = if (isFromMe) JustifyContent.end else JustifyContent.start
        }

        Box {
            sx {
                backgroundColor = if(isFromMe) {
                    theme.palette.primary.light
                }else {
                    theme.palette.primary.dark
                }
                color = theme.palette.primary.contrastText
                padding = 10.px
                margin = 5.px
                borderTopLeftRadius = 24.px
                borderTopRightRadius = 24.px
                borderBottomLeftRadius = if (isFromMe) 24.px else 0.px
                borderBottomRightRadius = if (isFromMe) 0.px else 24.px
            }

            UstadLinkify {
                + (props.message?.messageText ?: "")
            }

            ReactHTML.br()

            Typography {
                variant = TypographyVariant.caption

                + messageTime
            }
        }


    }
}