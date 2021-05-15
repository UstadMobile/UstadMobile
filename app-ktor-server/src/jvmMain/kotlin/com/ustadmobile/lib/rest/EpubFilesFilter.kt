package com.ustadmobile.lib.rest
import com.ustadmobile.port.sharedse.impl.http.EpubHtmlFilterSerializer
import java.io.ByteArrayInputStream
import org.kodein.di.DI
import org.kodein.di.DIAware
import java.io.InputStream

/**
 * Container Filter that will use EpubHtmlFilterSerializer to add meta viewport and some responsive
 * css to HTML that does not already container it.
 */
class EpubFilesFilter(override val di: DI): DIAware {

    val HTML_MIME_TYPES = arrayOf("text/html", "application/xhtml+xml")

    fun filterResponse(responseIn:InputStream, mimeType: String): InputStream {
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
}