package com.ustadmobile.view

import Breakpoint
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import kotlinx.css.LinearDimension
import kotlinx.css.width
import react.RBuilder
import react.RComponent
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

data class PopUpOptionItem(var icon: String?, var primaryText: Int, var secondaryText: Int = 0, val onOptionItemClicked: (() -> Unit)? = null)

interface OptionsProps: UmProps {
    var optionItems: List<PopUpOptionItem>
    var systemImpl: UstadMobileSystemImpl
    var shownAt: Long
    var title: String?
    var onDialogClosed: () -> Unit
}

class PopUpOptionsComponent(mProps: OptionsProps): RComponent<OptionsProps, UmState>(mProps) {

    private var showDialog: Boolean = true

    private var lastShownAt = 0L

    override fun RBuilder.render() {
        umDialog(showDialog,
            onClose = {handleDialogClosed()},
            fullWidth = true,
            maxWidth = Breakpoint.sm) {
            if(props.title != null){
                umDialogTitle("${props.title}")
            }
            umDialogContent {
                css {
                    width = LinearDimension("100%")
                }
                styledDiv {
                    css {
                        width = LinearDimension("100%")
                    }

                    umList {
                        props.optionItems.forEach { option ->
                            umListItem(
                                button = true,
                                onClick = {
                                    option.onOptionItemClicked?.invoke()
                                }) {
                                if(option.icon != null){
                                    umListItemAvatar {
                                        umAvatar {
                                            umIcon("${option.icon}")
                                        }
                                    }
                                }

                                umListItemText(
                                    primary = props.systemImpl.getString(option.primaryText, this),
                                    secondary = if(option.secondaryText != 0)
                                        props.systemImpl.getString(option.secondaryText, this) else "")
                            }
                        }
                    }
                }
            }

            umDialogActions {
                umButton(
                    props.systemImpl.getString(MessageID.cancel, this),
                    color = UMColor.secondary,
                    onClick = {
                       handleDialogClosed()
                    }
                )
            }
        }
    }

    private fun handleDialogClosed(){
        props.onDialogClosed.invoke()
        setState {
            showDialog = false
            lastShownAt = props.shownAt
        }
    }

    override fun componentDidUpdate(prevProps: OptionsProps, prevState: UmState, snapshot: Any) {

    }

    override fun componentWillUpdate(nextProps: OptionsProps, nextState: UmState) {
        showDialog = nextProps.shownAt != props.shownAt
    }
}

fun RBuilder.renderChoices(systemImpl: UstadMobileSystemImpl,
                           optionItems: List<PopUpOptionItem>,
                           shownAt: Long = Date().getTime().toLong(),
                           title: String? = null,
                           onDialogClosed: () -> Unit) = child(PopUpOptionsComponent::class) {
    attrs.optionItems = optionItems
    attrs.systemImpl = systemImpl
    attrs.shownAt = shownAt
    attrs.onDialogClosed = onDialogClosed
    attrs.title = title
}