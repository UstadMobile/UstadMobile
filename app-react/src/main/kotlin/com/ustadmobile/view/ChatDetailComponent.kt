package com.ustadmobile.view

import com.ustadmobile.core.controller.ChatDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ChatDetailView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.Chat
import com.ustadmobile.lib.db.entities.MessageWithPerson
import com.ustadmobile.mui.components.ButtonSize
import com.ustadmobile.mui.components.FabVariant
import com.ustadmobile.mui.components.umFab
import com.ustadmobile.mui.components.umInput
import com.ustadmobile.mui.ext.targetInputValue
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.chatDetailNewMessage
import com.ustadmobile.util.StyleManager.chatMessage
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.StyleManager.messageSendButton
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import kotlinx.browser.document
import kotlinx.css.Color
import kotlinx.css.em
import kotlinx.css.fontSize
import react.Props
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ChatDetailComponent(props: UmProps): UstadBaseComponent<UmProps, UmState>(props), ChatDetailView {

    override val viewNames: List<String>
        get() = listOf(ChatDetailView.VIEW_NAME)

    private var mPresenter: ChatDetailPresenter? = null

    private var typedMessage = ""

    private var showSendButton = false

    private var messages: List<MessageWithPerson> = mutableListOf()

    private val observer = ObserverFnWrapper<List<MessageWithPerson>>{
        if(it.isEmpty()) return@ObserverFnWrapper
        setState {
            messages = it
        }
    }
    override var title: String?
        get() = ustadComponentTitle
        set(value) {
            ustadComponentTitle = value
        }

    override var messageList: DoorDataSourceFactory<Int, MessageWithPerson>? = null
        get() = field
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(observer)
            liveData?.observe(this, observer)
        }

    override var editButtonMode: EditButtonMode = EditButtonMode.GONE

    override var entity: Chat? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.messages)
        mPresenter = ChatDetailPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
        updateUiWithStateChangeDelay(1000) {
            val messageInput = document.getElementById("um-message-input")
            messageInput?.addEventListener("focusin", {
                setState {
                    showSendButton = true
                }
            })

            messageInput?.addEventListener("focusout", {
                setState {
                    showSendButton = false
                }
            })
        }
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +contentContainer
                +defaultPaddingTop
            }

            styledDiv{
                css {
                    +chatDetailNewMessage
                    +chatMessage
                }

                umInput(typedMessage,
                    textColor = Color.white,
                    disableUnderline = true,
                    endAdornment = null,
                    rowsMax = 2,
                    multiline = true,
                    id =  "um-message-input",
                    onChange = {
                        setState {
                            typedMessage = it.targetInputValue
                        }
                    }
                ) {
                    css{
                        fontSize = (1.3).em
                    }
                    attrs.asDynamic().inputProps = object: Props {
                        val className = "${StyleManager.name}-chatInputMessageClass"
                    }
                }
            }

            if(showSendButton){
                umFab("send","",
                    id = "um-chat-send",
                    variant = FabVariant.round,
                    size = ButtonSize.large,
                    color = UMColor.secondary,
                    onClick = {
                        if(typedMessage.isNotEmpty()){
                            mPresenter?.addMessage(typedMessage)
                            setState {
                                typedMessage = ""
                            }
                        }
                    }) {
                    css(messageSendButton)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
    }

}