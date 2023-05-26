package com.ustadmobile.view.clazzassignment

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.UstadPersonAvatar
import js.core.jso
import mui.material.IconButton
import mui.material.InputAdornment
import mui.material.InputAdornmentPosition
import mui.material.InputBaseProps
import mui.material.ListItemIcon
import mui.material.ListItem
import react.FC
import mui.material.TextField
import react.Props
import react.ReactNode
import react.create
import react.dom.aria.ariaLabel
import mui.icons.material.Send as SendIcon

external interface AssignmentCommentTextFieldListItemProps: Props {
    var onChange: (String) -> Unit
    var label: ReactNode
    var value: String
    var activeUserPersonUid: Long
    var onClickSubmit: () -> Unit
    var textFieldId: String
}

val AssignmentCommentTextFieldListItem = FC<AssignmentCommentTextFieldListItemProps> { props ->
    val strings = useStringsXml()
    ListItem {
        ListItemIcon {
            UstadPersonAvatar {
                personUid = props.activeUserPersonUid
            }
        }


        TextField {
            label = props.label
            fullWidth = true
            value = props.value
            onTextChange = props.onChange
            id = props.textFieldId

            if(props.value.isNotEmpty()) {
                asDynamic().InputProps = jso<InputBaseProps> {
                    endAdornment = InputAdornment.create {
                        position = InputAdornmentPosition.end
                        IconButton {
                            onClick = {
                                props.onClickSubmit()
                            }
                            ariaLabel = strings[MessageID.send]
                            SendIcon {  }
                        }
                    }
                }
            }
        }
    }
}


