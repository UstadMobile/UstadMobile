package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.AvatarVariant
import mui.material.ListItemAvatar
import mui.material.ListItemAvatarProps
import org.w3c.dom.HTMLImageElement
import react.RBuilder
import react.dom.html.ImgHTMLAttributes
import styled.StyledHandler

/**
 * Simplest version of the list item avatar
 */
fun RBuilder.umListItemAvatar(
    className: String? = null,
    handler: StyledHandler<ListItemAvatarProps>? = null
) = createStyledComponent(ListItemAvatar, className, handler)

fun RBuilder.umListItemAvatar(
    src: String? = null,
    alt: String? = null,
    srcSet: String? = null,
    variant: AvatarVariant = AvatarVariant.circular,
    imgProps: ImgHTMLAttributes<HTMLImageElement>? = null,
    sizes: String? = null,
    className: String? = null,
    handler: StyledHandler<ListItemAvatarProps>? = null
) = createStyledComponent(ListItemAvatar, className, handler) {
    umAvatar(src, srcSet, alt, variant, imgProps, sizes)
}