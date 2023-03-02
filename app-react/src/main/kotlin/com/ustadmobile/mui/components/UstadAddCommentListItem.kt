package com.ustadmobile.mui.components

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import js.core.jso
import mui.icons.material.AccountCircle
import mui.icons.material.Send
import mui.material.*
import react.FC
import react.Props
import react.create
import react.dom.aria.ariaLabel
import react.dom.onChange
import react.useState

external interface UstadAddCommentListItemProps: Props {

    var text: String

    var placeholder: String

    var enabled: Boolean?

    var personUid: Long?

    var id: String

    var onChange: (String) -> Unit

    var onClickSubmit: () -> Unit

}

val UstadAddCommentListItem = FC<UstadAddCommentListItemProps> { props ->

    val strings = useStringsXml()

    ListItem {

        ListItemIcon {
            + AccountCircle.create()
        }

        TextField {
            fullWidth = true
            id = props.id
            onKeyUp = {
                if(it.key == "Enter") {
                    props.onClickSubmit()
                }
            }
            onChange = {
                val currentVal = it.target.asDynamic().value
                props.onChange(currentVal?.toString() ?: "")
            }
            placeholder = props.placeholder

            if(props.text.isNotBlank()) {
                asDynamic().InputProps = jso<InputBaseProps> {
                    endAdornment = InputAdornment.create {
                        position = InputAdornmentPosition.end

                        IconButton {
                            ariaLabel =  strings[MessageID.submit]
                            onClick = {
                                props.onClickSubmit()
                            }

                            Send { }
                        }
                    }
                }
            }
        }
    }
}

val UstadAddCommentListItemPreview = FC<Props> {

    var commentText by useState { "" }

    UstadAddCommentListItem {
        text = commentText
        enabled = true
        personUid = 0
        onClickSubmit = {}
        placeholder = "Add a comment"
        id = "addcomment"
        onChange = {
            commentText = it
        }
    }
}