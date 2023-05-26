package com.ustadmobile.view.discussionpost.coursediscussiondetail

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.view.components.UstadPersonAvatar
import csstype.Display
import csstype.JustifyContent
import csstype.Overflow
import csstype.TextOverflow
import csstype.WhiteSpace
import csstype.pct
import csstype.px
import kotlinx.datetime.TimeZone
import mui.icons.material.Chat
import mui.icons.material.ReplyAll
import mui.material.Box
import mui.material.ListItem
import mui.material.ListItemAlignItems
import mui.material.ListItemButton
import mui.material.ListItemButtonAlignItems
import mui.material.ListItemIcon
import mui.material.Stack
import mui.material.StackDirection
import mui.material.SvgIconColor
import mui.material.SvgIconSize
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.useMemo

external interface CourseDiscussionDetailPostListItemProps : Props {
    var discussionPost: DiscussionPostWithDetails?
    var onClick: () -> Unit
}

val CourseDiscussionDetailPostListItem = FC<CourseDiscussionDetailPostListItemProps> { props ->
    val plainTextDescription = useMemo(
        dependencies = arrayOf(
            props.discussionPost?.postLatestMessage,
            props.discussionPost?.discussionPostMessage
        )
    ) {
        val text = props.discussionPost?.postLatestMessage
            ?: props.discussionPost?.discussionPostMessage ?: ""
        text.htmlToPlainText()
    }
    val strings = useStringsXml()

    val formattedDateTime = useFormattedDateAndTime(
        timeInMillis = props.discussionPost?.discussionPostStartDate ?: 0L,
        timezoneId = TimeZone.currentSystemDefault().id,
    )

    ListItem {
        alignItems = ListItemAlignItems.flexStart

        ListItemButton {
            alignItems = ListItemButtonAlignItems.flexStart
            onClick = {
                props.onClick()
            }

            ListItemIcon {
                UstadPersonAvatar {
                    personUid = props.discussionPost?.discussionPostStartedPersonUid ?: 0L
                }
            }

            Stack {
                sx {
                    width = 100.pct
                }

                Box {
                    sx {
                        display = Display.flex
                        justifyContent = JustifyContent.spaceBetween
                    }

                    Typography {
                        variant = TypographyVariant.subtitle1
                        + "${props.discussionPost?.authorPersonFirstNames} ${props.discussionPost?.authorPersonLastName}"
                    }

                    Typography {
                        variant = TypographyVariant.caption

                        +formattedDateTime
                    }
                }


                Typography {
                    variant = TypographyVariant.subtitle2
                    + (props.discussionPost?.discussionPostTitle ?:"")
                }

                Stack {
                    direction = responsive(StackDirection.row)


                    Chat {
                        color = SvgIconColor.action
                        fontSize = SvgIconSize.small
                    }

                    Typography {
                        variant = TypographyVariant.body2
                        sx {
                            whiteSpace = WhiteSpace.nowrap
                            overflow = Overflow.hidden
                            textOverflow = TextOverflow.ellipsis
                            maxWidth = 480.px
                        }

                        +plainTextDescription
                    }
                }

                Stack {
                    direction = responsive(StackDirection.row)

                    ReplyAll {
                        color = SvgIconColor.action
                        fontSize = SvgIconSize.small
                    }

                    Typography {
                        variant = TypographyVariant.body2
                        +(strings[MessageID.num_replies].replace("%1\$d",
                            props.discussionPost?.postRepliesCount?.toString() ?: "0"))
                    }
                }
            }
        }
    }
}