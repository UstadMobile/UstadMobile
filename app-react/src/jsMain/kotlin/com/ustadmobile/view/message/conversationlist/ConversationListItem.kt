package com.ustadmobile.view.message.conversationlist

import com.ustadmobile.core.viewmodel.message.conversationlist.ConversationListUiState
import com.ustadmobile.hooks.useDayOrDate
import com.ustadmobile.lib.db.composites.MessageAndOtherPerson
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.wrappers.intl.Intl
import emotion.react.css
import kotlinx.datetime.TimeZone
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.styles.TypographyVariant
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML
import web.cssom.Display
import web.cssom.Overflow
import web.cssom.TextOverflow
import web.cssom.WhiteSpace
import web.cssom.pct


external interface ConversationListItemProps: Props {

    var message: MessageAndOtherPerson?

    var timeFormatter: Intl.Companion.DateTimeFormat

    var dateFormatter: Intl.Companion.DateTimeFormat

    var uiState: ConversationListUiState

    var onListItemClick: (MessageAndOtherPerson) -> Unit

}

val ConversationListItem = FC<ConversationListItemProps> { props ->

    val formattedTime = useDayOrDate(
        enabled = true,
        localDateTimeNow = props.uiState.localDateTimeNow,
        timestamp = props.message?.message?.messageTimestamp ?: 0,
        timeZone = TimeZone.currentSystemDefault(),
        showTimeIfToday = true,
        timeFormatter = props.timeFormatter,
        dateFormatter = props.dateFormatter,
        dayOfWeekStringMap = props.uiState.dayOfWeekStrings,
    )

    ListItem {
        ListItemButton{
            onClick = {
                props.message?.also { props.onListItemClick(it) }
            }

            ListItemIcon {
                UstadPersonAvatar {
                    pictureUri = props.message?.personPicture?.personPictureThumbnailUri
                    personName = props.message?.otherPerson?.fullName()
                }
            }

            ListItemText {
                primary = ReactNode("${props.message?.otherPerson?.fullName()}")
                secondary = ReactHTML.span.create {
                    css {
                        display = Display.inlineBlock
                        whiteSpace = WhiteSpace.nowrap
                        overflow = Overflow.hidden
                        width = 100.pct
                        textOverflow = TextOverflow.ellipsis
                    }
                    + "${props.message?.message?.messageText}"
                }
            }
        }
        secondaryAction = mui.material.Typography.create {
            variant = TypographyVariant.caption

            + formattedTime
        }
    }
}
