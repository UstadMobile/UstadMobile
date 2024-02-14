package com.ustadmobile.core.domain.contententry.launchcontent.xapi

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.tincan.Activity
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.bodyAsDecodedText
import com.ustadmobile.core.util.requireEntryByUri
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.setInputString
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.json.Json

/**
 * Shared logic that will open the TinCan XML for a given ContentEntryVersionUid and work out
 * the launch Url, title, etc. This is used by LaunchXapiUseCase to determine the URL when launching
 * Xapi content in external systems (e.g. ChromeTabs) and XapiContentViewModel.
 */
class ResolveXapiLaunchHrefUseCase(
    private val activeRepo: UmAppDatabase,
    private val httpClient: HttpClient,
    private val json: Json,
    private val xppFactory: XmlPullParserFactory,
) {

    /**
     * @param url the absolute, based on the manifest url of the ContentEntryVersion
     * @param launchUriInContent the path to the HTML as specified by the launch URL relative to the
     *        content base. In reality, the tincan.xml file is almost always
     */
    class XapiLaunchHrefResult(
        val url: String,
        val launchUriInContent: String,
        val launchActivity: Activity,
        val manifestUrl: String,
    )

    suspend operator fun invoke(
        contentEntryVersionUid: Long
    ) : XapiLaunchHrefResult {
        val contentEntryVersion = activeRepo.contentEntryVersionDao
            .findByUidAsync(contentEntryVersionUid) ?:
                throw IllegalArgumentException("could not load contententryversion $contentEntryVersionUid")
        val manifestUrl = contentEntryVersion.cevManifestUrl ?:
            throw IllegalStateException("ContentEntryVersion $contentEntryVersionUid manifesturl is null")
        val manifest: ContentManifest = json.decodeFromString(httpClient.get(manifestUrl).bodyAsDecodedText())
        val tinCanEntry = manifest.requireEntryByUri(
            contentEntryVersion.cevOpenUri!!)

        val tinCanXmlStr = httpClient.get(tinCanEntry.bodyDataUrl).bodyAsDecodedText()

        val xpp = xppFactory.newPullParser()
        xpp.setInputString(tinCanXmlStr)
        val tinCanXml = TinCanXML.loadFromXML(xpp)
        val launchHref = tinCanXml.launchActivity?.launchUrl ?:
            throw IllegalStateException("ContentEntryVersion $contentEntryVersionUid manifesturl is null")
        val url = UMFileUtil.resolveLink(manifestUrl, launchHref)

        val tinCanXmlPathPrefix = contentEntryVersion.cevOpenUri!!
            .substringBeforeLast("/", "")

        return XapiLaunchHrefResult(
            url = url,
            launchUriInContent = "$tinCanXmlPathPrefix${launchHref}",
            launchActivity = tinCanXml.launchActivity!!,
            manifestUrl = manifestUrl,
        )
    }


}