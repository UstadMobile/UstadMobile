package com.ustadmobile.view.discussionpost.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useDayOrDate
import com.ustadmobile.lib.db.composites.DiscussionPostAndPosterNames
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadRawHtml
import mui.material.ListItem
import mui.material.ListItemIcon
import react.FC
import react.Props
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.wrappers.intl.Intl
import js.objects.jso
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import mui.icons.material.MoreVert
import mui.material.IconButton
import mui.material.ListItemAlignItems
import mui.material.ListItemText
import mui.material.Menu
import mui.material.MenuItem
import mui.material.Stack
import mui.material.StackDirection
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.ReactNode
import react.create
import react.dom.aria.AriaHasPopup
import react.dom.aria.ariaExpanded
import react.dom.aria.ariaHasPopup
import react.dom.html.ReactHTML
import react.useRequiredContext
import react.useState
import web.dom.Element

external interface DiscussionPostListItemProps: Props {
    var discussionPost: DiscussionPostAndPosterNames?
    var onClickDelete: () -> Unit
    var showModerateOptions: Boolean
    var localDateTimeNow: LocalDateTime
    var timeFormat: Intl.Companion.DateTimeFormat
    var dateFormat: Intl.Companion.DateTimeFormat
    var dayOfWeekStrings: Map<DayOfWeek, String>
}

val DiscussionPostListItem = FC<DiscussionPostListItemProps> { props ->
    val posterName = "${props.discussionPost?.firstNames ?: ""} ${props.discussionPost?.lastName ?: ""}"

    var overflowAnchor by useState<Element?> { null }

    val overflowAnchorVal = overflowAnchor

    val theme by useRequiredContext(ThemeContext)

    val strings = useStringProvider()

    val dayOrDate = useDayOrDate(
        enabled = true,
        localDateTimeNow = props.localDateTimeNow,
        timestamp = props.discussionPost?.discussionPost?.discussionPostStartDate ?: 0L,
        timeZone = TimeZone.currentSystemDefault(),
        showTimeIfToday = true,
        timeFormatter = props.timeFormat,
        dateFormatter = props.dateFormat,
        dayOfWeekStringMap = props.dayOfWeekStrings,
    )

    ListItem {
        alignItems = ListItemAlignItems.flexStart
        secondaryAction = Stack.create {
            direction = responsive(StackDirection.row)

            Typography {
                variant = TypographyVariant.caption
                +dayOrDate
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

                    MoreVert()
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

        ListItemIcon {
            UstadPersonAvatar {
                personName = posterName
                pictureUri = props.discussionPost?.personPictureUri
            }
        }

        ListItemText {
            primary = ReactNode(posterName)
            secondary = Typography.create {
                variant = TypographyVariant.body2

                UstadRawHtml {
                    html = props.discussionPost?.discussionPost?.discussionPostMessage ?: ""
                }
            }
            secondaryTypographyProps = jso {
                component = ReactHTML.div
            }
        }
    }
}
