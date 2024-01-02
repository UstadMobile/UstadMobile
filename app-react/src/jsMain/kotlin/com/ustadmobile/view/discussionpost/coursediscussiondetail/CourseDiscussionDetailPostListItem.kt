package com.ustadmobile.view.discussionpost.coursediscussiondetail

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.view.components.UstadPersonAvatar
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.Overflow
import web.cssom.TextOverflow
import web.cssom.WhiteSpace
import web.cssom.pct
import web.cssom.px
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
    val strings = useStringProvider()

    val formattedDateTime = useFormattedDateAndTime(
        timeInMillis = props.discussionPost?.discussionPostStartDate ?: 0L,
        timezoneId = TimeZone.currentSystemDefault().id,
    )

    val authorName = "${props.discussionPost?.authorPersonFirstNames} ${props.discussionPost?.authorPersonLastName}"

    ListItem {
        alignItems = ListItemAlignItems.flexStart

        ListItemButton {
            alignItems = ListItemButtonAlignItems.flexStart
            onClick = {
                props.onClick()
            }

            ListItemIcon {
                UstadPersonAvatar {
                    personName = authorName
                    pictureUri = props.discussionPost?.authorPictureUri
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
                        + authorName
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
                        +strings.format(MR.strings.num_replies,
                            props.discussionPost?.postRepliesCount ?: 0)
                    }
                }
            }
        }
    }
}