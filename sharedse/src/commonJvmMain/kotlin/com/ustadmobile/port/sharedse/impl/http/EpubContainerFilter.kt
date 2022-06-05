package com.ustadmobile.port.sharedse.impl.http
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import java.io.ByteArrayInputStream
import com.ustadmobile.port.sharedse.ext.dataInflatedIfRequired
import com.ustadmobile.port.sharedse.util.XmlPassThroughFilter
import com.ustadmobile.port.sharedse.util.passXmlThrough
import org.kodein.di.DI
import org.kodein.di.DIAware
import java.io.InputStream

/**
 * Container Filter that will use EpubHtmlFilterSerializer to add meta viewport and some responsive
 * css to HTML that does not already container it.
 */
class EpubContainerFilter(override val di: DI): MountedContainerResponder.MountedContainerFilter, DIAware {

    val HTML_MIME_TYPES = arrayOf("text/html", "application/xhtml+xml")

    fun filterResponse(responseIn: InputStream, mimeType: String): InputStream {
        if(HTML_MIME_TYPES.any { mimeType.startsWith(it) }) {
            //filter it
            try {
                val htmlFilterSerializer = EpubHtmlFilterSerializer(di).also {
                    it.setIntput(responseIn)
                    it.liveWebServer = true
                }
                val filteredHtmlBytes = htmlFilterSerializer.output
                return ByteArrayInputStream(filteredHtmlBytes)
            }catch(e: Exception) {
                //If there is an exception processing it, return the original.
                return responseIn
            }

        }else {
            return responseIn
        }
    }

    override fun filterResponse(responseIn: NanoHTTPD.Response, uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val contentType = responseIn.getMimeType()
        if(HTML_MIME_TYPES.any { contentType.startsWith(it) }) {
            //filter it
            try {
                val htmlFilterSerializer = EpubHtmlFilterSerializer(di).also {
                    it.setIntput(responseIn.dataInflatedIfRequired())
                }

                val filteredHtmlBytes = htmlFilterSerializer.output

                return NanoHTTPD.newFixedLengthResponse(responseIn.status, contentType,
                        ByteArrayInputStream(filteredHtmlBytes), filteredHtmlBytes.size.toLong())
            }catch(e: Exception) {
                //If there is an exception processing it, return the original.
                return responseIn
            }

        }else {
            return responseIn
        }
    }
}