package com.ustadmobile.mui.components

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.UstadMarksPersonListItemUiState
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.hooks.useFormattedTime
import com.ustadmobile.lib.db.entities.CourseAssignmentMarkWithPersonMarker
import com.ustadmobile.lib.db.entities.Person
import mui.icons.material.AccountCircle
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface UstadMarksPersonListItemProps : Props {

    var uiState: UstadMarksPersonListItemUiState

    var onClickMark: (CourseAssignmentMarkWithPersonMarker) -> Unit

}

val UstadMarksPersonListItem = FC<UstadMarksPersonListItemProps> { props ->

    val strings = useStringsXml()

    val markUiSate = props.uiState.mark.listItemUiState
    var text = props.uiState.mark.marker?.fullName() ?: ""

    if (markUiSate.markerGroupNameVisible){
        text += "  (${strings[MessageID.group_number]
            .replace("%1\$s", props.uiState.mark.camMarkerSubmitterUid.toString())})"
    }

    val formattedTime = useFormattedTime(props.uiState.mark.camLct.toInt())

    ListItem{
        ListItemButton {
            onClick = {
                props.onClickMark(props.uiState.mark)
            }

            ListItemIcon {
                + AccountCircle.create()
            }

            ListItemText {
                primary = ReactNode(text)
                secondary = Stack.create {
                    direction = responsive(StackDirection.column)

                    Typography {
                        + ("")
                    }
                }
            }
        }
        secondaryAction = Typography.create {
            + formattedTime
        }
    }
}

val UstadMarksPersonListItemPreview = FC<Props> {

    UstadMarksPersonListItem {
        uiState = UstadMarksPersonListItemUiState(
            mark = CourseAssignmentMarkWithPersonMarker().apply {
                marker = Person().apply {
                    firstNames = "John"
                    lastName = "Smith"
                    isGroup = true
                    camMarkerSubmitterUid = 2
                    camMarkerComment = "Comment"
                    camPenalty = 3
                }
            }
        )
    }
}