package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import csstype.TransitionDuration
import mui.material.*
import org.w3c.dom.events.Event
import react.RBuilder
import styled.StyledHandler

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
) = convertFunctionalToClassElement(Drawer, className, handler) {
    attrs.anchor = anchor
    attrs.elevation = elevation
    modalProps?.let { attrs.ModalProps = it }
    attrs.onClose = { event, _ -> onClose?.invoke(event)}
    attrs.open = open
    paperProps?.let { attrs.PaperProps = it }
    slideProps?.let { attrs.SlideProps = it }
    attrs.variant = variant
    transitionDuration?.let { attrs.transitionDuration = it }
}