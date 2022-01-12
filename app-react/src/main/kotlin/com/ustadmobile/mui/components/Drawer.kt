package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import csstype.TransitionDuration
import mui.material.*
import org.w3c.dom.events.Event
import react.RBuilder
import styled.StyledHandler

@Suppress("EnumEntryName")
enum class DrawerAnchor {
    left, top, right, bottom
}

@Suppress("EnumEntryName")
enum class DrawerVariant {
    permanent, temporary, persistent
}

fun RBuilder.umDrawer(
    open: Boolean = false,
    anchor: DrawerAnchor = DrawerAnchor.left,
    variant: DrawerVariant = DrawerVariant.temporary,
    onClose: ((Event) -> Unit)? = null,
    elevation: Int = 16,
    modalProps: ModalProps? = null,
    paperProps: PaperProps? = null,
    slideProps: SlideProps? = null,
    transitionDuration: TransitionDuration? = null,

    className: String? = null,
    handler: StyledHandler<DrawerProps>
) = createStyledComponent(Drawer, className, handler) {
    attrs.anchor = anchor.toString()
    attrs.elevation = elevation
    modalProps?.let { attrs.ModalProps = it }
    onClose?.let { attrs.onClose = it }
    attrs.open = open
    paperProps?.let { attrs.PaperProps = it }
    slideProps?.let { attrs.SlideProps = it }
    attrs.variant = variant.toString()
    transitionDuration?.let { attrs.transitionDuration = it }
}