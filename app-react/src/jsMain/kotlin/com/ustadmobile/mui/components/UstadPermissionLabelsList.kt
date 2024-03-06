package com.ustadmobile.mui.components

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.ext.hasFlag
import dev.icerock.moko.resources.StringResource
import react.FC
import react.Props
import mui.material.List
import mui.material.ListItem
import mui.material.ListItemIcon
import react.dom.aria.ariaLabel
import mui.icons.material.Check as CheckIcon
import mui.icons.material.Close as CloseIcon
import com.ustadmobile.core.MR
import mui.material.ListItemText
import react.ReactNode

external interface UstadPermissionLabelsListProps: Props {

    var permissionLabels: List<Pair<StringResource, Long>>

    var value: Long

}

val UstadPermissionLabelsList = FC<UstadPermissionLabelsListProps> { props ->
    val strings = useStringProvider()

    List {
        props.permissionLabels.forEach {
            val permissionEnabled = props.value.hasFlag(it.second)

            ListItem {
                ListItemIcon {
                    if(permissionEnabled) {
                        CheckIcon {
                            ariaLabel = strings[MR.strings.enabled]
                        }
                    }else {
                        CloseIcon {
                            ariaLabel = strings[MR.strings.disabled]
                        }
                    }
                }

                ListItemText {
                    primary = ReactNode(strings[it.first])
                }
            }
        }
    }
}
