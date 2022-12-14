package com.ustadmobile.view

import com.ustadmobile.core.controller.DiscussionPostDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.DiscussionPostDetailView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.lib.db.entities.MessageWithPerson
import com.ustadmobile.mui.components.*
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
import com.ustadmobile.view.ext.umItemThumbnail
import kotlinx.css.*
import mui.material.AvatarVariant
import mui.material.FabColor
import mui.material.FabVariant
import mui.material.Size
import mui.material.styles.TypographyVariant
import react.Props
import react.RBuilder
import react.setState
import styled.css

class DiscussionPostDetailComponent(props: UmProps): UstadBaseComponent<UmProps, UmState>(props),
    DiscussionPostDetailView {

    private var mPresenter: DiscussionPostDetailPresenter? = null

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

    override var replies: DataSourceFactory<Int, MessageWithPerson>? = null
        get() = field
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(observer)
            liveData?.observe(this, observer)
        }

    override var editButtonMode: EditButtonMode = EditButtonMode.GONE

    override var entity: DiscussionPostWithDetails? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
            if(value?.discussionPostTitle != null){
                updateUiWithStateChangeDelay {
                    ustadComponentTitle = value.discussionPostTitle
                }
            }

        }


    override fun onCreateView() {
        super.onCreateView()
        fabManager?.visible = false
        mPresenter = DiscussionPostDetailPresenter(this, arguments, this, di)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {



        umGridContainer {
            css {
                +contentContainer
                +defaultPaddingTop
            }

            umItem(GridSize.cells2,GridSize.cells1) {
                umItemThumbnail("person", avatarVariant = AvatarVariant.circular)
            }

            umItem(GridSize.cells8, GridSize.cells9) {
                umTypography(entity?.authorPersonFirstNames + " " + entity?.authorPersonLastName,
                    variant = TypographyVariant.h6) {
                    css {
                        +StyleManager.alignTextToStart
                    }

                }

                linkifyReactTextView(entity?.discussionPostMessage, systemImpl, accountManager, this)
            }

            umItem {
                css{
                    margin(bottom = 10.spacingUnits)
                }
                messages.forEach {
                    val fromMe = accountManager.activeAccount.personUid == it.messagePerson?.personUid
                    renderConversationListItem(
                        !fromMe,
                        if(fromMe) getString(MessageID.you) else it.messagePerson?.fullName(),
                        it.messageText,
                        systemImpl,
                        accountManager,
                        this,
                        it.messageTimestamp
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
                            override var key: String?= "${StyleManager.name}-chatInputMessageClass"
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