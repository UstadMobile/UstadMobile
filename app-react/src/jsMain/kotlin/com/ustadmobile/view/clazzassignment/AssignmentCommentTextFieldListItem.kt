package com.ustadmobile.view.clazzassignment

import com.ustadmobile.mui.components.UstadSendTextField
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.UstadPersonAvatar
import mui.material.ListItem
import mui.material.ListItemIcon
import react.FC
import react.Props
import react.ReactNode

external interface AssignmentCommentTextFieldListItemProps: Props {
    var onChange: (String) -> Unit
    var label: ReactNode
    var value: String
    var activeUserPersonUid: Long
    var onClickSubmit: () -> Unit
    var textFieldId: String
}

val AssignmentCommentTextFieldListItem = FC<AssignmentCommentTextFieldListItemProps> { props ->
    ListItem {
        ListItemIcon {
            UstadPersonAvatar {
                personUid = props.activeUserPersonUid
            }
        }


        UstadSendTextField {
            label = props.label
            fullWidth = true
            value = props.value
            onTextChange = props.onChange
            id = props.textFieldId
            onClickSend = props.onClickSubmit
        }
    }
}


