package com.ustadmobile.view

import com.ustadmobile.core.controller.ChatDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ChatDetailView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.Chat
import com.ustadmobile.lib.db.entities.MessageRead
import com.ustadmobile.lib.db.entities.MessageWithPerson
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.spacingUnits
import com.ustadmobile.mui.components.umFab
import com.ustadmobile.mui.components.umInput
import com.ustadmobile.mui.ext.targetInputValue
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.chatDetailNewMessage
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.StyleManager.messageContainer
import com.ustadmobile.util.StyleManager.messageSendButton
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.util.Util
import com.ustadmobile.view.ext.renderConversationListItem
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.*
import mui.material.FabColor
import mui.material.FabVariant
import mui.material.Size
import react.Key
import react.Props
import react.RBuilder
import react.setState
import styled.css

class ChatDetailComponent(props: UmProps): UstadBaseComponent<UmProps, UmState>(props), ChatDetailView {

    private var mPresenter: ChatDetailPresenter? = null

    private var typedMessage = ""

    private var messages: List<MessageWithPerson> = mutableListOf()

    private var enterNewLine = false

    private val observer = ObserverFnWrapper<List<MessageWithPerson>>{
        if(it.isEmpty()) return@ObserverFnWrapper
        setState {
            messages = it.reversed()
        }
    }
    override var title: String?
        get() = ustadComponentTitle
        set(value) {
            ustadComponentTitle = value
        }

    override var messageList: DataSourceFactory<Int, MessageWithPerson>? = null
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
            if(value?.chatTitle != null){
                updateUiWithStateChangeDelay {
                    ustadComponentTitle = value.chatTitle
                }
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        fabManager?.visible = false
        mPresenter = ChatDetailPresenter(this, arguments, this, di)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        umGridContainer {
            css {
                +contentContainer
                +defaultPaddingTop
            }

            umItem {
                css{
                    margin(bottom = 10.spacingUnits)
                }
                messages.forEach {
                    val fromMe = accountManager.activeAccount.personUid == it.messagePerson?.personUid
                    //Update message read
                    if(it.messageRead == null) {
                        val messageRead = MessageRead(
                            accountManager.activeAccount.personUid, it.messageUid,
                            it.messageEntityUid ?: 0L
                        )
                        mPresenter?.updateMessageRead(messageRead)
                        it.messageRead = messageRead
                    }
                    renderConversationListItem(
                        !fromMe,
                        if(fromMe) getString(MessageID.you) else it.messagePerson?.fullName(),
                        it.messageText,
                        systemImpl,
                        accountManager,
                        this,
                        it.messageTimestamp,

                    )
                }
            }

            umGridContainer {
                css(messageContainer)
                umItem(GridSize.cells12, if(typedMessage.isNotEmpty()) GridSize.cells10
                else GridSize.cells12) {
                    css(chatDetailNewMessage)
                    umInput(typedMessage,
                        placeholder = getString(MessageID.type_here),
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
                        },
                        onKeyDown = {
                            if(it.shiftKey && it.key.lowercase() == "enter"){
                                it.preventDefault()
                                it.target.asDynamic().value += "\n"
                            }
                            if(!it.shiftKey && it.key.lowercase() == "enter"){
                                it.preventDefault()
                                handleSendMessage()
                            }
                        }
                    ) {
                        css{
                            fontSize = (1.3).em
                        }
                        attrs.asDynamic().inputProps = object: Props {
                            override var key: Key?= "${StyleManager.name}-chatInputMessageClass"
                            val className = "${StyleManager.name}-chatInputMessageClass"
                        }
                    }
                }
                if(typedMessage.isNotEmpty()){
                    umItem(GridSize.cells1, flexDirection = FlexDirection.rowReverse) {
                        css(messageSendButton)
                        umFab("send","",
                            id = "um-chat-send",
                            variant = FabVariant.circular,
                            size = Size.large,
                            color = FabColor.secondary,
                            onClick = {
                                Util.stopEventPropagation(it)
                                if(typedMessage.isNotEmpty()){
                                    handleSendMessage()
                                }
                            }) {
                            css{
                                marginTop = 1.spacingUnits
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleSendMessage(){
        if(typedMessage.isNotEmpty()){
            mPresenter?.addMessage(typedMessage)
            setState {
                typedMessage = ""
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
    }

}