package com.ustadmobile.view.clazzassignment

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.mui.components.UstadSendTextField
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.UstadPersonAvatar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import mui.material.ListItem
import mui.material.ListItemIcon
import react.FC
import react.Props
import react.ReactNode

external interface AssignmentCommentTextFieldListItemProps: Props {
    var onChange: (String) -> Unit
    var label: ReactNode
    var value: Flow<String>
    var activeUserPersonUid: Long
    var activeUserPersonName: String
    var activeUserPictureUri: String?
    var onClickSubmit: () -> Unit
    var textFieldId: String
}

val AssignmentCommentTextFieldListItem = FC<AssignmentCommentTextFieldListItemProps> { props ->
    val textValue by props.value.collectAsState(
        "", Dispatchers.Main.immediate
    )

    ListItem {
        ListItemIcon {
            UstadPersonAvatar {
                personName = props.activeUserPersonName
                pictureUri = props.activeUserPictureUri
            }
        }


        UstadSendTextField {
            label = props.label
            fullWidth = true
            value = textValue
            onTextChange = props.onChange
            id = props.textFieldId
            onClickSend = props.onClickSubmit
        }
    }
}


