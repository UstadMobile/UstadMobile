package com.ustadmobile.view.components

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorDatabaseRepository.Companion.DOOR_ATTACHMENT_URI_PREFIX
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.attachments.retrieveAttachment
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.view.UstadBaseComponent
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.Visibility
import kotlinx.css.visibility
import kotlinx.html.InputType
import kotlinx.html.id
import org.kodein.di.instance
import org.kodein.di.on
import org.w3c.dom.url.URL
import org.w3c.files.File
import react.*
import react.dom.onChange
import react.dom.onClick
import styled.css
import styled.styledImg
import styled.styledInput

interface AttachmentImageProps: UmProps {

    var onNewImageSelected: (String?) -> Unit

    var attachmentUri: String?

}

interface AttachmentImageState: UmState {

    var imageSrc: String?

}

class AttachmentImageComponent(
    props: AttachmentImageProps
) : UstadBaseComponent<AttachmentImageProps, AttachmentImageState>(props) {

    private val db: UmAppDatabase by di.on(accountManager.activeAccount).instance(tag = DoorTag.TAG_DB)

    override fun componentDidMount() {
        GlobalScope.launch {
            resolveUrl()
        }
    }

    override fun componentDidUpdate(
        prevProps: AttachmentImageProps,
        prevState: AttachmentImageState,
        snapshot: Any
    ) {
        super.componentDidUpdate(prevProps, prevState, snapshot)

        if(prevProps.attachmentUri != props.attachmentUri) {
//            state.imageSrc?.also {
//                URL.revokeObjectURL(it)
//            }

            GlobalScope.launch {
                resolveUrl()
            }
        }
    }

    override fun componentWillUnmount() {
//        state.imageSrc?.also {
//            URL.revokeObjectURL(it)
//        }
    }

    suspend fun resolveUrl() {
        val attachmentUri = props.attachmentUri
        console.log("AttachmentImage: resolving $attachmentUri")
        if(attachmentUri != null) {
            val newImgSrc = if(attachmentUri.startsWith(DOOR_ATTACHMENT_URI_PREFIX)) {
                db.retrieveAttachment(attachmentUri)
            }else {
                DoorUri.parse(attachmentUri)
            }

            setState {
                imageSrc = newImgSrc.toString()
            }
        }
    }



    override fun RBuilder.render() {

        styledImg(src = state.imageSrc) {
            attrs.width = "100px"
            attrs.height = "100px"
            attrs.onClick = {
                //launch the file picker
                document.getElementById("foo").asDynamic().click()
            }
        }

        styledInput(type = InputType.file) {
            css {
                visibility = Visibility.hidden
            }
            attrs.id = "foo"
            attrs.onChange = { evt ->
                evt.stopPropagation()
                evt.preventDefault()

                GlobalScope.launch {
                    val file: File = evt.asDynamic().target.files[0] as File
                    val url = URL.createObjectURL(file)
                    props.onNewImageSelected(url)
                }
            }
        }

    }
}