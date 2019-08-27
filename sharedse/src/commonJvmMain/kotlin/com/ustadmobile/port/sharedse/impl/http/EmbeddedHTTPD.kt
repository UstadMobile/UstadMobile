package com.ustadmobile.port.sharedse.impl.http


import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.util.UMFileUtil
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.exception.ZipException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

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
open class EmbeddedHTTPD @JvmOverloads constructor(portNum: Int, private val context: Any, private val appDatabase: UmAppDatabase = UmAppDatabase.getInstance(context), private val repository: UmAppDatabase = UmAppDatabase.getInstance(context)) : RouterNanoHTTPD(portNum) {

    private val id: Int

    private val responseListeners = Vector<ResponseListener>()

    private val mountedZips = Hashtable<String, ZipFile>()

    private val mountedContainers = Hashtable<String, ContainerManager>()

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

    val containerMounter: suspend (Long) -> String = {containerUid ->
        val contPath = mountContainer(containerUid, null)
        UMFileUtil.joinPaths(localHttpUrl, contPath!!)
    }


    interface ResponseListener {

        fun responseStarted(session: NanoHTTPD.IHTTPSession, response: NanoHTTPD.Response)

        fun responseFinished(session: NanoHTTPD.IHTTPSession, response: NanoHTTPD.Response?)

    }


    init {
        id = idCounter
        idCounter++
        addRoute("/ContainerEntryFile/(.*)+", ContainerEntryFileResponder::class.java, appDatabase)
        addRoute("/ContainerEntryList/findByContainerWithMd5(.*)+",
                ContainerEntryListResponder::class.java, appDatabase)
        addRoute("/xapi/statements(.*)+", XapiStatementResponder::class.java, repository)
        addRoute("/xapi/activities/state(.*)+", XapiStateResponder::class.java, repository)
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


    /**
     * Mount a zip to the given path.  The contents of the zip file will then be accessible by
     * HTTP using http://IP:PORT/mount/mountPath
     *
     * Zips should be unmounted when they are no longer needed.  Depending on how Android feels
     * this service may live on after an activity is finished.  The mounted zip keeps a cached
     * copy of the ZipFile object containing entry names, file sizes, data positions etc.
     *
     * For performance the mountPath should include a time/date component.  All files served will be
     * with cache a 1 year maxage cache header
     *
     * @param mountPath The path to use after /mount .
     * @param zipPath The local filesystem path to the zip file (e.g. /path/to/file.epub)
     */
    @Deprecated("")
    fun mountZip(zipPath: String, mountPath: String?): String? {
        var mountPath = mountPath
        if (mountPath == null) {
            mountPath = UMFileUtil.getFilename(zipPath) + '-'.toString() +
                    SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        }

        try {
            val zipFile = ZipFile(zipPath)
            addRoute(PREFIX_MOUNT + mountPath + "/" + MountedZipHandler.URI_ROUTE_POSTFIX,
                    MountedZipHandler::class.java, zipFile)
            val fullPath = toFullZipMountPath(mountPath)
            mountedZips[fullPath!!] = zipFile
            return toFullZipMountPath(mountPath)
        } catch (e: ZipException) {
            UMLog.l(UMLog.ERROR, 90, zipPath, e)
        }

        return null
    }

    @JvmOverloads
    fun mountContainer(containerUid: Long, mountPath: String?,
                       filters: List<MountedContainerResponder.MountedContainerFilter> = ArrayList()): String? {
        val container = repository.containerDao.findByUid(containerUid) ?: return null
        val containerManager = ContainerManager(container, appDatabase, repository)
        return mountContainer(containerManager, mountPath, filters)
    }

    fun mountContainer(containerManager: ContainerManager, mountPath: String?,
                       filters: List<MountedContainerResponder.MountedContainerFilter>): String {
        var mountPath = mountPath
        if (mountPath == null) {
            mountPath = "/container/" + containerManager.containerUid + "/" +
                    System.currentTimeMillis() + "/"
        }

        addRoute(mountPath + MountedContainerResponder.URI_ROUTE_POSTFIX,
                MountedContainerResponder::class.java, context, filters)

        return mountPath
    }

    fun unmountContainer(mountPath: String) {
        removeRoute(mountPath + MountedContainerResponder.URI_ROUTE_POSTFIX)
    }

    private fun toFullZipMountPath(mountPath: String): String? {
        try {
            return PREFIX_MOUNT + URLEncoder.encode(mountPath, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            //Should enver happen
            UMLog.l(UMLog.ERROR, 0, null, e)
        }

        return null
    }

    /**
     * Unmount a zip that was mounted with mountZip
     *
     * @param mountPath The mount path given to mount the zip
     */
    fun unmountZip(mountPath: String) {
        val encodedPath = mountPath.substring(PREFIX_MOUNT.length)
        try {
            val route = (PREFIX_MOUNT + URLDecoder.decode(encodedPath, "UTF-8") + "/"
                    + MountedZipHandler.URI_ROUTE_POSTFIX)
            removeRoute(route)
            mountedZips.remove(toFullZipMountPath(mountPath)!!)
        } catch (e: UnsupportedEncodingException) {
            UMLog.l(UMLog.ERROR, 20, mountPath, e)
        }

    }

    /**
     * Convenience method to make the ZipFile object accessible if a presenter needs it after
     * mounting it on http. This will avoid having to read the file again.
     *
     * @param mountPath The path as returned by mountZip
     *
     * @return ZipFile object for the zip that was mounted on that path, null if it's not mounted.
     */
    fun getMountedZip(mountPath: String): ZipFile? {
        return mountedZips[mountPath]
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

    /**
     * Mount a Zip File to the http server.  Optionally specify a preferred mount point (useful if
     * the activity is being created from a saved state)
     *
     * ***PORTED FROM NetworkManager***. TODO: refactor / clean this up somewhat.
     *
     * @param zipPath Path to the zip that should be mounted (mandatory)
     * @param mountName Directory name that this should be mounted as e.g. something.epub-timestamp. Can be null
     *
     * @return The mountname that was used - the content will then be accessible on getZipMountURL()/return value
     */
    fun mountZipOnHttp(zipPath: String, mountName: String?): String? {
        var mountName = mountName
        UMLog.l(UMLog.INFO, 371, "Mount zip " + zipPath + " on service "
                + this + "httpd server = " + this + " listening port = " + listeningPort)

        mountName = mountZip(zipPath, mountName)
        return mountName
    }

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
