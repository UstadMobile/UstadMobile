package com.ustadmobile.view.components

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.attachments.retrieveAttachment
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.mui.components.AvatarVariant
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.view.UstadBaseComponent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.instance
import org.kodein.di.on
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.url.URL
import react.RBuilder
import react.dom.html.ImgHTMLAttributes
import react.setState

/**
 * The lookup adapter should return the attachment uri for a given entity primary key
 */
fun interface AttachmentImageLookupAdapter {

    suspend fun lookupAttachmentUri(
        db: UmAppDatabase,
        entityUid: Long
    ): String?

}

interface AttachmentImageLookupProps: UmProps {

    /**
     * The entity primary key to lookup
     */
    var entityUid: Long

    /**
     * A lookup adapter that can be used to retrieve the attachment uri
     */
    var lookupAdapter: AttachmentImageLookupAdapter?

    /**
     * RBuilder function that will use the localUrl (e.g. to display as part of an avatar etc)
     */
    var contentBlock: (RBuilder.(attachmentLocalUrl: String?) -> Unit)?

}

interface AttachmentImageLookupState: UmState {

    var imgSrc: String?

}

/**
 * This component will use its lifecycle to manage creating and revoking a local url for entity
 * image attachments (e.g. person picture, clazz picture, etc).
 */
open class AttachmentImageLookupComponent(
    props: AttachmentImageLookupProps
): UstadBaseComponent<AttachmentImageLookupProps, AttachmentImageLookupState>(props) {

    override fun onCreateView() {
        super.onCreateView()

        lookupImage()
    }

    override fun componentDidUpdate(
        prevProps: AttachmentImageLookupProps,
        prevState: AttachmentImageLookupState,
        snapshot: Any
    ) {
        super.componentDidUpdate(prevProps, prevState, snapshot)

        if(prevProps.entityUid != props.entityUid || prevProps.lookupAdapter != props.lookupAdapter)
            lookupImage()
    }

    override fun componentWillUnmount() {
        super.componentWillUnmount()

        state.imgSrc?.also {
            console.log("AttachmentImageLookupComp: unmount / revoke $it")
            URL.revokeObjectURL(it)
        }
    }

    fun lookupImage(){
        GlobalScope.launch {
            val accountManager: UstadAccountManager by di.instance()
            val db: UmAppDatabase by di.on(accountManager.activeAccount).instance(tag = DoorTag.TAG_DB)

            console.log("AttachmentImageLookupComp: Lookup entity uid = ${props.entityUid}")
            val attachmentUri = props.lookupAdapter?.lookupAttachmentUri(db, props.entityUid)

            //If there was a previously created URL, revoke it.
            state.imgSrc?.also {
                console.log("AttachmentImageLookupComp: revoke $it")
                URL.revokeObjectURL(it)
            }

            val imgSrcUrl = attachmentUri?.let { db.retrieveAttachment(it) }
            console.log("AttachmentImageLookupComp: imgSrcUrl= $imgSrcUrl")
            setState {
                imgSrc = imgSrcUrl?.toString()
            }
        }
    }

    override fun RBuilder.render() {
        props.contentBlock?.invoke(this, state.imgSrc)
    }
}