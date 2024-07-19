package com.ustadmobile.core.domain.contententry.launchcontent.epub

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.contententry.launchcontent.LaunchContentEntryVersionUseCase
import com.ustadmobile.core.domain.htmlcontentdisplayengine.LaunchChromeUseCase
import com.ustadmobile.core.embeddedhttp.EmbeddedHttpServer
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentViewModel
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.domain.xapi.XapiSession

/*
 * Launching EPUB on Compose/Desktop(JVM) is done by launching Chrome and using the app-react
 * (Kotlin/JS) implementation via the embedded server, because there is currently no good WebView
 * solution available for Compose/Desktop.
 *
 * This use case will adjust the manifest url to be based on the embedded server and supply this as
 * an argument to the EpubContentScreen. This means the JS version does not need to use its database
 * etc.
 */
class LaunchEpubUseCaseJvm(
    private val launchChromeUseCase: LaunchChromeUseCase,
    private val embeddedHttpServer: EmbeddedHttpServer,
    private val endpoint: Endpoint,
    private val systemImpl: UstadMobileSystemImpl,
    private val getApiUrlUseCase: GetApiUrlUseCase,
): LaunchEpubUseCase {

    override suspend fun invoke(
        contentEntryVersion: ContentEntryVersion,
        navController: UstadNavController,
        target: OpenExternalLinkUseCase.Companion.LinkTarget,
        xapiSession: XapiSession,
    ): LaunchContentEntryVersionUseCase.LaunchResult {
        val manifestUrl = contentEntryVersion.cevManifestUrl ?:
            throw IllegalStateException("ContentEntryVersion $contentEntryVersion manifesturl is null")
        val localManifestUrl = getApiUrlUseCase(manifestUrl.substringAfter(endpoint.url))
        val xapiStatementsUrl = getApiUrlUseCase("api/xapi/statement")

        val cevOpenUri = contentEntryVersion.cevOpenUri ?:
            throw IllegalStateException("ContentEntryVersion $contentEntryVersion openUri is null")

        val tocString = systemImpl.getString(MR.strings.table_of_contents)
        val url = getApiUrlUseCase(
             path = "umapp/#/${EpubContentViewModel.DEST_NAME}?" +
                    "${EpubContentViewModel.ARG_MANIFEST_URL}=${UrlEncoderUtil.encode(localManifestUrl)}&" +
                    "${EpubContentViewModel.ARG_CEV_URI}=${UrlEncoderUtil.encode(cevOpenUri)}&" +
                    "${EpubContentViewModel.ARG_NAVIGATION_VISIBLE}=false&" +
                    "${EpubContentViewModel.ARG_TOC_OPTIONS_STRING}=${UrlEncoderUtil.encode(tocString)}&" +
                    "${EpubContentViewModel.ARG_XAPI_STATEMENTS_URL}=${UrlEncoderUtil.encode(xapiStatementsUrl)}"

        )
        launchChromeUseCase(url)
        return LaunchContentEntryVersionUseCase.LaunchResult()
    }
}