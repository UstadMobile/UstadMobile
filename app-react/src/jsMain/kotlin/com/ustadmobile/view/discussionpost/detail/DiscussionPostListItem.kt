package com.ustadmobile.view.discussionpost.detail

import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.composites.DiscussionPostAndPosterNames
import com.ustadmobile.mui.components.UstadRawHtml
import mui.material.ListItem
import mui.material.ListItemIcon
import react.FC
import react.Props
import com.ustadmobile.view.components.UstadPersonAvatar
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.pct
import kotlinx.datetime.TimeZone
import mui.material.Box
import mui.material.ListItemAlignItems
import mui.material.Stack
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx

external interface DiscussionPostListItemProps: Props {
    var discussionPost: DiscussionPostAndPosterNames?
}

val DiscussionPostListItem = FC<DiscussionPostListItemProps> { props ->
    val formattedDateTime = useFormattedDateAndTime(
        timeInMillis = props.discussionPost?.discussionPost?.discussionPostStartDate ?: 0L,
        timezoneId = TimeZone.currentSystemDefault().id,
    )
    val posterName = "${props.discussionPost?.firstNames ?: ""} ${props.discussionPost?.lastName ?: ""}"

    ListItem {
        alignItems = ListItemAlignItems.flexStart

        ListItemIcon {
            UstadPersonAvatar {
                personName = posterName
                pictureUri = props.discussionPost?.personPictureUri
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
                    width = 100.pct
                }

                Typography {
                    variant = TypographyVariant.subtitle1

                    + posterName
                }

                Typography {
                    variant = TypographyVariant.caption

                    +formattedDateTime
                }
            }


            Typography {
                variant = TypographyVariant.body2

                UstadRawHtml {
                    html = props.discussionPost?.discussionPost?.discussionPostMessage ?: ""
                }
            }
        }

    }
}
