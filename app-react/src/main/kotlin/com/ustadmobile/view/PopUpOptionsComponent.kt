package com.ustadmobile.view

import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.dialog.mDialog
import com.ccfraser.muirwik.components.dialog.mDialogActions
import com.ccfraser.muirwik.components.dialog.mDialogContent
import com.ccfraser.muirwik.components.list.mList
import com.ccfraser.muirwik.components.list.mListItem
import com.ccfraser.muirwik.components.list.mListItemAvatar
import com.ccfraser.muirwik.components.list.mListItemText
import com.ccfraser.muirwik.components.mAvatar
import com.ccfraser.muirwik.components.mIcon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import kotlinx.css.LinearDimension
import kotlinx.css.pc
import kotlinx.css.width
import react.*
import styled.css
import styled.styledDiv

data class PopUpOptionItem(var icon: String, var primaryText: Int, var secondaryText: Int = 0, val onOptionItemClicked: (() -> Unit)? = null)

interface OptionsProps: RProps {
    var optionItems: List<PopUpOptionItem>
    var systemImpl: UstadMobileSystemImpl
    var shownAt: Long
    var onDialogClosed: () -> Unit
}

class PopUpOptionsComponent(mProps: OptionsProps): RComponent<OptionsProps, RState>(mProps) {

    private var showDialog: Boolean = true

    private var lastShownAt = 0L

    override fun RBuilder.render() {
        mDialog(showDialog, onClose = { _, _ ->
            handleDialogClosed()
        }) {
            mDialogContent {
                css {
                    width = 25.pc
                }
                styledDiv {
                    css {
                        width = LinearDimension("100%")
                    }
                    mList {

                        props.optionItems.forEach { option ->
                            mListItem(button = true,
                                onClick = { option.onOptionItemClicked?.invoke() }
                            ) {
                                mListItemAvatar {
                                    mAvatar {
                                        mIcon(option.icon)
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
        }
    }

    private fun handleDialogClosed(){
        props.onDialogClosed.invoke()
        setState {
            showDialog = false
            lastShownAt = props.shownAt
        }
    }

    override fun componentDidUpdate(prevProps: OptionsProps, prevState: RState, snapshot: Any) {

    }

    override fun componentWillUpdate(nextProps: OptionsProps, nextState: RState) {
        showDialog = nextProps.shownAt != props.shownAt
    }
}

fun RBuilder.renderPopUpOptions(systemImpl: UstadMobileSystemImpl,
                                optionItems: List<PopUpOptionItem>,
                                shownAt: Long, onDialogClosed: () -> Unit) = child(PopUpOptionsComponent::class) {
    attrs.optionItems = optionItems
    attrs.systemImpl = systemImpl
    attrs.shownAt = shownAt
    attrs.onDialogClosed = onDialogClosed
}