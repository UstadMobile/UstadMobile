package com.ustadmobile.mui.components

import com.ustadmobile.core.hooks.useStringProvider
import mui.material.ListItem
import mui.material.ListItemText
import react.FC
import react.Props
import react.ReactNode
import com.ustadmobile.core.MR
import js.objects.jso
import mui.material.IconButton
import mui.material.LinearProgress
import mui.material.LinearProgressVariant
import mui.material.Tooltip
import react.create
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.div
import mui.icons.material.Close as CloseIcon

external interface UstadLinearProgressListItemProps: Props {
    var progress: Float?
    var secondaryContent: ReactNode
    var onCancel: (() -> Unit)?
    var error: String?
    var onDismissError: (() -> Unit)?
}

val UstadLinearProgressListItem = FC<UstadLinearProgressListItemProps> {props ->
    val strings = useStringProvider()
    val errorVal = props.error
    val progressVal = props.progress
    val onCancelVal = props.onCancel
    val onDismissErrorVal = props.onDismissError

    ListItem {
        if(errorVal != null) {
            ListItemText {
                primary = ReactNode(strings[MR.strings.import_error])
                secondary = ReactNode(errorVal)
            }
        }else {
            ListItemText {
                primary = LinearProgress.create {
                    if(progressVal != null) {
                        variant = LinearProgressVariant.determinate
                        this.value = progressVal * 100
                    }else {
                        variant = LinearProgressVariant.indeterminate
                    }
                }
                secondary = props.secondaryContent

                primaryTypographyProps = jso {
                    component = div
                }
                secondaryTypographyProps = jso {
                    component = div
                }
            }

        }

        val showSecondaryAction = (errorVal != null && onDismissErrorVal != null) ||
                onDismissErrorVal != null

        if(showSecondaryAction) {
            secondaryAction = Tooltip.create {
                title = ReactNode(strings[MR.strings.cancel])

                IconButton {
                    ariaLabel = strings[MR.strings.cancel]

                    onClick = {
                        if(errorVal != null && onDismissErrorVal != null) {
                            onDismissErrorVal()
                        }else if(onCancelVal != null) {
                            onCancelVal()
                        }
                    }

                    CloseIcon()
                }
            }
        }
    }
}



