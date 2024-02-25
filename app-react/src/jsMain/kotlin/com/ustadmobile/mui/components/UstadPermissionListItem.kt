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
import react.FC
import react.Props
import react.ReactNode
import react.useMemo
import mui.icons.material.Group as GroupIcon
external interface UstadPermissionListItemProps: Props {

    var value: Long

    var permissionLabels: List<Pair<StringResource, Long>>

    var primary: ReactNode

    var toPerson: Person?

    var toPersonPicture: PersonPicture?

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
        ListItemButton {
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
                secondary = ReactNode(permissionStr)
            }
        }
    }
}
