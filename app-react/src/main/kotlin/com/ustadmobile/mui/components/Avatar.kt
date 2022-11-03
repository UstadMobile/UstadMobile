package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.Avatar
import mui.material.AvatarProps
import mui.material.AvatarVariant
import dom.html.HTMLImageElement
import react.RBuilder
import react.dom.html.ImgHTMLAttributes
import styled.StyledHandler

fun RBuilder.umAvatar(
    src: String? = null,
    alt: String? = null,
    srcSet: String? = null,
    variant: AvatarVariant = AvatarVariant.circular,
    imgProps: ImgHTMLAttributes<HTMLImageElement>? = null,
    sizes: String? = null,
    className: String? = null,
    handler: StyledHandler<AvatarProps>? = null
) = convertFunctionalToClassElement(Avatar, className, handler) {
    imgProps?.let {attrs.imgProps = imgProps }
    alt?.let { attrs.alt = alt }
    sizes?.let { attrs.sizes = sizes }
    src?.let { attrs.src = src }
    srcSet?.let { attrs.srcSet = srcSet }
    attrs.variant = variant
}