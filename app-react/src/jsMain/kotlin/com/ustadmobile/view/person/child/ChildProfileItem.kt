package com.ustadmobile.view.person.child

import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.view.components.UstadPersonAvatar
import mui.icons.material.Delete
import mui.material.IconButton
import react.*
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText

external interface ChildProfileItemProps : Props {
    var childProfile: Person
    var onClickEditChild: (Person) -> Unit
    var onClickDeleteChildProfile: (Person) -> Unit
}

val ChildProfileItem = FC<ChildProfileItemProps> { props ->
    val person = props.childProfile

    ListItem {

        ListItemButton {
            onClick = { props.onClickEditChild(person) }

            ListItemIcon {
                UstadPersonAvatar {
                    personName = person.fullName()
                }
            }

            ListItemText {
                primary = ReactNode(person.fullName())
            }


        }
        secondaryAction = IconButton.create {
            onClick = {
                props.onClickDeleteChildProfile(person)
            }
            Delete {}
        }

    }
}
