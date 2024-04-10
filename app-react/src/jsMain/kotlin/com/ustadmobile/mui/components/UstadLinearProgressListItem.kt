package com.ustadmobile.mui.components

import com.ustadmobile.core.hooks.useStringProvider
import mui.material.ListItem
import mui.material.ListItemText
import react.FC
import react.Props
import react.ReactNode
import com.ustadmobile.core.MR
import js.objects.jso
import mui.material.LinearProgress
import mui.material.LinearProgressVariant
import react.create
import react.dom.html.ReactHTML.div


external interface UstadLinearProgressListItemProps: Props {
    var progress: Float?
    var secondaryContent: ReactNode
    var onCancel: () -> Unit
    var error: String?
    var onDismissError: () -> Unit
}

val UstadLinearProgressListItem = FC<UstadLinearProgressListItemProps> {props ->
    val strings = useStringProvider()
    val errorVal = props.error
    val progressVal = props.progress

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

    }
}



