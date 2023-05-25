package com.ustadmobile.view.discussionpost.detail

import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.composites.DiscussionPostAndPosterNames
import com.ustadmobile.mui.components.UstadRawHtml
import mui.material.ListItem
import mui.material.ListItemIcon
import react.FC
import react.Props
import com.ustadmobile.view.components.UstadPersonAvatar
import kotlinx.datetime.TimeZone
import mui.material.ListItemSecondaryAction
import mui.material.Stack
import mui.material.Typography
import mui.material.styles.TypographyVariant

external interface DiscussionPostListItemProps: Props {
    var discussionPost: DiscussionPostAndPosterNames?
}

val DiscussionPostListItem = FC<DiscussionPostListItemProps> { props ->
    val formattedDateTime = useFormattedDateAndTime(
        timeInMillis = props.discussionPost?.discussionPost?.discussionPostStartDate ?: 0L,
        timezoneId = TimeZone.currentSystemDefault().id,
    )

    ListItem {
        ListItemIcon {
            UstadPersonAvatar {
                personUid = props.discussionPost?.discussionPost?.discussionPostStartedPersonUid ?: 0L
            }
        }

        Stack {
            Typography {
                variant = TypographyVariant.subtitle1

                + ((props.discussionPost?.firstNames ?: "") + " " + (props.discussionPost?.lastName))
            }

            Typography {
                variant = TypographyVariant.body2

                UstadRawHtml {
                    html = props.discussionPost?.discussionPost?.discussionPostMessage ?: ""
                }
            }
        }

        ListItemSecondaryAction {
            + formattedDateTime
        }
    }
}
