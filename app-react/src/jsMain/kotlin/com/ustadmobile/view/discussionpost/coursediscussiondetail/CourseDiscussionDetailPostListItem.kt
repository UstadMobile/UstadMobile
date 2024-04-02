package com.ustadmobile.view.discussionpost.coursediscussiondetail

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.hooks.useDayOrDate
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.wrappers.intl.Intl
import js.objects.jso
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import web.cssom.Overflow
import web.cssom.TextOverflow
import web.cssom.WhiteSpace
import web.cssom.px
import kotlinx.datetime.TimeZone
import mui.icons.material.Chat as ChatIcon
import mui.icons.material.MoreVert as MoreVertIcon
import mui.icons.material.ReplyAll as ReplyAllIcon
import mui.material.IconButton
import mui.material.ListItem
import mui.material.ListItemAlignItems
import mui.material.ListItemButton
import mui.material.ListItemButtonAlignItems
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Menu
import mui.material.MenuItem
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
import react.ReactNode
import react.create
import react.dom.aria.AriaHasPopup
import react.dom.aria.ariaExpanded
import react.dom.aria.ariaHasPopup
import react.dom.html.ReactHTML
import react.useMemo
import react.useRequiredContext
import react.useState
import web.dom.Element

external interface CourseDiscussionDetailPostListItemProps : Props {
    var discussionPost: DiscussionPostWithDetails?
    var onClick: () -> Unit
    var onClickDelete: () -> Unit
    var showModerateOptions: Boolean
    var localDateTimeNow: LocalDateTime
    var timeFormat: Intl.Companion.DateTimeFormat
    var dateFormat: Intl.Companion.DateTimeFormat
    var dayOfWeekStrings: Map<DayOfWeek, String>
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

    val formattedTime = useDayOrDate(
        enabled = true,
        localDateTimeNow = props.localDateTimeNow,
        timestamp = props.discussionPost?.discussionPostStartDate ?: 0L,
        timeZone = TimeZone.currentSystemDefault(),
        showTimeIfToday = true,
        timeFormatter = props.timeFormat,
        dateFormatter = props.dateFormat,
        dayOfWeekStringMap = props.dayOfWeekStrings,
    )

    val theme by useRequiredContext(ThemeContext)

    val authorName = "${props.discussionPost?.authorPersonFirstNames} ${props.discussionPost?.authorPersonLastName}"

    var overflowAnchor by useState<Element?> { null }

    val overflowAnchorVal = overflowAnchor

    ListItem {
        alignItems = ListItemAlignItems.flexStart

        secondaryAction = Stack.create {
            direction = responsive(StackDirection.row)
            Typography {
                variant = TypographyVariant.caption
                +formattedTime
            }

            if(props.showModerateOptions) {
                IconButton {
                    ariaHasPopup = AriaHasPopup.`true`
                    ariaExpanded = overflowAnchorVal != null

                    onClick = {
                        overflowAnchor = if(overflowAnchor == null) {
                            it.currentTarget
                        }else {
                            null
                        }
                    }

                    MoreVertIcon()
                }

                if(overflowAnchorVal != null) {
                    Menu {
                        open = true
                        anchorEl = {
                            overflowAnchorVal
                        }

                        sx {
                            marginTop = theme.spacing(2)
                        }

                        onClose = {
                            overflowAnchor = null
                        }

                        MenuItem {
                            onClick = {
                                props.onClickDelete()
                                overflowAnchor = null
                            }

                            + strings[MR.strings.delete]
                        }
                    }
                }
            }
        }

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

            ListItemText {
                primary = ReactNode(authorName)
                secondary = Stack.create {
                    + (props.discussionPost?.discussionPostTitle ?:"")

                    Stack {
                        direction = responsive(StackDirection.row)

                        ChatIcon {
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

                        ReplyAllIcon {
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
                secondaryTypographyProps = jso {
                    component = ReactHTML.div
                }
            }
        }
    }
}