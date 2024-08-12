package com.ustadmobile.view.discussionpost.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.wrappers.quill.ReactQuill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import web.cssom.px
import mui.material.Button
import mui.material.ButtonVariant
import mui.system.sx
import react.FC
import react.Props

external interface DiscussionPostReplyProps: Props {

    var reply: Flow<String>

    var onClickPostReplyButton: () -> Unit

    var onReplyChanged: (String) -> Unit

    var disabled: Boolean

}

val DiscussionPostReply = FC<DiscussionPostReplyProps> { props ->
    val strings = useStringProvider()

    val replyVal by props.reply.collectAsState("", Dispatchers.Main.immediate)

    ReactQuill {
        id = "discussion_reply"
        onChange = props.onReplyChanged
        value = replyVal
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
        disabled = (replyVal.isEmpty() || props.disabled)

        +strings[MR.strings.post]
    }
}
