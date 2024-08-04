package com.ustadmobile.core.domain.contententry.launchcontent.xapi

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCase
import com.ustadmobile.core.domain.xapi.ext.agent
import com.ustadmobile.core.domain.xapi.ext.authorizationHeader
import com.ustadmobile.core.domain.xapi.ext.registrationUuid
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.starthttpsession.ResumeOrStartXapiSessionUseCase
import com.ustadmobile.core.tincan.Activity
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.bodyAsDecodedText
import com.ustadmobile.core.util.ext.localFirstThenRepoIfNull
import com.ustadmobile.core.util.requireEntryByUri
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.setInputString
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.json.Json

/**
 * Shared logic that will open the TinCan XML for a given ContentEntryVersionUid and work out
 * the launch Url, title, etc. This is used by LaunchXapiUseCase to determine the URL when launching
 * Xapi content in external systems (e.g. ChromeTabs, Chrome or other external browser on desktop,
 * and new window on JS) and XapiContentViewModel.
 */
class ResolveXapiLaunchHrefUseCase(
    private val activeRepo: UmAppDatabase,
    private val httpClient: HttpClient,
    private val json: Json,
    private val xppFactory: XmlPullParserFactory,
    private val resumeOrStartXapiSessionUseCase: ResumeOrStartXapiSessionUseCase,
    private val getApiUrlUseCase: GetApiUrlUseCase,
    private val accountManager: UstadAccountManager,
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
        clazzUid: Long,
        cbUid: Long,
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
        val accountPersonUid = accountManager.currentUserSession.person.personUid

        val xapiSession = resumeOrStartXapiSessionUseCase(
            accountPersonUid = accountPersonUid,
            actor = accountManager.currentUserSession.toXapiAgent(),
            activityId = launchActivityId,
            clazzUid = clazzUid,
            cbUid = cbUid,
            contentEntryUid = contentEntryVersion.cevContentEntryUid,
        )

        val queryParams: Map<String, String> = mapOf(
            "endpoint" to getApiUrlUseCase("/api/xapi/"),
            "auth" to xapiSession.authorizationHeader(),
            "actor" to json.encodeToString(XapiAgent.serializer(), xapiSession.agent(endpoint)),
            "registration" to xapiSession.registrationUuid.toString(),
            "activity_id" to xapiSession.xseRootActivityId,
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