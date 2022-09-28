package com.ustadmobile.port.sharedse.impl.http



import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.util.UMFileUtil
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import com.ustadmobile.core.view.ContainerMounter
import kotlin.jvm.JvmOverloads
import org.kodein.di.*
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.sharedse.network.NetworkManagerBle
import com.ustadmobile.sharedse.impl.http.vhToPxFactor
import com.ustadmobile.door.ext.DoorTag

/**
 * Embedded HTTP Server which runs to serve files directly out of a zipped container on the fly
 *
 * Mounted zips will be acessible under http://IP:PORT/mount/mountName
 *
 * For performance reasons mounted zip files are served with cache headers with a max-age to prevent
 * additional requests - therefor the mountName should include a date or timestamp component to prevent
 * stale files being served.
 *
 * Created by mike on 8/14/15.
 */
open class EmbeddedHTTPD @JvmOverloads constructor(portNum: Int, override val di: DI) : RouterNanoHTTPD(portNum), DIAware, ContainerMounter {

    private val id: Int

    private val responseListeners = Vector<ResponseListener>()

    private val networkManager: NetworkManagerBle by instance()

    /**
     * Returns the local URL in the form of http://localhost;PORT/
     *
     * @return Local URL as above including a trailing slash
     */
    val localURL: String
        get() = "http://localhost:$listeningPort/"

    /**
     * Get the local HTTP server url with the URL as it is to be used for access over the loopback
     * interface
     *
     * @return Local http server url e.g. http://127.0.0.1:PORT/
     */
    val localHttpUrl: String
        get() = "http://127.0.0.1:$listeningPort/"


    interface ResponseListener {

        fun responseStarted(session: NanoHTTPD.IHTTPSession, response: NanoHTTPD.Response)

        fun responseFinished(session: NanoHTTPD.IHTTPSession, response: NanoHTTPD.Response?)

    }


    init {
        id = idCounter
        idCounter++

        addRoute("/:${ContainerEntryListResponder.PATH_VAR_ENDPOINT}/ContainerEntryList/findByContainerWithMd5",
                ContainerEntryListResponder::class.java, di)
        addRoute("/:${XapiStatementResponder.URI_PARAM_ENDPOINT}/xapi/:contentEntryUid/:clazzUid/statements",
                XapiStatementResponder::class.java, di)
        addRoute("/:${XapiStateResponder.URI_PARAM_ENDPOINT}/xapi/activities/state",
                XapiStateResponder::class.java, di)
    }


    override fun serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
        val response = super.serve(session)
        if (!responseListeners.isEmpty() && response != null) {
            fireResponseStarted(session, response)
            response.data = InputStreamWithCloseListener(response.data, object : InputStreamWithCloseListener.OnCloseListener {
                override fun onStreamClosed() {
                    fireResponseFinished(session, response)
                }
            })
        }

        return response
    }

    override fun addMappings() {
        super.addMappings()
    }


    override fun toString(): String {
        return "EmbeddedHTTPServer on port : $listeningPort id: $id"
    }


    @JvmOverloads
    override suspend fun mountContainer(endpointUrl: String, containerUid: Long, filterMode: Int): String {
        val endpoint = Endpoint(endpointUrl)
        val endpointDb: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)
        val endpointRepo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

        val container = endpointRepo.containerDao.findByUidAsync(containerUid)
                ?: throw IllegalArgumentException("Container $containerUid on $endpointUrl not found")

        val mountPath = "/${sanitizeDbNameFromUrl(endpointUrl)}/container/$containerUid/"

        val filters = if(filterMode == ContainerMounter.FILTER_MODE_EPUB) {
            listOf<MountedContainerResponder.MountedContainerFilter>(EpubContainerFilter(di),
                CssVhFilter() {vhToPxFactor()})
        }else {
            listOf<MountedContainerResponder.MountedContainerFilter>()
        }

        addRoute("$mountPath${MountedContainerResponder.URI_ROUTE_POSTFIX}",
                MountedContainerResponder::class.java, containerUid.toString(), endpointDb, filters)
        return UMFileUtil.joinPaths(localHttpUrl, mountPath)
    }


    override suspend fun unMountContainer(endpointUrl: String, mountPath: String) {
        removeRoute(mountPath + MountedContainerResponder.URI_ROUTE_POSTFIX)
    }

    /**
     * Add an entry response listener. This will receive response events when entries are sent to
     * clients.
     *
     * @param listener
     */
    fun addResponseListener(listener: ResponseListener) {
        responseListeners.add(listener)
    }

    /**
     * Remove an entry response listener.
     *
     * @param listener
     */
    fun removeResponseListener(listener: ResponseListener) {
        responseListeners.remove(listener)
    }

    protected fun fireResponseStarted(session: NanoHTTPD.IHTTPSession, response: NanoHTTPD.Response) {
        synchronized(responseListeners) {
            for (listener in responseListeners) {
                listener.responseStarted(session, response)
            }
        }
    }

    protected fun fireResponseFinished(session: NanoHTTPD.IHTTPSession, response: NanoHTTPD.Response?) {
        synchronized(responseListeners) {
            for (listener in responseListeners) {
                listener.responseFinished(session, response)
            }
        }
    }

    fun newSession(inputStream: InputStream, outputStream: OutputStream): IHTTPSession =
            HTTPSession(tempFileManagerFactory.create(), inputStream, outputStream)

    companion object {

        var idCounter = 0

        const val PREFIX_MOUNT = "/mount/"

        /**
         * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
         */
        private val theMimeTypes = HashMap<String, String>()

        init {
            theMimeTypes["htm"] = "text/html"
            theMimeTypes["html"] = "text/html"
            theMimeTypes["xhtml"] = "application/xhtml+xml"
            theMimeTypes["xml"] = "text/xml"
            theMimeTypes["txt"] = "text/plain"
            theMimeTypes["webp"] = "image/webp"
            theMimeTypes["webm"] = "video/webm"

            val st = StringTokenizer(
                    "css		text/css " +
                            "asc		text/plain " +
                            "gif		image/gif " +
                            "jpg		image/jpeg " +
                            "jpeg		image/jpeg " +
                            "png		image/png " +
                            "mp3		audio/mpeg " +
                            "m3u		audio/mpeg-url " +
                            "mp4		video/mp4 " +
                            "m4v        video/mp4 " +
                            "ogv		video/ogg " +
                            "flv		video/x-flv " +
                            "mov		video/quicktime " +
                            "swf		application/x-shockwave-flash " +
                            "js			application/javascript " +
                            "pdf		application/pdf " +
                            "doc		application/msword " +
                            "ogg		application/x-ogg " +
                            "zip		application/octet-stream " +
                            "exe		application/octet-stream " +
                            "wav		audio/wav " +
                            "class		application/octet-stream " +
                            "docx       application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            while (st.hasMoreTokens())
                theMimeTypes[st.nextToken()] = st.nextToken()
        }

        fun getMimeType(uri: String): String {
            val mimeResult = theMimeTypes[UMFileUtil.getExtension(uri)]
            return mimeResult ?: "application/octet-stream"
        }
    }
}
