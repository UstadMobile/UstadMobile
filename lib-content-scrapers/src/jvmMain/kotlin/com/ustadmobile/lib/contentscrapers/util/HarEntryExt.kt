package com.ustadmobile.lib.contentscrapers.util

import com.ustadmobile.core.contentformats.har.HarContent
import com.ustadmobile.core.contentformats.har.HarNameValuePair
import com.ustadmobile.core.contentformats.har.HarRequest
import com.ustadmobile.core.contentformats.har.HarResponse
import net.lightbody.bmp.core.har.HarEntry

fun HarEntry.toHarEntryContent(): com.ustadmobile.core.contentformats.har.HarEntry{
    return com.ustadmobile.core.contentformats.har.HarEntry().apply {

        request = HarRequest().apply {

            val proxyRequest = this@toHarEntryContent.request
            bodySize = proxyRequest.bodySize
            headers = proxyRequest.headers.map { HarNameValuePair(it.name, it.value) }
            method = proxyRequest.method
            queryString = proxyRequest.queryString.map { HarNameValuePair(it.name, it.value) }
            url = proxyRequest.url
            headersSize = proxyRequest.headersSize
        }

        response = HarResponse().apply {

            val proxyResponse = this@toHarEntryContent.response
            status = proxyResponse.status
            statusText = proxyResponse.statusText
            headers = proxyResponse.headers.map { HarNameValuePair(it.name, it.value) }
            headersSize = proxyResponse.headersSize
            bodySize = proxyResponse.bodySize
            content = HarContent().apply {

                val proxyContent = this@toHarEntryContent.response.content
                encoding = proxyContent.encoding
                mimeType = proxyContent.mimeType
                text = proxyContent.text
                size = proxyContent.size

            }

        }

    }
}