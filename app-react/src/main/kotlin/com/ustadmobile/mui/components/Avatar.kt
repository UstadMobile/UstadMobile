package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.Avatar
import mui.material.AvatarProps
import org.w3c.dom.HTMLImageElement
import react.RBuilder
import react.dom.html.ImgHTMLAttributes
import styled.StyledHandler

@Suppress("EnumEntryName")
enum class AvatarVariant {
    rounded, square, circle, circular
}

fun RBuilder.umAvatar(
    src: String? = null,
    alt: String? = null,
    srcSet: String? = null,
    variant: AvatarVariant = AvatarVariant.circular,
    component: String = "div",
    imgProps: ImgHTMLAttributes<HTMLImageElement>? = null,
    sizes: String? = null,
    className: String? = null,
    handler: StyledHandler<AvatarProps>? = null
) = createStyledComponent(Avatar, className, handler) {
    imgProps?.let {
        attrs.imgProps = imgProps }
    alt?.let { attrs.alt = alt }
    attrs.asDynamic().component = component
    sizes?.let { attrs.sizes = sizes }
    src?.let { attrs.src = src }
    srcSet?.let { attrs.srcSet = srcSet }
    attrs.asDynamic().variant = variant
}