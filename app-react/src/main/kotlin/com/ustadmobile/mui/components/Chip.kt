package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.Chip
import mui.material.ChipProps
import org.w3c.dom.events.Event
import react.RBuilder
import react.ReactElement
import react.ReactNode
import styled.StyledHandler


@Suppress("EnumEntryName")
enum class ChipVariant {
    default, outlined
}

@Suppress("EnumEntryName")
enum class ChipColor {
    default, primary, secondary
}

enum class ChipSize {
    small, medium
}


fun RBuilder.umChip(
    label: String,
    avatar: ReactElement? = null,
    onClick: ((Event) -> Unit)? = null,
    onDelete: ((Event) -> Unit)? = null,
    key: Any? = null,
    color: ChipColor = ChipColor.default,
    size: ChipSize = ChipSize.medium,
    variant: ChipVariant = ChipVariant.default,
    className: String? = null,
    handler: StyledHandler<ChipProps>? = null
) = createStyledComponent(Chip, className, handler) {
    avatar?.let { attrs.avatar = it }
    attrs.color = color.toString()
    attrs.asDynamic().component = "div"
    attrs.label = ReactNode(label)
    key?.let { attrs.asDynamic().key = it }
    onClick?.let { attrs.asDynamic().onClick = it }
    onDelete?.let { attrs.onDelete = it }
    attrs.size = size.toString()
    attrs.variant = variant.toString()
}
