package com.ustadmobile.view.components

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.attachments.retrieveAttachment
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.view.UstadBaseComponent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.w3c.dom.url.URL
import react.RBuilder
import react.setState

/**
 * The lookup adapter should return the attachment uri for a given entity primary key
 */
fun interface AttachmentImageLookupAdapter {

    fun lookupAttachmentUri(db: UmAppDatabase, entityUid: Long): DoorLiveData<String?>

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

    private var lastAttachmentUri: String? = null

    private var imageLookupJob: Job? = null

    private var currentLiveData: DoorLiveData<String?>? = null

    private lateinit var db: UmAppDatabase

    private val uriObserver = DoorObserver<String?> { attachmentUri ->
        if(attachmentUri == lastAttachmentUri)
            return@DoorObserver

        imageLookupJob?.cancel()
        lastAttachmentUri = attachmentUri
        imageLookupJob = GlobalScope.launch {
            console.log("AttachmentImageLookupComp: Lookup entity uid = ${props.entityUid}")

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

    private fun setupLiveData() {
        currentLiveData?.removeObserver(uriObserver)
        currentLiveData = props.lookupAdapter?.lookupAttachmentUri(db, props.entityUid)
        currentLiveData?.observe(this, uriObserver)
    }

    override fun onCreateView() {
        super.onCreateView()
        db = di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_DB)

        setupLiveData()
    }

    override fun componentDidUpdate(
        prevProps: AttachmentImageLookupProps,
        prevState: AttachmentImageLookupState,
        snapshot: Any
    ) {
        super.componentDidUpdate(prevProps, prevState, snapshot)

        if(prevProps.entityUid != props.entityUid || prevProps.lookupAdapter != props.lookupAdapter)
            setupLiveData()
    }

    override fun componentWillUnmount() {
        super.componentWillUnmount()

        currentLiveData?.removeObserver(uriObserver)
        state.imgSrc?.also {
            console.log("AttachmentImageLookupComp: unmount / revoke $it")
            URL.revokeObjectURL(it)
        }
    }

    override fun RBuilder.render() {
        props.contentBlock?.invoke(this, state.imgSrc)
    }
}