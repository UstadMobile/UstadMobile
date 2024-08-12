package com.ustadmobile.mui.components

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.ext.filterByFlags
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.view.components.UstadPersonAvatar
import dev.icerock.moko.resources.StringResource
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Tooltip
import react.FC
import react.Props
import react.ReactNode
import react.useMemo
import mui.icons.material.Group as GroupIcon
import com.ustadmobile.core.MR
import com.ustadmobile.util.ext.useLineClamp
import emotion.react.css
import js.objects.jso
import mui.material.IconButton
import react.create
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.div
import mui.icons.material.Delete as DeleteIcon

external interface UstadPermissionListItemProps: Props {

    var value: Long

    var permissionLabels: List<Pair<StringResource, Long>>

    var primary: ReactNode

    var toPerson: Person?

    var toPersonPicture: PersonPicture?

    var onClick: () -> Unit

    var showDelete: Boolean

    var onClickDelete: () -> Unit

}

val UstadPermissionListItem = FC<UstadPermissionListItemProps> { props ->
    val strings = useStringProvider()
    val toPersonVal = props.toPerson

    val permissionStr = useMemo(props.value, props.permissionLabels) {
        props.permissionLabels.filterByFlags(props.value).joinToString {
            strings[it.first]
        }
    }

    ListItem {
        if(props.showDelete) {
            secondaryAction = Tooltip.create {
                title = ReactNode(strings[MR.strings.delete])

                IconButton {
                    ariaLabel = strings[MR.strings.delete]
                    onClick = {
                        props.onClickDelete()
                    }

                    DeleteIcon()
                }
            }
        }

        ListItemButton {
            onClick = {
                props.onClick()
            }

            ListItemIcon {
                if(toPersonVal != null) {
                    UstadPersonAvatar {
                        personName = toPersonVal.fullName()
                        pictureUri = props.toPersonPicture?.personPictureThumbnailUri
                    }
                }else {
                    GroupIcon()
                }
            }

            ListItemText {
                primary = props.primary
                secondary = div.create {
                    css {
                       useLineClamp(2)
                    }

                    + permissionStr
                }

                secondaryTypographyProps = jso {
                    component = div
                }
            }
        }
    }
}
