package com.ustadmobile.core.domain.contententry.launchcontent.xapi

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.ext.agent
import com.ustadmobile.core.domain.xapi.ext.registrationUuid
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.starthttpsession.StartXapiSessionOverHttpUseCase
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.tincan.Activity
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.bodyAsDecodedText
import com.ustadmobile.core.util.ext.localFirstThenRepoIfNull
import com.ustadmobile.core.util.requireEntryByUri
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.setInputString
import io.github.aakira.napier.Napier
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
    private val startXapiSessionOverHttpUseCase: StartXapiSessionOverHttpUseCase,
    private val stringHasher: XXStringHasher,
    private val endpoint: Endpoint,
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
        contentEntryVersionUid: Long,
        xapiSession: XapiSessionEntity,
    ) : XapiLaunchHrefResult {
        //ContentEntryVersion is immutable, so if available in db, no need to go to repo which would
        //make an http request
        Napier.v { "Resolving xAPI url for contentEntryVersion $contentEntryVersionUid" }
        val contentEntryVersion = activeRepo.localFirstThenRepoIfNull {
            it.contentEntryVersionDao().findByUidAsync(contentEntryVersionUid)
        } ?: throw IllegalArgumentException("could not load contententryversion $contentEntryVersionUid")

        val manifestUrl = contentEntryVersion.cevManifestUrl ?:
            throw IllegalStateException("ContentEntryVersion $contentEntryVersionUid manifesturl is null")
        val manifest: ContentManifest = json.decodeFromString(httpClient.get(manifestUrl).bodyAsDecodedText())
        val tinCanEntry = manifest.requireEntryByUri(contentEntryVersion.cevOpenUri!!)

        val tinCanXmlStr = httpClient.get(tinCanEntry.bodyDataUrl).bodyAsDecodedText()

        val xpp = xppFactory.newPullParser()
        xpp.setInputString(tinCanXmlStr)
        val tinCanXml = TinCanXML.loadFromXML(xpp)
        val launchHref = tinCanXml.launchActivity?.launchUrl ?:
            throw IllegalStateException("ContentEntryVersion $contentEntryVersionUid manifesturl is null")
        val launchActivityId = tinCanXml.launchActivity?.id ?:
            throw IllegalStateException("Launch Activity must have id")

        val xapiSessionWithActivityId = xapiSession.copy(
            xseRootActivityId = launchActivityId,
            xseRootActivityUid = stringHasher.hash(launchActivityId)
        )

        val httpSessionResult = startXapiSessionOverHttpUseCase(xapiSessionWithActivityId)

        val queryParams: Map<String, String> = mapOf(
            "endpoint" to httpSessionResult.httpUrl,
            "auth" to httpSessionResult.auth,
            "actor" to json.encodeToString(XapiAgent.serializer(), xapiSession.agent(endpoint)),
            "registration" to xapiSessionWithActivityId.registrationUuid.toString(),
            "activity_id" to xapiSessionWithActivityId.xseRootActivityId,
        )

        val url = UMFileUtil.resolveLink(manifestUrl, launchHref).appendQueryArgs(queryParams)

        val tinCanXmlPathPrefix = contentEntryVersion.cevOpenUri!!
            .substringBeforeLast("/", "")

        Napier.v { "Resolved xAPI url for contentEntryVersion $contentEntryVersionUid : $url" }
        return XapiLaunchHrefResult(
            url = url,
            launchUriInContent = "$tinCanXmlPathPrefix${launchHref}".appendQueryArgs(queryParams),
            launchActivity = tinCanXml.launchActivity!!,
            manifestUrl = manifestUrl,
        )
    }


}