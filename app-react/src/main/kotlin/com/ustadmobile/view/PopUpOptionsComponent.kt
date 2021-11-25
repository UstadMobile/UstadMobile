package com.ustadmobile.view

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import react.*
import kotlin.js.Date
import com.ustadmobile.util.*

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
/*        mDialog(showDialog, onClose = { _, _ ->
            handleDialogClosed()
        },fullWidth = true, maxWidth = Breakpoint.sm) {
            if(props.title != null){
                mDialogTitle("${props.title}")
            }
            mDialogContent {
                css {
                    width = LinearDimension("100%")
                }
                styledDiv {
                    css {
                        width = LinearDimension("100%")
                    }

                    mList {
                        props.optionItems.forEach { option ->
                            mListItem(
                                button = true,
                                onClick = { option.onOptionItemClicked?.com.ustadmobile.components.theming.invoke() }) {
                                if(option.icon != null){
                                    mListItemAvatar {
                                        mAvatar {
                                            mIcon("${option.icon}")
                                        }
                                    }
                                }

                                mListItemText(
                                    primary = props.systemImpl.getString(option.primaryText, this),
                                    secondary = if(option.secondaryText != 0)
                                        props.systemImpl.getString(option.secondaryText, this) else "")
                            }
                        }
                    }
                }
            }

            mDialogActions {
                mButton(props.systemImpl.getString(MessageID.cancel, this), color = MColor.primary,
                    onClick = {
                       handleDialogClosed()
                    }
                )
            }
        }*/
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