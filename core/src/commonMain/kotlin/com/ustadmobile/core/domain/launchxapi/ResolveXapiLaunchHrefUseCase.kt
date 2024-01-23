package com.ustadmobile.core.domain.launchxapi

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.requireEntryByUri
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.setInputString
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class ResolveXapiLaunchHrefUseCase(
    private val activeRepo: UmAppDatabase,
    private val httpClient: HttpClient,
    private val xppFactory: XmlPullParserFactory,
) {

    class XapiLaunchHrefResult(
        val url: String,
    )

    suspend operator fun invoke(
        contentEntryVersionUid: Long
    ) : XapiLaunchHrefResult {
        val contentEntryVersion = activeRepo.contentEntryVersionDao
            .findByUidAsync(contentEntryVersionUid) ?:
                throw IllegalArgumentException("could not load contententryversion $contentEntryVersionUid")
        val manifestUrl = contentEntryVersion.cevManifestUrl ?:
            throw IllegalStateException("ContentEntryVersion $contentEntryVersionUid manifesturl is null")
        val manifest: ContentManifest = httpClient.get(manifestUrl).body()
        val tinCanEntry = manifest.requireEntryByUri(
            contentEntryVersion.cevOpenUri!!)

        val tinCanXmlStr = httpClient.get(tinCanEntry.bodyDataUrl).bodyAsText()

        val xpp = xppFactory.newPullParser()
        xpp.setInputString(tinCanXmlStr)
        val tinCanXml = TinCanXML.loadFromXML(xpp)
        val launchHref = tinCanXml.launchActivity?.launchUrl ?:
            throw IllegalStateException("ContentEntryVersion $contentEntryVersionUid manifesturl is null")
        val url = UMFileUtil.resolveLink(manifestUrl, launchHref)
        return XapiLaunchHrefResult(url)
    }


}