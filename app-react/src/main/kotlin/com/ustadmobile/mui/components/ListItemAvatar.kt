package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createReUsableComponent
import mui.material.ListItemAvatar
import mui.material.ListItemAvatarProps
import react.RBuilder
import styled.StyledHandler

/**
 * Simplest version of the list item avatar
 */
fun RBuilder.umListItemAvatar(
    className: String? = null,
    handler: StyledHandler<ListItemAvatarProps>? = null
) = createReUsableComponent(ListItemAvatar, className, handler)