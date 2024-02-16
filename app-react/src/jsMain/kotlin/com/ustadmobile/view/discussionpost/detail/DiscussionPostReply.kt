package com.ustadmobile.view.discussionpost.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.mui.components.StatefulReactQuill
import web.cssom.px
import mui.material.Button
import mui.material.ButtonVariant
import mui.system.sx
import react.FC
import react.Props

external interface DiscussionPostReplyProps: Props {

    var reply: String

    var onClickPostReplyButton: () -> Unit

    var onReplyChanged: (String) -> Unit

    var disabled: Boolean

}

val DiscussionPostReply = FC<DiscussionPostReplyProps> { props ->
    val strings = useStringProvider()

    StatefulReactQuill {
        id = "discussion_reply"
        onChange = props.onReplyChanged
        value = props.reply
        placeholder = strings[MR.strings.add_a_reply]
        readOnly = props.disabled
    }

    Button {
        sx {
            marginTop = 12.px
            marginBottom = 12.px
        }
        onClick = {
            props.onClickPostReplyButton()
        }

        fullWidth = true
        variant = ButtonVariant.outlined
        disabled = (props.reply.isEmpty() || props.disabled)

        +strings[MR.strings.post]
    }
}
